package edu.cpp.campusapps.FeedsAggregator.service;

import edu.cpp.campusapps.FeedsAggregator.model.CacheControllerV0Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;

@Service
public class CacheService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheManager cacheManager;

    public boolean evictFeed(HttpServletRequest request) {
        String oidc = request.getHeader(HttpHeaders.AUTHORIZATION);

        return false;
    }

    public boolean evictFeed(String feedUrl) {
        Cache<String, Object> cache = cacheManager.getCache("feeds");

        CacheControllerV0Response response = new CacheControllerV0Response();

        if (!cache.containsKey(feedUrl)) {
            return false;
        }

        cache.remove(feedUrl);

        if (cache.containsKey((feedUrl))) {
            logger.error(String.format("Failed to evict a feed from the cache. Feed URL: %s", feedUrl));

            return false;
        }

        logger.info(String.format("Removed feed from the cache. Feed URL: %s", feedUrl));

        return true;
    }
}
