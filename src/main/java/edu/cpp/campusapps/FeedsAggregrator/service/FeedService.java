package edu.cpp.campusapps.FeedsAggregrator.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import edu.cpp.campusapps.FeedsAggregrator.Application;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    private Logger logger = LoggerFactory.getLogger(Application.class);

    @Cacheable("rssFeeds")
    public List<SyndEntry> fetch(String url) throws Exception {
        URL inputUrl = new URL(url);

        SyndFeedInput input = new SyndFeedInput();

        this.logger.debug("Fetching " + url);

        SyndFeed inFeed = input.build(new XmlReader(inputUrl));

        this.logger.debug("Feed fetched");

        return inFeed.getEntries();
    }
}
