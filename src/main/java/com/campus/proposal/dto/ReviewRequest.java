package com.campus.proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 审查决定:accept=true 立案 / false 不予立案 */
public record ReviewRequest(
        @NotNull(message = "审查结论不能为空") Boolean accept,
        @Size(max = 500, message = "审查意见不能超过 500 字") String note) {
}
