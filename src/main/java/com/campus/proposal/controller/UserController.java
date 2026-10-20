package com.campus.proposal.controller;

import com.campus.proposal.common.Role;
import com.campus.proposal.entity.User;
import com.campus.proposal.repository.UserRepository;
import com.campus.proposal.service.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final AuthContext authContext;

    public UserController(UserRepository userRepository, AuthContext authContext) {
        this.userRepository = userRepository;
        this.authContext = authContext;
    }

    /** 按角色查用户列表(分办下拉框用),仅提案工作办公室可调用 */
    @GetMapping
    public List<User> byRole(@RequestParam Role role, HttpServletRequest request) {
        User me = authContext.requireUser(request);
        authContext.requireRole(me, Role.STAFF, "仅提案工作办公室可查询用户列表");
        return userRepository.findByRole(role);
    }
}
