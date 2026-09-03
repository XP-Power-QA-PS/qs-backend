package pnh.dev.qs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pnh.dev.qs.auth.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class QsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsApplication.class, args);
    }

}
