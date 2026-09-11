package pnh.dev.qs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import pnh.dev.qs.auth.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class QsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsApplication.class, args);
    }

}
