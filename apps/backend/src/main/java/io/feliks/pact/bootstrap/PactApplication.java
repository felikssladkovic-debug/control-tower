package io.feliks.pact.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.feliks.pact")
public class PactApplication {

    public static void main(String[] args) {
        SpringApplication.run(PactApplication.class, args);
    }
}
