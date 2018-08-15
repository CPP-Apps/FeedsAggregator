package edu.cpp.campusapps.FeedsAggregrator;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCaching
@RestController
public class Application {

    private ArrayList<String> feeds = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private FeedService service;

    @PostConstruct
    public void init() {
        this.feeds.add("https://polycentric.cpp.edu/tag/student-and-campus-life/feed/");
        this.feeds.add("https://polycentric.cpp.edu/tag/student-success/feed/");
    }

    @RequestMapping("/")
    public String index() throws Exception {
        SyndFeed feed = new SyndFeedImpl();

        feed.setFeedType("rss_2.0");
        feed.setTitle("Aggregated Feed");
        feed.setDescription("Personalized Aggregated Feed");
        feed.setAuthor("Various Authors");
        feed.setLink("https://my.cpp.edu");

        List entries = new ArrayList();
        feed.setEntries(entries);

        for(String feedUrl : feeds) {
            List<SyndEntry> feedEntries = this.service.fetch(feedUrl);

            entries.addAll(feedEntries);

            Collections.sort(entries, new SortByPubDate());
        }

        Collections.reverse(entries);

        SyndFeedOutput output = new SyndFeedOutput();
        return output.outputString(feed);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
