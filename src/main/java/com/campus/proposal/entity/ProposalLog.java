package com.campus.proposal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 办理留痕:提案每一次流转(提交/立案/退回/分办/答复/评价)都追加一条记录,
 * 只增不改 —— 这张表是"谁在什么时候对这个提案做了什么"的唯一事实来源。
 */
@Entity
@Table(name = "proposal_logs")
public class ProposalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "proposal_id")
    private Proposal proposal;

    /** 动作:提交 / 立案 / 退回 / 分办 / 答复 / 评价 */
    @Column(nullable = false, length = 20)
    private String action;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Proposal getProposal() { return proposal; }
    public void setProposal(Proposal proposal) { this.proposal = proposal; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
