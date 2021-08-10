package edu.cpp.campusapps.FeedsAggregator;

import com.rometools.rome.feed.synd.SyndEntry;

import edu.cpp.campusapps.FeedsAggregator.properties.CategoriesProperties;
import edu.cpp.campusapps.FeedsAggregator.properties.Category;
import edu.cpp.campusapps.FeedsAggregator.service.FeedService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class CacheJob implements Job {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CategoriesProperties categoriesProperties;

    @Autowired
    private FeedService feedService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int feeds = 0;
        int entries = 0;

        Instant startTime = Instant.now();

        for (Map.Entry<String, Category> feedCategory :
                this.categoriesProperties.getCategories().entrySet()) {
            logger.info("Caching RSS feeds for category = {}", feedCategory.getKey());

            for (String feedUrl : feedCategory.getValue().getFeeds()) {
                List<SyndEntry> rssFeed = this.feedService.cache(feedUrl);

                if (rssFeed == null) {
                    logger.error("Failed to get RSS feed: {}", feedUrl);
                    continue;
                }

                feeds++;

                entries += rssFeed.size();

                logger.info("Cached {} entry/entries from {}", rssFeed.size(), feedUrl);
            }
        }

        Instant endTime = Instant.now();

        logger.info(
                "Cached {} RSS feed(s) with {} entries in {} seconds",
                feeds,
                entries,
                Duration.between(startTime, endTime).getSeconds());
    }
}
