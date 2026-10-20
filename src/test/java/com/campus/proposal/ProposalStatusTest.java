package com.campus.proposal;

import com.campus.proposal.common.ProposalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 状态机规则测试:这是整个系统流程正确性的根基。
 * 合法流转:SUBMITTED→ACCEPTED→ASSIGNED→REPLIED→CLOSED,以及 SUBMITTED→REJECTED。
 */
class ProposalStatusTest {

    @Test
    void submittedCanOnlyBeAcceptedOrRejected() {
        assertTrue(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.ACCEPTED));
        assertTrue(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.REJECTED));
        assertFalse(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.ASSIGNED));
        assertFalse(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.REPLIED));
        assertFalse(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.CLOSED));
    }

    @Test
    void happyPathIsFullyTraversable() {
        assertTrue(ProposalStatus.SUBMITTED.canTransitionTo(ProposalStatus.ACCEPTED));
        assertTrue(ProposalStatus.ACCEPTED.canTransitionTo(ProposalStatus.ASSIGNED));
        assertTrue(ProposalStatus.ASSIGNED.canTransitionTo(ProposalStatus.REPLIED));
        assertTrue(ProposalStatus.REPLIED.canTransitionTo(ProposalStatus.CLOSED));
    }

    @Test
    void terminalStatesCannotLeave() {
        for (ProposalStatus target : ProposalStatus.values()) {
            assertFalse(ProposalStatus.CLOSED.canTransitionTo(target), "CLOSED 应为终态");
            assertFalse(ProposalStatus.REJECTED.canTransitionTo(target), "REJECTED 应为终态");
        }
    }

    @Test
    void cannotSkipSteps() {
        assertFalse(ProposalStatus.ACCEPTED.canTransitionTo(ProposalStatus.CLOSED), "未分办答复不得直接办结");
        assertFalse(ProposalStatus.ASSIGNED.canTransitionTo(ProposalStatus.CLOSED), "未答复不得直接办结");
        assertFalse(ProposalStatus.ACCEPTED.canTransitionTo(ProposalStatus.REJECTED), "立案后不得改为退回");
    }
}
