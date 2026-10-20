package com.campus.proposal.repository;

import com.campus.proposal.entity.ProposalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalLogRepository extends JpaRepository<ProposalLog, Long> {

    List<ProposalLog> findByProposalIdOrderByCreatedAtAsc(Long proposalId);
}
