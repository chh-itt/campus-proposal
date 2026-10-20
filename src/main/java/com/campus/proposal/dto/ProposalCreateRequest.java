package com.campus.proposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProposalCreateRequest(
        @NotBlank(message = "标题不能为空") @Size(min = 4, max = 100, message = "标题长度需在 4-100 字之间") String title,
        @NotBlank(message = "内容不能为空") @Size(min = 5, max = 2000, message = "内容长度需在 5-2000 字之间") String content) {
}
