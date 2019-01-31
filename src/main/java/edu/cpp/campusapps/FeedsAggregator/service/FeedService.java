package edu.cpp.campusapps.FeedsAggregator.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;
import java.io.InputStream;
import java.util.List;

@Service
public class FeedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @CacheResult(cacheName = "feeds")
    public List<SyndEntry> fetch(String url) throws Exception {
        this.logger.debug("Fetching " + url);

        SyndFeed inFeed;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpUriRequest request = new HttpGet(url);

            try (CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent()) {

                SyndFeedInput input = new SyndFeedInput();

                inFeed = input.build(new XmlReader(stream));
            }
        }

        this.logger.debug("Feed fetched");

        return inFeed.getEntries();
    }
}
