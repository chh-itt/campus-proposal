package com.campus.proposal.common;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * 提案状态机。
 *
 * 主流程:SUBMITTED → ACCEPTED → ASSIGNED → REPLIED → CLOSED
 * 分支:SUBMITTED → REJECTED(不予立案,终态)
 *
 * 设计要点:流转规则集中定义在这一个枚举里,任何状态变更都必须先经过
 * canTransitionTo 校验,保证"没有接口能绕过流程直接改状态"。
 */
public enum ProposalStatus {
    SUBMITTED("已提交·待审查"),
    ACCEPTED("已立案·待分办"),
    ASSIGNED("办理中"),
    REPLIED("已答复·待评价"),
    CLOSED("已办结"),
    REJECTED("不予立案");

    private final String label;

    ProposalStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** 允许的状态流转表:键 = 当前状态,值 = 允许到达的下一状态集合 */
    private static final Map<ProposalStatus, Set<ProposalStatus>> TRANSITIONS =
            new EnumMap<>(Map.of(
                    SUBMITTED, Set.of(ACCEPTED, REJECTED),
                    ACCEPTED, Set.of(ASSIGNED),
                    ASSIGNED, Set.of(REPLIED),
                    REPLIED, Set.of(CLOSED),
                    CLOSED, Set.of(),      // 终态
                    REJECTED, Set.of()     // 终态
            ));

    public boolean canTransitionTo(ProposalStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }
}
