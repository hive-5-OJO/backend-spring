package org.backend.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.member.dto.MemberMemoResponse;
import org.backend.domain.member.dto.MemoRequest;
import org.backend.domain.member.service.MemberMemoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member-memos")
public class MemberMemoController {

    private final MemberMemoService memberMemoService;

    // 고객 id 별 메모 작성
    @PostMapping("/{memberId}")
    public CommonResponse<Long> saveMemo(@PathVariable Long memberId, @RequestBody MemoRequest request) {
        try {
            Long memoId = memberMemoService.saveOrUpdateMemo(memberId, request.getContent());
            return CommonResponse.success(memoId, "메모가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }
    // 고객 id 별 메모 조회
    @GetMapping("/{memberId}")
    public CommonResponse<MemberMemoResponse> getMemo(@PathVariable Long memberId) {
        try {
            MemberMemoResponse memo = memberMemoService.getMemo(memberId);
            return CommonResponse.success(memo, "메모 조회 성공");
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 고객 id별 메모 삭제
    @DeleteMapping("/{memberId}")
    public CommonResponse<Void> deleteMemo(@PathVariable Long memberId) {
        try {
            memberMemoService.deleteMemoByMemberId(memberId);
            return CommonResponse.success(null, "메모가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }
}