package kr.mkkang.mkkangbackend;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Slf4j
@EnableJpaRepositories(
        basePackages = "kr.mkkang.mkkangbackend",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "kr\\.mkkang\\.mkkangbackend\\.redis\\..*"))
@EnableRedisRepositories(
        includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "kr\\.mkkang\\.mkkangbackend\\.redis\\..*"))
@SpringBootApplication
public class MkkangBackendApplication {

    @Value("${DB_URL}")
    private String dbUrl;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${REDIS_HOST}")
    private String redisHost;

    public static void main(String[] args) {
        SpringApplication.run(MkkangBackendApplication.class, args);
    }

    @PostConstruct
    public void variables() {
        log.info("dbUrl: {}", dbUrl);
        log.info("clientId: {}", clientId);
        log.info("REDIS_HOST: {}", redisHost);
    }

}
