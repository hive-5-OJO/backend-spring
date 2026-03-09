package org.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_memo", indexes = {
        @Index(name = "idx_member_memo_unique", columnList = "memberId", unique = true)
})
public class MemberMemo  { // 등록/수정일 공통 클래스 사용 권장

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 한 명의 회원당 하나만 존재하도록 설정
    private Long memberId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public MemberMemo(Long memberId, String content) {
        this.memberId = memberId;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}