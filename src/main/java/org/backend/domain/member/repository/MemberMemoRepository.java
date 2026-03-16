package org.backend.domain.member.repository;

import org.backend.domain.member.entity.MemberMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberMemoRepository extends JpaRepository<MemberMemo, Long> {
    Optional<MemberMemo> findByMemberId(Long memberId);
}