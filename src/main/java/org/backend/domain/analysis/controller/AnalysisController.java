package org.backend.domain.analysis.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.analysis.dto.AnalysisSummaryResponseDto;
import org.backend.domain.analysis.dto.LtvResponseDto;
import org.backend.domain.analysis.dto.RfmKpiResponseDto;
import org.backend.domain.analysis.dto.RfmResponseDto;
import org.backend.domain.analysis.service.AnalysisService;
import org.backend.domain.analysis.service.KpiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;
    private final KpiService kpiService;

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

    // rfm 조회
    @GetMapping("/rfm/{memberId}")
    public ResponseEntity<CommonResponse<RfmResponseDto>> getRfm(@PathVariable Long memberId){
        RfmResponseDto data = analysisService.getRfm(memberId);
        return ResponseEntity.ok(CommonResponse.success(data, null));
    }

    // kpi 도출
    @GetMapping("/rfmkpi")
    public ResponseEntity<CommonResponse<RfmKpiResponseDto>> getKpi(@RequestParam(name = "baseMonth") String baseMonth){
        RfmKpiResponseDto data = kpiService.getKpi(baseMonth);
        return ResponseEntity.ok(CommonResponse.success(data, "kpi 조회 성공"));
    }
}
