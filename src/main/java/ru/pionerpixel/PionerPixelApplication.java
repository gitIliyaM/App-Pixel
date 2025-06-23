package ru.pionerpixel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PionerPixelApplication {

    public static void main(String[] args) {
        SpringApplication.run(PionerPixelApplication.class, args);
    }

}
