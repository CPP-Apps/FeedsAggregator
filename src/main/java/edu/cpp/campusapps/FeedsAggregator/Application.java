package edu.cpp.campusapps.FeedsAggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({
        @PropertySource(value = "classpath:feeds-aggregator.properties"),
        @PropertySource(value = "file:${portal.home}/global.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${portal.home}/feeds-aggregator.properties", ignoreResourceNotFound = true),
})
@EnableCaching
public class Application extends SpringBootServletInitializer {

    private static Class<Application> applicationClass = Application.class;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }
}
