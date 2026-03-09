package org.backend.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.member.dto.MemoRequest;
import org.backend.domain.member.entity.MemberMemo;
import org.backend.domain.member.service.MemberMemoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member-memos")
public class MemberMemoController {

    private final MemberMemoService memberMemoService;

    // 등록 및 수정
    @PostMapping("/{memberId}")
    public CommonResponse<Long> saveMemo(@PathVariable Long memberId, @RequestBody MemoRequest request) {
        try {
            Long memoId = memberMemoService.saveOrUpdateMemo(memberId, request.getContent());
            return CommonResponse.success(memoId, "메모가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 메모 단일 조회
    @GetMapping("/{memberId}")
    public CommonResponse<MemberMemo> getMemo(@PathVariable Long memberId) {
        try {
            MemberMemo memo = memberMemoService.getMemo(memberId);
            return CommonResponse.success(memo, "메모 조회 성공");
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 메모 삭제
    @DeleteMapping("/{memberId}")
    public CommonResponse<Void> deleteMemo(@PathVariable Long memberId) {
        try {
            memberMemoService.deleteMemoByMemberId(memberId);
            return CommonResponse.success(null, "메모가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            // 삭제 대상이 없거나 회원이 없을 경우 FAIL 응답 반환
            return CommonResponse.fail(e.getMessage());
        }
    }
}