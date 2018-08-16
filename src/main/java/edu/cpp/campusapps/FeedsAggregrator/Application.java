package edu.cpp.campusapps.FeedsAggregrator;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCaching
@RestController
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private FeedProperties feedProperties;

    @Autowired
    private FeedsProperties fp;

    @Autowired
    private FeedService service;

    @Value("${maxAge:4}")
    private long maxAge;

    @RequestMapping("/")
    public String index(HttpServletRequest request) throws Exception {
        String categoriesParameter = request.getParameter("categories");

        List<String> categories = new ArrayList<>();

        categories.add("general");

        if (categoriesParameter != null) {
            for (String category : categoriesParameter.split(",")) {
                if (fp.getFeeds().containsKey(category)) {
                    categories.add(category);
                }
            }
        }

        SyndFeed feed = new SyndFeedImpl();

        feed.setFeedType(this.feedProperties.getType());
        feed.setTitle(this.feedProperties.getTitle());
        feed.setDescription(this.feedProperties.getDescription());
        feed.setAuthor(this.feedProperties.getAuthor());
        feed.setLink(this.feedProperties.getLink());

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
            List<String> feedUrls = fp.getFeeds().get(category);

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
                        entries.add(entry);
                    }
                }

                Collections.sort(entries, new SortByPubDate());
            }
        }

        Collections.reverse(entries);

        SyndFeedOutput output = new SyndFeedOutput();
        return output.outputString(feed);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
