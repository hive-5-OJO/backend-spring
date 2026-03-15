// PathVariable 방식
// package org.backend.domain.member.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.backend.common.CommonResponse;
//import org.backend.domain.member.dto.MemberMemoResponse;
//import org.backend.domain.member.dto.MemoRequest;
//import org.backend.domain.member.service.MemberMemoService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/member-memos")
//public class MemberMemoController {
//
//    private final MemberMemoService memberMemoService;
//
//
//    // 상담사별 고객 메모 저장/수정
//    @PostMapping("/{adminId}/{memberId}")
//    public CommonResponse<Long> saveMemo(
//            @PathVariable Long adminId,
//            @PathVariable Long memberId,
//            @RequestBody MemoRequest request) {
//        try {
//            Long memoId = memberMemoService.saveOrUpdateMemo(adminId, memberId, request.getContent());
//            return CommonResponse.success(memoId, "메모가 저장되었습니다.");
//        } catch (IllegalArgumentException e) {
//            return CommonResponse.fail(e.getMessage());
//        }
//    }
//
//
//     // 상담사별 고객 메모 조회
//    @GetMapping("/{adminId}/{memberId}")
//    public CommonResponse<MemberMemoResponse> getMemo(
//            @PathVariable Long adminId,
//            @PathVariable Long memberId) {
//        try {
//            MemberMemoResponse memo = memberMemoService.getMemo(adminId, memberId);
//            return CommonResponse.success(memo, "메모 조회 성공");
//        } catch (IllegalArgumentException e) {
//            return CommonResponse.fail(e.getMessage());
//        }
//    }
//

//     // 상담사별 고객 메모 삭제
//    @DeleteMapping("/{adminId}/{memberId}")
//    public CommonResponse<Void> deleteMemo(
//            @PathVariable Long adminId,
//            @PathVariable Long memberId) {
//        try {
//            memberMemoService.deleteMemo(adminId, memberId);
//            return CommonResponse.success(null, "메모가 삭제되었습니다.");
//        } catch (IllegalArgumentException e) {
//            return CommonResponse.fail(e.getMessage());
//        }
//    }
//}



// JWT  방식
package org.backend.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.member.dto.MemberMemoResponse;
import org.backend.domain.member.dto.MemoRequest;
import org.backend.domain.member.service.MemberMemoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member-memos")
public class MemberMemoController {

    private final MemberMemoService memberMemoService;

    // JWT 토큰에서 adminId 추출하는 공통 메서드
    // JwtAuthenticationFilter에서 principal을 adminId(String)으로 저장하므로 파싱해서 사용
    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }
        return Long.parseLong((String) authentication.getPrincipal());
    }

    // 상담사별 고객 메모 저장/수정 (JWT에서 adminId 자동 추출)
    @PostMapping("/{memberId}")
    public CommonResponse<Long> saveMemo(
            @PathVariable Long memberId,
            @RequestBody MemoRequest request) {
        try {
            Long adminId = getCurrentAdminId();
            Long memoId = memberMemoService.saveOrUpdateMemo(adminId, memberId, request.getContent());
            return CommonResponse.success(memoId, "메모가 저장되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 상담사별 고객 메모 조회 (JWT에서 adminId 자동 추출)
    @GetMapping("/{memberId}")
    public CommonResponse<MemberMemoResponse> getMemo(@PathVariable Long memberId) {
        try {
            Long adminId = getCurrentAdminId();
            MemberMemoResponse memo = memberMemoService.getMemo(adminId, memberId);
            return CommonResponse.success(memo, "메모 조회 성공");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 상담사별 고객 메모 삭제 (JWT에서 adminId 자동 추출)
    @DeleteMapping("/{memberId}")
    public CommonResponse<Void> deleteMemo(@PathVariable Long memberId) {
        try {
            Long adminId = getCurrentAdminId();
            memberMemoService.deleteMemo(adminId, memberId);
            return CommonResponse.success(null, "메모가 삭제되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }
}