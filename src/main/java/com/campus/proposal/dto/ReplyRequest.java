package com.campus.proposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 承办部门答复 */
public record ReplyRequest(
        @NotBlank(message = "答复内容不能为空") @Size(min = 5, max = 2000, message = "答复长度需在 5-2000 字之间") String reply) {
}
