package org.backend.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.backend.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfm")
@Data
public class Rfm {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDateTime recency;
    private Integer frequency;
    private Long monetary;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
