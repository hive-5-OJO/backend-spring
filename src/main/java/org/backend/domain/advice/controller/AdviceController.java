package org.backend.domain.advice.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.advice.dto.AdviceTimeStatResponse;
import org.backend.domain.advice.dto.AdviceTimelineResponse;
import org.backend.domain.advice.service.AdviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/advice")
public class AdviceController {

    private final AdviceService adviceService;

    // 상담 시간대별 통계
    @GetMapping("/time")
    public CommonResponse<List<AdviceTimeStatResponse>> getAdviceTimeStats() {
        try {
            List<AdviceTimeStatResponse> stats = adviceService.getAdviceTimeStats();
            return CommonResponse.success(stats, "시간대별 통계 조회 성공");
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    // 고객별 상담 타임라인
    @GetMapping("/timeline/{memberId}")
    public CommonResponse<List<AdviceTimelineResponse>> getAdviceTimeline(@PathVariable("memberId") Long memberId) {
        try {
            List<AdviceTimelineResponse> timeline = adviceService.getMemberTimeline(memberId);
            return CommonResponse.success(timeline, "고객 타임라인 조회 성공");
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}