package org.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_memo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_memo_admin_member", columnNames = {"admin_id", "member_id"})
})
public class MemberMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;   // 메모를 작성한 상담사 ID (admin 테이블 FK)

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // 메모 대상 고객 ID

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public MemberMemo(Long adminId, Long memberId, String content) {
        this.adminId = adminId;
        this.memberId = memberId;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}