package com.campus.proposal.repository;

import com.campus.proposal.common.ProposalStatus;
import com.campus.proposal.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findAllByOrderByCreatedAtDesc();

    List<Proposal> findByStatusOrderByCreatedAtDesc(ProposalStatus status);

    List<Proposal> findByProposerIdOrderByCreatedAtDesc(Long proposerId);

    List<Proposal> findByProposerIdAndStatusOrderByCreatedAtDesc(Long proposerId, ProposalStatus status);

    long countByStatus(ProposalStatus status);
}
