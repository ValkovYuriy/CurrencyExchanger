package yuriy.dev.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("yuriy.dev")
@EntityScan("yuriy.dev.model")
@EnableJpaRepositories("yuriy.dev.repository")
public class AuthLibraryAutoConfiguration {
}
