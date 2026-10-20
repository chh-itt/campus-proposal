package com.campus.proposal.service;

import com.campus.proposal.common.ProposalStatus;
import com.campus.proposal.common.Role;
import com.campus.proposal.dto.AssignRequest;
import com.campus.proposal.dto.EvaluateRequest;
import com.campus.proposal.dto.ProposalCreateRequest;
import com.campus.proposal.dto.ReplyRequest;
import com.campus.proposal.dto.ReviewRequest;
import com.campus.proposal.entity.Proposal;
import com.campus.proposal.entity.ProposalLog;
import com.campus.proposal.entity.User;
import com.campus.proposal.exception.BizException;
import com.campus.proposal.repository.ProposalLogRepository;
import com.campus.proposal.repository.ProposalRepository;
import com.campus.proposal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalLogRepository logRepository;
    private final UserRepository userRepository;

    public ProposalService(ProposalRepository proposalRepository, ProposalLogRepository logRepository,
                           UserRepository userRepository) {
        this.proposalRepository = proposalRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    // ---------- 查询 ----------

    /** 角色感知的列表:提案人只能看到自己的提案,工作人员和承办部门可见全部 */
    public List<Proposal> listFor(User me, ProposalStatus status) {
        boolean proposerOnly = me.getRole() == Role.PROPOSER;
        if (proposerOnly && status != null) {
            return proposalRepository.findByProposerIdAndStatusOrderByCreatedAtDesc(me.getId(), status);
        }
        if (proposerOnly) {
            return proposalRepository.findByProposerIdOrderByCreatedAtDesc(me.getId());
        }
        if (status != null) {
            return proposalRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return proposalRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 详情 + 办理留痕 */
    public Map<String, Object> detail(Long id) {
        Proposal p = getOrThrow(id);
        List<ProposalLog> logs = logRepository.findByProposalIdOrderByCreatedAtAsc(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("proposal", p);
        result.put("logs", logs);
        return result;
    }

    // ---------- 生命周期动作 ----------

    @Transactional
    public Proposal create(User proposer, ProposalCreateRequest req) {
        if (proposer.getRole() != Role.PROPOSER) {
            throw new BizException(403, "仅学生提案人可提交提案");
        }
        Proposal p = new Proposal();
        p.setCode(nextCode());
        p.setTitle(req.title().trim());
        p.setContent(req.content().trim());
        p.setProposer(proposer);
        p.setStatus(ProposalStatus.SUBMITTED);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        p = proposalRepository.save(p);
        addLog(p, "提交", proposer, null, LocalDateTime.now());
        return p;
    }

    /** 审查:立案 / 不予立案 */
    @Transactional
    public Proposal review(User staff, Long id, ReviewRequest req) {
        if (staff.getRole() != Role.STAFF) {
            throw new BizException(403, "仅提案工作办公室可执行审查操作");
        }
        Proposal p = getOrThrow(id);
        ProposalStatus target = Boolean.TRUE.equals(req.accept()) ? ProposalStatus.ACCEPTED : ProposalStatus.REJECTED;
        transition(p, target, Boolean.TRUE.equals(req.accept()) ? "立案" : "退回", staff, req.note(),
                LocalDateTime.now());
        return p;
    }

    /** 分办:立案后指定承办部门经办人 */
    @Transactional
    public Proposal assign(User staff, Long id, AssignRequest req) {
        if (staff.getRole() != Role.STAFF) {
            throw new BizException(403, "仅提案工作办公室可执行分办操作");
        }
        Proposal p = getOrThrow(id);
        User dept = userRepository.findById(req.deptUserId())
                .orElseThrow(() -> new BizException(400, "承办部门账号不存在"));
        if (dept.getRole() != Role.DEPT) {
            throw new BizException(400, "目标账号不是承办部门");
        }
        p.setHandler(dept);
        transition(p, ProposalStatus.ASSIGNED, "分办", staff, dept.getName() + "(" + dept.getDepartment() + ")",
                LocalDateTime.now());
        return p;
    }

    /** 答复:只有被分办的承办部门本人可以答复 */
    @Transactional
    public Proposal reply(User operator, Long id, ReplyRequest req) {
        Proposal p = getOrThrow(id);
        if (p.getHandler() == null || !Objects.equals(p.getHandler().getId(), operator.getId())) {
            throw new BizException(403, "只有该提案的承办部门可以答复");
        }
        p.setReply(req.reply().trim());
        transition(p, ProposalStatus.REPLIED, "答复", operator, null, LocalDateTime.now());
        return p;
    }

    /** 评价办结:只有提案人本人可以评价 */
    @Transactional
    public Proposal evaluate(User operator, Long id, EvaluateRequest req) {
        Proposal p = getOrThrow(id);
        if (!Objects.equals(p.getProposer().getId(), operator.getId())) {
            throw new BizException(403, "只有提案人本人可以评价");
        }
        p.setRating(req.rating());
        p.setEvaluation(req.comment());
        transition(p, ProposalStatus.CLOSED, "评价", operator, "满意度 " + req.rating() + " 星", LocalDateTime.now());
        return p;
    }

    // ---------- 内部工具 ----------

    /**
     * 所有状态变更的唯一入口:先校验状态机,再改状态、写留痕。
     * 任何接口都不得绕过这里直接 setStatus —— 这是流程完整性的关键防线。
     */
    private void transition(Proposal p, ProposalStatus target, String action, User operator, String note,
                            LocalDateTime time) {
        ProposalStatus from = p.getStatus();
        if (!from.canTransitionTo(target)) {
            throw new BizException(400, "状态不允许流转: " + from.label() + " → " + target.label());
        }
        p.setStatus(target);
        p.setUpdatedAt(time);
        addLog(p, action, operator, note, time);
    }

    private Proposal getOrThrow(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "提案不存在"));
    }

    private void addLog(Proposal p, String action, User operator, String note, LocalDateTime time) {
        ProposalLog l = new ProposalLog();
        l.setProposal(p);
        l.setAction(action);
        l.setOperator(operator);
        l.setNote(note);
        l.setCreatedAt(time);
        logRepository.save(l);
    }

    /**
     * 生成提案编号 TA-2026-0001。
     * TODO(留给你自己动手改):当前按"已有数量+1"生成,并发下两个提案可能撞号。
     * 改进方向:数据库序列(如 Hibernate @SequenceGenerator)或带前缀的独立序号表。
     * —— 这是一个可以在面试里主动讲的"已知简化 + 改进方案"。
     */
    private String nextCode() {
        int n = (int) proposalRepository.count() + 1;
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        return "TA-" + year + "-" + String.format("%04d", n);
    }
}
