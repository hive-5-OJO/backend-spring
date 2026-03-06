package org.backend.domain.advice.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.dto.HourlyConsultationDto;
import org.backend.domain.advice.repository.AdviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdviceStatisticsService {

    private final AdviceRepository adviceRepository;

    public List<HourlyConsultationDto> getHourlyStatistics() {
        return adviceRepository.getHourlyStatistics();
    }
}
