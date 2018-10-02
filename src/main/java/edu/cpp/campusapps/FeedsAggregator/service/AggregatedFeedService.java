package edu.cpp.campusapps.FeedsAggregator.service;

import com.rometools.rome.feed.synd.*;
import edu.cpp.campusapps.FeedsAggregator.util.AggregatedFeedProperties;
import edu.cpp.campusapps.FeedsAggregator.util.FeedsProperties;
import edu.cpp.campusapps.FeedsAggregator.util.SortByPubDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AggregatedFeedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AggregatedFeedProperties aggregatedFeedProperties;

    @Autowired
    private FeedsProperties feedsProperties;

    @Autowired
    private FeedService service;

    @Value("${maxAge:4}")
    private long maxAge;

    private final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    public SyndFeed aggregateFeeds(String strCategories) throws Exception {
        List<String> categories = new ArrayList<>();

        categories.add("general");

        if (strCategories != null) {
            for (String category : strCategories.split(",")) {
                if (feedsProperties.getFeeds().containsKey(category)) {
                    categories.add(category);
                }
            }
        }

        return this.aggregateFeeds(categories);
    }

    public SyndFeed aggregateFeeds(List<String> categories) throws Exception {
        SyndFeed feed = new SyndFeedImpl();

        feed.setFeedType(this.aggregatedFeedProperties.getType());
        feed.setTitle(this.aggregatedFeedProperties.getTitle());
        feed.setDescription(this.aggregatedFeedProperties.getDescription());
        feed.setAuthor(this.aggregatedFeedProperties.getAuthor());
        feed.setLink(this.aggregatedFeedProperties.getLink());

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setEntries(entries);

        // https://stackoverflow.com/a/23885950
        Date cutoffDate =
                Date.from(
                        LocalDateTime.now()
                                .minusWeeks(maxAge)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        for (String category : categories) {
            List<String> feedUrls = feedsProperties.getFeeds().get(category);

            for (String feedUrl : feedUrls) {
                List<SyndEntry> feedEntries = this.service.fetch(feedUrl);

                for (SyndEntry entry : feedEntries) {
                    boolean addEntry = true;

                    if (entry.getPublishedDate().before(cutoffDate)) {
                        continue;
                    }

                    for (SyndEntry existingEntry : entries) {
                        if (existingEntry.getUri().equals(entry.getUri())) {
                            addEntry = false;

                            logger.debug(entry.getUri() + " exists. Skipping");

                            break;
                        }
                    }

                    if (addEntry) {
                        for (SyndEnclosure enclosure : entry.getEnclosures()) {
                            if (enclosure.getType() != null && !enclosure.getType().isEmpty()) {
                                continue;
                            }

                            // TODO: Improve file type checking
                            String extension =
                                    enclosure
                                            .getUrl()
                                            .substring(enclosure.getUrl().lastIndexOf("."));

                            enclosure.setType(fileTypeMap.getContentType(extension));
                        }

                        entries.add(entry);
                    }
                }

                Collections.sort(entries, new SortByPubDate());
            }
        }

        Collections.reverse(entries);

        logger.debug(entries.size() + " entries");

        return feed;
    }
}
