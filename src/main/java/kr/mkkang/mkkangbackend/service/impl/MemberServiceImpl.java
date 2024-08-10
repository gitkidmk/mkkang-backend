package kr.mkkang.mkkangbackend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.domain.MemberRole;
import kr.mkkang.mkkangbackend.domain.repository.MemberRepository;
import kr.mkkang.mkkangbackend.redis.ChangeRoleRequestDto;
import kr.mkkang.mkkangbackend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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

    @Override
    public Member postMember(ChangeRoleRequestDto requestDto) {
        log.debug("requestDto: {}", requestDto);

        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(EntityNotFoundException::new);
        MemberRole role = MemberRole.valueOf(requestDto.getRole());
        member.setRole(role);

        return memberRepository.save(member);
    }
}
