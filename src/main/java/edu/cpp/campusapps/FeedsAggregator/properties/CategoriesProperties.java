package edu.cpp.campusapps.FeedsAggregator.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@ConfigurationProperties
public class CategoriesProperties {

    private final HashMap<String, Category> categories = new HashMap<>();

    public HashMap<String, Category> getCategories() {
        return categories;
    }
}
