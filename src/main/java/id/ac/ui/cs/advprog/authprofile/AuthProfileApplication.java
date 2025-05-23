package id.ac.ui.cs.advprog.authprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class AuthProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthProfileApplication.class, args);
    }
}
