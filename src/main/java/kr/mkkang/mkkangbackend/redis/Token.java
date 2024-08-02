package kr.mkkang.mkkangbackend.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class Token {

    @Id
    private String accessToken;

    private String refreshToken;
}
