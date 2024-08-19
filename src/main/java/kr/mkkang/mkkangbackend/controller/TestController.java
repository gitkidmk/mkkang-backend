package kr.mkkang.mkkangbackend.controller;

import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.redis.ChangeRoleRequestDto;
import kr.mkkang.mkkangbackend.redis.Token;
import kr.mkkang.mkkangbackend.redis.TokenRepository;
import kr.mkkang.mkkangbackend.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class TestController {
    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    MemberService memberService;

    @GetMapping("/login/tmp")
    public void test() {
        Token saved = tokenRepository.save(new Token("abc", "cdf"));
        log.info(saved.toString());
    }

    @GetMapping("/admin")
    public List<Member> admin() {
        return memberService.getAllMembers();
    }

    // TODO: RequestBody 통해 수정할 내역 받아오기
    @PostMapping("/admin/role")
    public Member changeRole(@RequestBody ChangeRoleRequestDto requestDto) {
        return memberService.postMember(requestDto);
    }

    @GetMapping("/family")
    public List<Member> family() {
        return memberService.getAllMembers();
    }

    @GetMapping("/any")
    public void any() {
        log.info("any");
    }

}
