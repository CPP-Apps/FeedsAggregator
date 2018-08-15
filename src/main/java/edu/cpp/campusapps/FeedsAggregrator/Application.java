package edu.cpp.campusapps.FeedsAggregrator;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class Application {

    private ArrayList<String> feeds;

    Application() {
        this.feeds = new ArrayList<>();

        this.feeds.add("https://polycentric.cpp.edu/tag/student-and-campus-life/feed/");
        this.feeds.add("http://polycentric.cpp.edu/tag/student-success/feed/");
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
            URL inputUrl = new URL(feedUrl);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed inFeed = input.build(new XmlReader(inputUrl));

            entries.addAll(inFeed.getEntries());
        }

        SyndFeedOutput output = new SyndFeedOutput();
        return output.outputString(feed);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
