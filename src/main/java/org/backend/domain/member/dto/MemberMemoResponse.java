package org.backend.domain.member.dto;

import lombok.Getter;
import org.backend.domain.member.entity.MemberMemo;

@Getter
public class MemberMemoResponse {

    private final Long id;
    private final Long adminId;
    private final Long memberId;
    private final String content;

    private MemberMemoResponse(Long id, Long adminId, Long memberId, String content) {
        this.id = id;
        this.adminId = adminId;
        this.memberId = memberId;
        this.content = content;
    }

    public static MemberMemoResponse from(MemberMemo memo) {
        return new MemberMemoResponse(
                memo.getId(),
                memo.getAdminId(),
                memo.getMemberId(),
                memo.getContent()
        );
    }
}