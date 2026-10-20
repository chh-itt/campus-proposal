package com.campus.proposal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 提案人评价办结 */
public record EvaluateRequest(
        @NotNull(message = "满意度评分不能为空") @Min(value = 1, message = "评分最低 1 星") @Max(value = 5, message = "评分最高 5 星") Integer rating,
        @Size(max = 500, message = "评价备注不能超过 500 字") String comment) {
}
