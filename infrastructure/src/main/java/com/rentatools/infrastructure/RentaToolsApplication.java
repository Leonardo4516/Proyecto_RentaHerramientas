package com.rentatools.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.rentatools"})
public class RentaToolsApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(RentaToolsApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RentaToolsApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(RentaToolsApplication.class, args);
    }
}
