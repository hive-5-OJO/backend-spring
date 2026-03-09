//package org.backend.domain.member.service;
//
//import lombok.RequiredArgsConstructor;
//import org.backend.domain.member.entity.MemberMemo;
//import org.backend.domain.member.repository.MemberMemoRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class MemberMemoService {
//
//    private final MemberMemoRepository memberMemoRepository;
//
//    @Transactional
//    public Long saveOrUpdateMemo(Long memberId, String content) {
//        // 이미 메모가 있는지 확인
//        return memberMemoRepository.findByMemberId(memberId)
//                .map(memo -> {
//                    memo.updateContent(content); // 있으면 수정
//                    return memo.getId();
//                })
//                .orElseGet(() -> {
//                    MemberMemo newMemo = MemberMemo.builder()
//                            .memberId(memberId)
//                            .content(content)
//                            .build();
//                    return memberMemoRepository.save(newMemo).getId(); // 없으면 신규 생성
//                });
//    }
//
//    public MemberMemo getMemo(Long memberId) {
//        return memberMemoRepository.findByMemberId(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("등록된 메모가 없습니다."));
//    }
//
//    @Transactional
//    public void deleteMemo(Long memoId) {
//        memberMemoRepository.deleteById(memoId);
//    }
//}
//
//
//
//
//





package org.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
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
    private final MemberRepository memberRepository; // 회원 확인을 위해 주입

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

    public MemberMemo getMemo(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId);
        }

        return memberMemoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 메모가 없습니다."));
    }

    @Transactional
    public void deleteMemoByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId);
        }

        MemberMemo memo = memberMemoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 메모가 존재하지 않습니다."));

        memberMemoRepository.delete(memo);
    }
}





