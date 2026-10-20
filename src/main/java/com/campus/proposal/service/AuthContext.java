package com.campus.proposal.service;

import com.campus.proposal.common.Role;
import com.campus.proposal.entity.User;
import com.campus.proposal.exception.BizException;
import com.campus.proposal.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** 从会话解析当前登录用户,并提供角色断言 */
@Component
public class AuthContext {

    public static final String SESSION_KEY = "uid";

    private final UserRepository userRepository;

    public AuthContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(HttpServletRequest request) {
        Object uid = request.getSession().getAttribute(SESSION_KEY);
        if (uid == null) {
            throw new BizException(401, "未登录或会话已过期");
        }
        return userRepository.findById((Long) uid)
                .orElseThrow(() -> new BizException(401, "账号不存在"));
    }

    public void requireRole(User user, Role role, String message) {
        if (user.getRole() != role) {
            throw new BizException(403, message);
        }
    }
}
