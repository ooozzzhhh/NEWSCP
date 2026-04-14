package com.newscp.backend.auth;

import com.newscp.backend.sys.role.mapper.SysPermissionMapper;
import com.newscp.backend.sys.user.entity.SysUser;
import com.newscp.backend.sys.user.mapper.SysUserMapper;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;

    public UserDetailsServiceImpl(SysUserMapper userMapper, SysPermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(Long.parseLong(userId));
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        List<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());
        Collection<? extends GrantedAuthority> authorities = permCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        boolean enabled = "ACTIVE".equals(user.getStatus());
        boolean accountNonLocked = !"LOCKED".equals(user.getStatus());
        return new User(
                userId,
                user.getPasswordHash(),
                enabled,
                true,
                true,
                accountNonLocked,
                authorities
        );
    }
}
