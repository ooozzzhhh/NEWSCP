package com.newscp.backend.sys.dept;

import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.dept.dto.DeptCreateDTO;
import com.newscp.backend.sys.dept.dto.DeptUpdateDTO;
import com.newscp.backend.sys.dept.entity.SysDept;
import com.newscp.backend.sys.dept.mapper.SysDeptMapper;
import com.newscp.backend.sys.dept.mapper.SysUserDeptMapper;
import com.newscp.backend.sys.dept.vo.DeptTreeVO;
import com.newscp.backend.sys.dept.vo.DeptUserVO;
import com.newscp.backend.tenant.TenantContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserDeptMapper userDeptMapper;

    public DeptService(SysDeptMapper deptMapper, SysUserDeptMapper userDeptMapper) {
        this.deptMapper = deptMapper;
        this.userDeptMapper = userDeptMapper;
    }

    public List<DeptTreeVO> tree() {
        List<SysDeptMapper.DeptFlatRow> rows = deptMapper.selectFlatRows();
        Map<Long, DeptTreeVO> nodeMap = new HashMap<>();
        for (SysDeptMapper.DeptFlatRow row : rows) {
            DeptTreeVO node = toNode(row);
            node.setUserCount(userDeptMapper.countUsersByDeptId(row.id()));
            nodeMap.put(row.id(), node);
        }

        List<DeptTreeVO> roots = new ArrayList<>();
        for (SysDeptMapper.DeptFlatRow row : rows) {
            DeptTreeVO node = nodeMap.get(row.id());
            Long parentId = row.parentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    public List<DeptTreeVO> listFlat() {
        return deptMapper.selectFlatRows().stream().map(this::toNode).toList();
    }

    @Transactional
    public Long create(DeptCreateDTO dto) {
        validateParent(dto.parentId(), null);
        String deptName = normalizeName(dto.deptName());
        if (deptMapper.countSiblingName(dto.parentId(), deptName) > 0) {
            throw new BusinessException("同级部门名称已存在");
        }
        SysDept dept = new SysDept();
        dept.setTenantId(TenantContext.getTenantId());
        dept.setDeptName(deptName);
        dept.setParentId(dto.parentId());
        dept.setLeaderId(dto.leaderId());
        dept.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        dept.setRemark(dto.remark());
        dept.setCreatedBy(SecurityUtils.getCurrentUserId());
        dept.setCreatedAt(LocalDateTime.now());
        dept.setDeleted(0);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Transactional
    public void update(Long id, DeptUpdateDTO dto) {
        SysDept dept = getDeptOrThrow(id);
        validateParent(dto.parentId(), id);
        String deptName = normalizeName(dto.deptName());
        if (deptMapper.countSiblingNameExclude(dto.parentId(), deptName, id) > 0) {
            throw new BusinessException("同级部门名称已存在");
        }

        dept.setDeptName(deptName);
        dept.setParentId(dto.parentId());
        dept.setLeaderId(dto.leaderId());
        dept.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        dept.setRemark(dto.remark());
        dept.setUpdatedBy(SecurityUtils.getCurrentUserId());
        dept.setUpdatedAt(LocalDateTime.now());
        deptMapper.updateById(dept);
    }

    @Transactional
    public void delete(Long id) {
        getDeptOrThrow(id);
        if (deptMapper.countChildren(id) > 0) {
            throw new BusinessException("存在子部门，请先删除子部门");
        }
        long userCount = userDeptMapper.countUsersByDeptId(id);
        if (userCount > 0) {
            throw new BusinessException("部门下存在 " + userCount + " 个用户，请先将用户迁移至其他部门");
        }
        deptMapper.deleteById(id);
    }

    public List<DeptUserVO> users(Long deptId) {
        getDeptOrThrow(deptId);
        return userDeptMapper.selectDeptUsers(deptId).stream()
                .map(row -> new DeptUserVO(row.userId(), row.username(), row.realName(), row.status()))
                .toList();
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null) {
            throw new BusinessException("上级部门不能为空");
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException("不能将自身设为上级部门");
        }
        if (parentId == 0) {
            return;
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
            throw new BusinessException("上级部门不存在");
        }

        if (selfId == null) {
            return;
        }
        Map<Long, Long> parentMap = deptMapper.selectFlatRows().stream()
                .collect(HashMap::new, (m, row) -> m.put(row.id(), row.parentId()), HashMap::putAll);
        Long cursor = parentId;
        while (cursor != null && cursor != 0) {
            if (selfId.equals(cursor)) {
                throw new BusinessException("不允许把部门移动到自己的子部门下");
            }
            cursor = parentMap.get(cursor);
        }
    }

    private SysDept getDeptOrThrow(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null || Integer.valueOf(1).equals(dept.getDeleted())) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    private DeptTreeVO toNode(SysDeptMapper.DeptFlatRow row) {
        DeptTreeVO node = new DeptTreeVO();
        node.setId(row.id());
        node.setDeptName(row.deptName());
        node.setParentId(row.parentId());
        node.setLeaderId(row.leaderId());
        node.setLeaderName(row.leaderName());
        node.setSortOrder(row.sortOrder());
        node.setRemark(row.remark());
        node.setUserCount(0);
        return node;
    }

    private void sortTree(List<DeptTreeVO> nodes) {
        nodes.sort(Comparator.comparing(DeptTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DeptTreeVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (DeptTreeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private String normalizeName(String deptName) {
        if (!StringUtils.hasText(deptName)) {
            throw new BusinessException("部门名称不能为空");
        }
        return deptName.trim();
    }
}
