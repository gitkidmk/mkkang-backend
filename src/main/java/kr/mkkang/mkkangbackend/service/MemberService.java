package kr.mkkang.mkkangbackend.service;

import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.redis.ChangeRoleRequestDto;

import java.util.List;

public interface MemberService {
    List<Member> getAllMembers();
    Member postMember(ChangeRoleRequestDto requestDto);
}
