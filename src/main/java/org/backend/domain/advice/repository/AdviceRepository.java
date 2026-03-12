package org.backend.domain.advice.repository;

import org.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AdviceRepository extends JpaRepository<Advice, Long>, AdviceRepositoryCustom {

    @Query("SELECT a FROM Advice a LEFT JOIN FETCH a.category WHERE a.member.id = :memberId")
    Set<Advice> findWithCategoryByMemberId(@Param("memberId") Long memberId);
}
