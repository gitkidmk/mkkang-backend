package kr.mkkang.mkkangbackend.service.impl;

import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.domain.repository.MemberRepository;
import kr.mkkang.mkkangbackend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Override
    public List<Member> getAllMembers() {
        // crudRepo를 상속받으면 iterable 반환
        // jpaRepo를 상속받으면 list 반환
        return memberRepository.findAll();
    }
}
