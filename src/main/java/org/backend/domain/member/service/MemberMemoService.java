package org.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.member.dto.MemberMemoResponse;
import org.backend.domain.member.entity.MemberMemo;
import org.backend.domain.member.repository.MemberMemoRepository;
import org.backend.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberMemoService {

    private final MemberMemoRepository memberMemoRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public Long saveOrUpdateMemo(Long memberId, String content) {
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId);
        }

        return memberMemoRepository.findByMemberId(memberId)
                .map(memo -> {
                    memo.updateContent(content);
                    return memo.getId();
                })
                .orElseGet(() -> {
                    MemberMemo newMemo = MemberMemo.builder()
                            .memberId(memberId)
                            .content(content)
                            .build();
                    return memberMemoRepository.save(newMemo).getId();
                });
    }


    public MemberMemoResponse getMemo(Long memberId) {
        MemberMemo memo = memberMemoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 메모가 없습니다."));
        return MemberMemoResponse.from(memo);
    }


    @Transactional
    public void deleteMemoByMemberId(Long memberId) {
        MemberMemo memo = memberMemoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 메모가 존재하지 않습니다."));
        memberMemoRepository.delete(memo);
    }
}