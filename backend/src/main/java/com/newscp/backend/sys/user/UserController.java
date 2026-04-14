package com.newscp.backend.sys.user;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.user.dto.PasswordChangeDTO;
import com.newscp.backend.sys.user.dto.UserCreateDTO;
import com.newscp.backend.sys.user.dto.UserPageQueryDTO;
import com.newscp.backend.sys.user.dto.UserProfileUpdateDTO;
import com.newscp.backend.sys.user.dto.UserUpdateDTO;
import com.newscp.backend.sys.user.vo.ResetPasswordVO;
import com.newscp.backend.sys.user.vo.UserDetailVO;
import com.newscp.backend.sys.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping
    public ApiResponse<PageResult<UserVO>> page(UserPageQueryDTO query) {
        return ApiResponse.ok(userService.page(query));
    }

    @PreAuthorize("hasAuthority('sys:user:create')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return ApiResponse.ok(userService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:user:read')")
    @GetMapping("/{id}")
    public ApiResponse<UserDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(userService.detail(id));
    }

    @PreAuthorize("hasAuthority('sys:user:update')")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.update(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:user:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:user:update')")
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        userService.enable(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:user:update')")
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        userService.disable(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:user:reset-pwd')")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<ResetPasswordVO> resetPassword(@PathVariable Long id) {
        return ApiResponse.ok(userService.resetPassword(id));
    }

    @GetMapping("/me")
    public ApiResponse<UserDetailVO> me() {
        return ApiResponse.ok(userService.me());
    }

    @PutMapping("/me/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return ApiResponse.ok();
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(dto);
        return ApiResponse.ok();
    }
}
