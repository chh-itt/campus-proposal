package com.campus.proposal.controller;

import com.campus.proposal.dto.LoginRequest;
import com.campus.proposal.entity.User;
import com.campus.proposal.exception.BizException;
import com.campus.proposal.repository.UserRepository;
import com.campus.proposal.service.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 会话登录:登录成功后把 userId 写进 HttpSession,拦截器据此放行 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthContext authContext;

    public AuthController(UserRepository userRepository, AuthContext authContext) {
        this.userRepository = userRepository;
        this.authContext = authContext;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginRequest req, HttpSession session) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BizException(400, "用户名或密码错误"));
        // 演示环境明文比对;生产必须改为 BCrypt 哈希校验,且错误提示不区分"用户不存在/密码错误",
        // 避免向外部暴露账号是否存在
        if (!user.getPassword().equals(req.password())) {
            throw new BizException(400, "用户名或密码错误");
        }
        session.setAttribute(AuthContext.SESSION_KEY, user.getId());
        return Map.of("user", user);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request) {
        return Map.of("user", authContext.requireUser(request));
    }
}
