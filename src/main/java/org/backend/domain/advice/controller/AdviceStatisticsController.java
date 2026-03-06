package org.backend.domain.advice.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.advice.dto.HourlyConsultationDto;
import org.backend.domain.advice.service.AdviceStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/advice")
public class AdviceStatisticsController {

    private final AdviceStatisticsService adviceStatisticsService;

    @GetMapping("/time")
    public CommonResponse<List<HourlyConsultationDto>> getHourlyStatistics() {
        try {
            List<HourlyConsultationDto> stats = adviceStatisticsService.getHourlyStatistics();
            return CommonResponse.success(stats, null);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
