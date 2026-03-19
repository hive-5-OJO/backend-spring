package org.backend.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.entity.QAdvice;
import org.backend.domain.advice.entity.QCategories;
import org.backend.domain.analysis.entity.QAnalysis;
import org.backend.domain.analysis.entity.QRfm;
import org.backend.domain.member.dto.CustomerFilterRequest;
import org.backend.domain.member.dto.CustomerFilterResponse;
import org.backend.domain.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CustomerFilterResponse> findFilteredCustomers(CustomerFilterRequest request, Pageable pageable) {
        QMember member = QMember.member;
        QAnalysis analysis = QAnalysis.analysis;
        QRfm rfm = QRfm.rfm;
        QAdvice advice = QAdvice.advice;

        List<CustomerFilterResponse> content = queryFactory
                .select(Projections.constructor(CustomerFilterResponse.class,
                        member.id,
                        member.name,
                        member.email,
                        member.phone,
                        Expressions.nullExpression(String.class),
                        Expressions.nullExpression(String.class),
                        member.createdAt,
                        com.querydsl.core.types.ExpressionUtils.as(
                                JPAExpressions.select(advice.id.count())
                                        .from(advice)
                                        .where(advice.member.eq(member)),
                                "frequency"),
                        analysis.type))
                .from(member)
                .leftJoin(analysis).on(analysis.member.eq(member))
                .leftJoin(rfm).on(rfm.member.eq(member))
                .where(
                        segmentIn(analysis, request.getSegments()),
                        frequencyIn(advice, member, request.getFrequencies()),
                        categoryIn(advice, member, request.getCategoryIds())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalValue = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(analysis).on(analysis.member.eq(member))
                .leftJoin(rfm).on(rfm.member.eq(member))
                .where(
                        segmentIn(analysis, request.getSegments()),
                        frequencyIn(advice, member, request.getFrequencies()),
                        categoryIn(advice, member, request.getCategoryIds())
                )
                .fetchOne();

        long total = totalValue != null ? totalValue : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    // 세그먼트 다중 IN 조건
    private BooleanExpression segmentIn(QAnalysis analysis, List<String> segments) {
        if (CollectionUtils.isEmpty(segments)) return null;
        return analysis.type.in(segments);
    }

    // 상담 빈도 다중 OR 조건
    private BooleanExpression frequencyIn(QAdvice advice, QMember member, List<String> frequencies) {
        if (CollectionUtils.isEmpty(frequencies)) return null;

        var countQuery = JPAExpressions.select(advice.id.count())
                .from(advice)
                .where(advice.member.eq(member));

        List<BooleanExpression> conditions = new ArrayList<>();
        for (String frequency : frequencies) {
            switch (frequency.toUpperCase()) {
                case "LOW"    -> conditions.add(Expressions.asNumber(countQuery).loe(2L));
                case "MEDIUM" -> conditions.add(Expressions.asNumber(countQuery).between(3L, 5L));
                case "HIGH"   -> conditions.add(Expressions.asNumber(countQuery).goe(6L));
            }
        }

        if (conditions.isEmpty()) return null;

        // 여러 빈도 조건을 OR로 연결
        return conditions.stream()
                .reduce(BooleanExpression::or)
                .orElse(null);
    }

    // 카테고리 다중 IN 조건
    private BooleanExpression categoryIn(QAdvice advice, QMember member, List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) return null;

        QCategories category = QCategories.categories;
        return member.id.in(
                JPAExpressions
                        .select(advice.member.id)
                        .from(advice)
                        .join(advice.category, category)
                        .where(
                                category.id.in(categoryIds)
                                        .or(category.parentId.in(categoryIds))
                        )
        );
    }
}