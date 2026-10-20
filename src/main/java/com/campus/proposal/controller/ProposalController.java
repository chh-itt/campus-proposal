package com.campus.proposal.controller;

import com.campus.proposal.common.ProposalStatus;
import com.campus.proposal.dto.AssignRequest;
import com.campus.proposal.dto.EvaluateRequest;
import com.campus.proposal.dto.ProposalCreateRequest;
import com.campus.proposal.dto.ReplyRequest;
import com.campus.proposal.dto.ReviewRequest;
import com.campus.proposal.entity.Proposal;
import com.campus.proposal.entity.User;
import com.campus.proposal.service.AuthContext;
import com.campus.proposal.service.ProposalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalService proposalService;
    private final AuthContext authContext;

    public ProposalController(ProposalService proposalService, AuthContext authContext) {
        this.proposalService = proposalService;
        this.authContext = authContext;
    }

    /** 列表:提案人只见自己的,工作人员/承办部门见全部;?status= 按状态筛选 */
    @GetMapping
    public List<Proposal> list(@RequestParam(required = false) ProposalStatus status,
                               HttpServletRequest request) {
        User me = authContext.requireUser(request);
        return proposalService.listFor(me, status);
    }

    /** 详情 + 办理留痕 */
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id, HttpServletRequest request) {
        authContext.requireUser(request);
        return proposalService.detail(id);
    }

    /** 提交提案(仅学生) */
    @PostMapping
    public Map<String, Object> create(@RequestBody @Valid ProposalCreateRequest req,
                                      HttpServletRequest request) {
        User me = authContext.requireUser(request);
        Proposal p = proposalService.create(me, req);
        return proposalService.detail(p.getId());
    }

    /** 审查:立案 / 不予立案(仅提案工作办公室) */
    @PostMapping("/{id}/review")
    public Map<String, Object> review(@PathVariable Long id, @RequestBody @Valid ReviewRequest req,
                                      HttpServletRequest request) {
        User me = authContext.requireUser(request);
        proposalService.review(me, id, req);
        return proposalService.detail(id);
    }

    /** 分办到承办部门(仅提案工作办公室) */
    @PostMapping("/{id}/assign")
    public Map<String, Object> assign(@PathVariable Long id, @RequestBody @Valid AssignRequest req,
                                      HttpServletRequest request) {
        User me = authContext.requireUser(request);
        proposalService.assign(me, id, req);
        return proposalService.detail(id);
    }

    /** 承办答复(仅被分办的承办部门) */
    @PostMapping("/{id}/reply")
    public Map<String, Object> reply(@PathVariable Long id, @RequestBody @Valid ReplyRequest req,
                                     HttpServletRequest request) {
        User me = authContext.requireUser(request);
        proposalService.reply(me, id, req);
        return proposalService.detail(id);
    }

    /** 评价办结(仅提案人本人) */
    @PostMapping("/{id}/evaluate")
    public Map<String, Object> evaluate(@PathVariable Long id, @RequestBody @Valid EvaluateRequest req,
                                        HttpServletRequest request) {
        User me = authContext.requireUser(request);
        proposalService.evaluate(me, id, req);
        return proposalService.detail(id);
    }
}
