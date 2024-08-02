package kr.mkkang.mkkangbackend.controller;

import kr.mkkang.mkkangbackend.redis.Token;
import kr.mkkang.mkkangbackend.redis.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    @Autowired
    TokenRepository tokenRepository;

    @GetMapping("/")
    public void test() {
        Token saved = tokenRepository.save(new Token("abc", "cdf"));
        log.info(saved.toString());
    }

    @GetMapping("/happy")
    public void happy() {
        log.info("happy");
    }

    @GetMapping("/admin")
    public void admin() {
        log.info("admin");
    }

    @GetMapping("/family")
    public void family() {
        log.info("family");
    }
}
