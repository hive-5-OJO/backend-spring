package org.backend.domain.analysis.repository;

import org.backend.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findByMemberId(Long memberId);

    // 대시보드용 - 전체 고객 ltv, 등급별 고객수 등
}
