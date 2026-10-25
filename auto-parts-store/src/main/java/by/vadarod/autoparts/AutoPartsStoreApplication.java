package by.vadarod.autoparts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AutoPartsStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoPartsStoreApplication.class, args);
    }
}
