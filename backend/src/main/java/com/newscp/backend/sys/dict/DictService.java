package com.newscp.backend.sys.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.dict.dto.DictBatchItemDTO;
import com.newscp.backend.sys.dict.dto.DictItemDTO;
import com.newscp.backend.sys.dict.dto.DictTypeDTO;
import com.newscp.backend.sys.dict.entity.DictItem;
import com.newscp.backend.sys.dict.entity.DictType;
import com.newscp.backend.sys.dict.mapper.DictItemMapper;
import com.newscp.backend.sys.dict.mapper.DictTypeMapper;
import com.newscp.backend.sys.dict.vo.DictDropdownVO;
import com.newscp.backend.sys.dict.vo.DictItemVO;
import com.newscp.backend.sys.dict.vo.DictTypeVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DictService {

    private static final String SOURCE_BUILTIN = "BUILTIN";
    private static final String SOURCE_CUSTOM = "CUSTOM";
    private static final Set<String> VALID_SOURCE = Set.of(SOURCE_BUILTIN, SOURCE_CUSTOM);

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public DictService(DictTypeMapper dictTypeMapper, DictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    public PageResult<DictTypeVO> pageTypes(String tenantId, Integer page, Integer size, String keyword) {
        Page<DictType> mpPage = new Page<>(safePage(page), safeSize(size));
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<DictType>()
                .eq(DictType::getTenantId, tenantId)
                .eq(DictType::getDeleted, 0)
                .orderByAsc(DictType::getSortOrder)
                .orderByAsc(DictType::getId);
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(DictType::getTypeCode, k).or().like(DictType::getTypeName, k));
        }
        Page<DictType> result = dictTypeMapper.selectPage(mpPage, wrapper);
        List<DictTypeVO> records = result.getRecords().stream().map(this::toTypeVO).toList();
        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), records);
    }

    public DictTypeVO getType(String tenantId, Long id) {
        return toTypeVO(getTypeOrThrow(tenantId, id));
    }

    public List<DictItemVO> listItems(String tenantId, String typeCode) {
        String code = normalizeTypeCode(typeCode);
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getTenantId, tenantId)
                .eq(DictItem::getTypeCode, code)
                .eq(DictItem::getDeleted, 0)
                .orderByAsc(DictItem::getSortOrder)
                .orderByAsc(DictItem::getId);
        return dictItemMapper.selectList(wrapper).stream().map(this::toItemVO).toList();
    }

    @Cacheable(cacheNames = "dictDropdown", key = "#root.target.buildDropdownCacheKey(#tenantId, #typeCodes)")
    public Map<String, List<DictDropdownVO>> dropdown(String tenantId, List<String> typeCodes) {
        List<String> normalized = normalizeTypeCodes(typeCodes);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DictDropdownVO> rows = dictItemMapper.selectEnabledDropdown(tenantId, normalized);
        Map<String, List<DictDropdownVO>> grouped = new LinkedHashMap<>();
        for (String code : normalized) {
            grouped.put(code, new ArrayList<>());
        }
        for (DictDropdownVO row : rows) {
            grouped.computeIfAbsent(row.typeCode(), ignored -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public Long createType(String tenantId, DictTypeDTO dto) {
        DictType type = new DictType();
        type.setTenantId(tenantId);
        type.setTypeCode(normalizeTypeCode(dto.typeCode()));
        type.setTypeName(normalizeRequired(dto.typeName(), "类型名称不能为空"));
        type.setSource(normalizeSource(dto.source()));
        type.setEditable(asBoolean(dto.editable(), "editable"));
        type.setStatus(asBoolean(dto.status(), "status"));
        type.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        type.setRemark(normalizeNullable(dto.remark()));
        type.setCreatedBy(SecurityUtils.getCurrentUserId());
        type.setCreatedAt(LocalDateTime.now());
        type.setDeleted(0);
        if (existsTypeCode(tenantId, type.getTypeCode(), null)) {
            throw new BusinessException("类型编码已存在");
        }
        dictTypeMapper.insert(type);
        return type.getId();
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public void updateType(String tenantId, Long id, DictTypeDTO dto) {
        DictType current = getTypeOrThrow(tenantId, id);
        String source = normalizeSource(dto.source());
        if (SOURCE_BUILTIN.equals(current.getSource()) && !SOURCE_BUILTIN.equals(source)) {
            throw new BusinessException("内置类型来源不可改为 CUSTOM");
        }
        String typeCode = normalizeTypeCode(dto.typeCode());
        if (!Objects.equals(current.getTypeCode(), typeCode) && existsTypeCode(tenantId, typeCode, id)) {
            throw new BusinessException("类型编码已存在");
        }

        String oldTypeCode = current.getTypeCode();
        current.setTypeCode(typeCode);
        current.setTypeName(normalizeRequired(dto.typeName(), "类型名称不能为空"));
        current.setSource(source);
        current.setEditable(asBoolean(dto.editable(), "editable"));
        current.setStatus(asBoolean(dto.status(), "status"));
        current.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        current.setRemark(normalizeNullable(dto.remark()));
        current.setUpdatedBy(SecurityUtils.getCurrentUserId());
        current.setUpdatedAt(LocalDateTime.now());
        dictTypeMapper.updateById(current);

        if (!Objects.equals(oldTypeCode, typeCode)) {
            List<DictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                    .eq(DictItem::getTenantId, tenantId)
                    .eq(DictItem::getTypeCode, oldTypeCode)
                    .eq(DictItem::getDeleted, 0));
            for (DictItem item : items) {
                item.setTypeCode(typeCode);
                item.setUpdatedBy(SecurityUtils.getCurrentUserId());
                item.setUpdatedAt(LocalDateTime.now());
                dictItemMapper.updateById(item);
            }
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public void deleteType(String tenantId, Long id) {
        DictType current = getTypeOrThrow(tenantId, id);
        if (SOURCE_BUILTIN.equals(current.getSource())) {
            throw new BusinessException("BUILTIN 类型不可删除");
        }
        List<DictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getTenantId, tenantId)
                .eq(DictItem::getTypeCode, current.getTypeCode())
                .eq(DictItem::getDeleted, 0));
        for (DictItem item : items) {
            dictItemMapper.deleteById(item.getId());
        }
        dictTypeMapper.deleteById(current.getId());
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public Long createItem(String tenantId, DictItemDTO dto) {
        DictType type = getTypeByCodeOrThrow(tenantId, dto.typeCode());
        if (!isEditable(type)) {
            throw new BusinessException("当前类型不允许新增枚举值");
        }
        String value = normalizeValue(dto.value());
        if (existsItemValue(tenantId, type.getTypeCode(), value, null)) {
            throw new BusinessException("枚举值已存在");
        }
        DictItem item = new DictItem();
        item.setTenantId(tenantId);
        item.setTypeCode(type.getTypeCode());
        item.setValue(value);
        item.setLabel(normalizeRequired(dto.label(), "label不能为空"));
        item.setLabelEn(normalizeNullable(dto.labelEn()));
        item.setColor(normalizeNullable(dto.color()));
        item.setExtra(normalizeNullable(dto.extra()));
        item.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        item.setStatus(asBoolean(dto.status(), "status"));
        item.setIsDefault(asBoolean(dto.isDefault(), "isDefault"));
        item.setRemark(normalizeNullable(dto.remark()));
        item.setCreatedBy(SecurityUtils.getCurrentUserId());
        item.setCreatedAt(LocalDateTime.now());
        item.setDeleted(0);
        dictItemMapper.insert(item);
        return item.getId();
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public void updateItem(String tenantId, Long id, DictItemDTO dto) {
        DictItem item = getItemOrThrow(tenantId, id);
        DictType type = getTypeByCodeOrThrow(tenantId, item.getTypeCode());
        if (!isEditable(type)) {
            throw new BusinessException("当前类型不允许修改枚举值");
        }
        String value = normalizeValue(dto.value());
        if (existsItemValue(tenantId, item.getTypeCode(), value, id)) {
            throw new BusinessException("枚举值已存在");
        }
        item.setValue(value);
        item.setLabel(normalizeRequired(dto.label(), "label不能为空"));
        item.setLabelEn(normalizeNullable(dto.labelEn()));
        item.setColor(normalizeNullable(dto.color()));
        item.setExtra(normalizeNullable(dto.extra()));
        item.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        item.setStatus(asBoolean(dto.status(), "status"));
        item.setIsDefault(asBoolean(dto.isDefault(), "isDefault"));
        item.setRemark(normalizeNullable(dto.remark()));
        item.setUpdatedBy(SecurityUtils.getCurrentUserId());
        item.setUpdatedAt(LocalDateTime.now());
        dictItemMapper.updateById(item);
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public void deleteItem(String tenantId, Long id) {
        DictItem item = getItemOrThrow(tenantId, id);
        DictType type = getTypeByCodeOrThrow(tenantId, item.getTypeCode());
        if (!isEditable(type)) {
            throw new BusinessException("当前类型不允许删除枚举值");
        }
        dictItemMapper.deleteById(id);
    }

    @Transactional
    @CacheEvict(cacheNames = "dictDropdown", allEntries = true)
    public void batchSortItems(String tenantId, DictBatchItemDTO dto) {
        if (dto.items() == null || dto.items().isEmpty()) {
            throw new BusinessException("排序项不能为空");
        }
        Map<Long, Integer> idToSort = dto.items().stream()
                .collect(Collectors.toMap(DictBatchItemDTO.SortItem::id, DictBatchItemDTO.SortItem::sortOrder, (a, b) -> b, HashMap::new));
        List<DictItem> items = dictItemMapper.selectBatchIds(idToSort.keySet()).stream()
                .filter(item -> tenantId.equals(item.getTenantId()) && !Objects.equals(item.getDeleted(), 1))
                .toList();
        if (items.size() != idToSort.size()) {
            throw new BusinessException("存在无效枚举项");
        }
        for (DictItem item : items) {
            item.setSortOrder(idToSort.get(item.getId()));
            item.setUpdatedBy(SecurityUtils.getCurrentUserId());
            item.setUpdatedAt(LocalDateTime.now());
            dictItemMapper.updateById(item);
        }
    }

    private DictType getTypeOrThrow(String tenantId, Long id) {
        DictType type = dictTypeMapper.selectById(id);
        if (type == null || Objects.equals(type.getDeleted(), 1) || !tenantId.equals(type.getTenantId())) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    private DictType getTypeByCodeOrThrow(String tenantId, String typeCode) {
        String code = normalizeTypeCode(typeCode);
        DictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getTenantId, tenantId)
                .eq(DictType::getTypeCode, code)
                .eq(DictType::getDeleted, 0)
                .last("LIMIT 1"));
        if (type == null) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    private DictItem getItemOrThrow(String tenantId, Long id) {
        DictItem item = dictItemMapper.selectById(id);
        if (item == null || Objects.equals(item.getDeleted(), 1) || !tenantId.equals(item.getTenantId())) {
            throw new BusinessException("字典枚举项不存在");
        }
        return item;
    }

    private boolean existsTypeCode(String tenantId, String typeCode, Long excludeId) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<DictType>()
                .eq(DictType::getTenantId, tenantId)
                .eq(DictType::getTypeCode, typeCode)
                .eq(DictType::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(DictType::getId, excludeId);
        }
        return dictTypeMapper.selectCount(wrapper) > 0;
    }

    private boolean existsItemValue(String tenantId, String typeCode, String value, Long excludeId) {
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getTenantId, tenantId)
                .eq(DictItem::getTypeCode, typeCode)
                .eq(DictItem::getValue, value)
                .eq(DictItem::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(DictItem::getId, excludeId);
        }
        return dictItemMapper.selectCount(wrapper) > 0;
    }

    private boolean isEditable(DictType type) {
        return type.getEditable() != null && type.getEditable() == 1;
    }

    private DictTypeVO toTypeVO(DictType type) {
        return new DictTypeVO(
                type.getId(),
                type.getTenantId(),
                type.getTypeCode(),
                type.getTypeName(),
                type.getSource(),
                type.getEditable(),
                type.getStatus(),
                type.getSortOrder(),
                type.getRemark()
        );
    }

    private DictItemVO toItemVO(DictItem item) {
        return new DictItemVO(
                item.getId(),
                item.getTenantId(),
                item.getTypeCode(),
                item.getValue(),
                item.getLabel(),
                item.getLabelEn(),
                item.getColor(),
                item.getExtra(),
                item.getSortOrder(),
                item.getStatus(),
                item.getIsDefault(),
                item.getRemark()
        );
    }

    private List<String> normalizeTypeCodes(List<String> typeCodes) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            return List.of();
        }
        return typeCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .toList();
    }

    public String buildDropdownCacheKey(String tenantId, List<String> typeCodes) {
        return tenantId + ":" + String.join(",", normalizeTypeCodes(typeCodes));
    }

    private String normalizeTypeCode(String value) {
        String normalized = normalizeRequired(value, "typeCode不能为空").toUpperCase();
        if (!normalized.matches("^[A-Z0-9_]{2,64}$")) {
            throw new BusinessException("typeCode格式非法（仅支持大写字母、数字、下划线）");
        }
        return normalized;
    }

    private String normalizeValue(String value) {
        String normalized = normalizeRequired(value, "value不能为空").toUpperCase();
        if (!normalized.matches("^[A-Z0-9_\\-]{1,64}$")) {
            throw new BusinessException("value格式非法（仅支持字母、数字、下划线、中划线）");
        }
        return normalized;
    }

    private String normalizeSource(String value) {
        String source = normalizeRequired(value, "source不能为空").toUpperCase();
        if (!VALID_SOURCE.contains(source)) {
            throw new BusinessException("source仅支持 BUILTIN 或 CUSTOM");
        }
        return source;
    }

    private String normalizeRequired(String value, String msg) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(msg);
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Integer asBoolean(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException(field + " 必须是0或1");
        }
        return value;
    }

    private long safePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private long safeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
