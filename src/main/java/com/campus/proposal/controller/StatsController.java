package com.campus.proposal.controller;

import com.campus.proposal.common.ProposalStatus;
import com.campus.proposal.entity.Proposal;
import com.campus.proposal.entity.User;
import com.campus.proposal.service.AuthContext;
import com.campus.proposal.service.ProposalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 总览统计:数据口径与列表接口一致(提案人只统计自己的) */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ProposalService proposalService;
    private final AuthContext authContext;

    public StatsController(ProposalService proposalService, AuthContext authContext) {
        this.proposalService = proposalService;
        this.authContext = authContext;
    }

    @GetMapping
    public Map<String, Object> overview(HttpServletRequest request) {
        User me = authContext.requireUser(request);
        List<Proposal> all = proposalService.listFor(me, null);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ProposalStatus s : ProposalStatus.values()) {
            byStatus.put(s.name(), all.stream().filter(p -> p.getStatus() == s).count());
        }

        double avgRating = all.stream().map(Proposal::getRating)
                .filter(r -> r != null).mapToInt(Integer::intValue).average().orElse(0);
        double avgHandleDays = all.stream()
                .filter(p -> p.getStatus() == ProposalStatus.CLOSED)
                .map(p -> Duration.between(p.getCreatedAt(), p.getUpdatedAt()).toHours() / 24.0)
                .mapToDouble(d -> d).average().orElse(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", all.size());
        result.put("byStatus", byStatus);
        result.put("closedCount", byStatus.get(ProposalStatus.CLOSED.name()));
        result.put("avgRating", Math.round(avgRating * 10) / 10.0);
        result.put("avgHandleDays", Math.round(avgHandleDays * 10) / 10.0);
        return result;
    }
}
