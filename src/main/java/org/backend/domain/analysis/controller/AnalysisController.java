package org.backend.domain.analysis.controller;

import lombok.RequiredArgsConstructor;
import org.backend.domain.analysis.dto.AnalysisSummaryResponseDto;
import org.backend.domain.analysis.dto.LtvResponseDto;
import org.backend.domain.analysis.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    // 특정 고객 LTV 조회
    @GetMapping("/ltv/{memberId}")
    public ResponseEntity<LtvResponseDto> getLtv(@PathVariable Long memberId){
        return ResponseEntity.ok(analysisService.getLtvDetail(memberId));
    }

    // 고객 통합 분석 요약
    @GetMapping("/summary/{memberId}")
    public ResponseEntity<AnalysisSummaryResponseDto> getAnalysisSummary(@PathVariable Long memberId){
        return ResponseEntity.ok(analysisService.getAnalysisSummary(memberId));
    }

    // 대시보드
//    @GetMapping("/dashboard")
}
