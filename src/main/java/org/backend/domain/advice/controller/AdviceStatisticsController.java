package org.backend.domain.advice.controller;

import org.backend.common.CommonResponse;
import org.backend.domain.advice.dto.AdviceCategoryRatioResponse;
import org.backend.domain.advice.service.AdviceStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/advice")
public class AdviceStatisticsController {

    private final AdviceStatisticsService adviceStatisticsService;

    public AdviceStatisticsController(AdviceStatisticsService adviceStatisticsService) {
        this.adviceStatisticsService = adviceStatisticsService;
    }

    @GetMapping("/categories")
    public CommonResponse<AdviceCategoryRatioResponse> categoryRatios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AdviceCategoryRatioResponse data = adviceStatisticsService.getCategoryRatios(from, to);
        return CommonResponse.success(data, "상담 카테고리별 비중 조회 성공");
    }
}