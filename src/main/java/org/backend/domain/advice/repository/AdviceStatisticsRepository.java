package org.backend.domain.advice.repository;

import org.backend.domain.advice.dto.AdviceCategoryRatioItem;
import org.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdviceStatisticsRepository extends JpaRepository<Advice, Long> {

    @Query("""
        select new org.backend.domain.advice.dto.AdviceCategoryRatioItem(
            c.id,
            c.categoryName,
            count(a),
            0.0
        )
        from Advice a
        join a.category c
        where (:from is null or a.createdAt >= :from)
          and (:toExclusive is null or a.createdAt < :toExclusive)
        group by c.id, c.categoryName
        order by count(a) desc
    """)
    List<AdviceCategoryRatioItem> countByCategory(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    @Query("""
        select count(a)
        from Advice a
        where (:from is null or a.createdAt >= :from)
          and (:toExclusive is null or a.createdAt < :toExclusive)
    """)
    long totalCount(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}