package org.backend.domain.advice.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.dto.AdviceTimeStatResponse;
import org.backend.domain.advice.dto.AdviceTimelineResponse;
import org.backend.domain.advice.entity.Advice;
import org.backend.domain.advice.repository.AdviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 최적화
public class AdviceService {

    private final AdviceRepository adviceRepository;

    // 시간대별 통계 조회
    public List<AdviceTimeStatResponse> getAdviceTimeStats() {
        return adviceRepository.countAdviceByHour();
    }

    // 고객별 타임라인 조회
    public List<AdviceTimelineResponse> getMemberTimeline(Long memberId) {
        List<Advice> advices = adviceRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return advices.stream()
                .map(AdviceTimelineResponse::from)
                .collect(Collectors.toList());
    }
}