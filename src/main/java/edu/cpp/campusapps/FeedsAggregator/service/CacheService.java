package edu.cpp.campusapps.FeedsAggregator.service;

import edu.cpp.campusapps.FeedsAggregator.dao.uPortalGroupsDao;
import edu.cpp.campusapps.FeedsAggregator.model.CacheControllerV0Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Service
public class CacheService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private uPortalGroupsDao groupsDao;

    @Value("${edu.cpp.mobwebapps.FeedsAggregator.managementGroups}")
    private String managementGroupsParameter;

    public boolean evictFeed(HttpServletRequest request) {
        List<String> groups = groupsDao.getGroups(request);

        boolean canEvict = false;

        for(String group : groups) {
            for(String requisiteGroup : managementGroupsParameter.split(",")) {
                if (requisiteGroup.equals(group)) {
                    canEvict = true;

                    break;
                }
            }

            if (canEvict) {
                break;
            }
        }

        if (!canEvict) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.error("{} is not authorized to evict feed from cache", authentication.getPrincipal());

            return false;
        }

        String feedUrl = request.getParameter("feed");

        return this.evictFeed(feedUrl);
    }

    private boolean evictFeed(String feedUrl) {
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
