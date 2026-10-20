package com.campus.proposal.dto;

import jakarta.validation.constraints.NotNull;

/** 分办请求:指定承办部门的经办账号 */
public record AssignRequest(@NotNull(message = "承办部门不能为空") Long deptUserId) {
}
