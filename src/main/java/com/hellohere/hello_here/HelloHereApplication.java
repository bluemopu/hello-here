package com.hellohere.hello_here;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class HelloHereApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloHereApplication.class, args);
    }
}
