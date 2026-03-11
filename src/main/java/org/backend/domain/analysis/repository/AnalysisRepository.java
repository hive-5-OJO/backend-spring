package org.backend.domain.analysis.repository;

import org.backend.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findByMemberId(Long memberId);

    @Query("SELECT a FROM Analysis a JOIN FETCH a.member")
    List<Analysis> findAllWithMember();
}
