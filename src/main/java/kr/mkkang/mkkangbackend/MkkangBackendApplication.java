package kr.mkkang.mkkangbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableJpaRepositories(
        basePackages = "kr.mkkang.mkkangbackend",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "kr\\.mkkang\\.mkkangbackend\\.redis\\..*"))
@EnableRedisRepositories(
        includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "kr\\.mkkang\\.mkkangbackend\\.redis\\..*"))
@SpringBootApplication
public class MkkangBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MkkangBackendApplication.class, args);
    }

}
