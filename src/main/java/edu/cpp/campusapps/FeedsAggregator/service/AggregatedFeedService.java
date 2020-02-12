package edu.cpp.campusapps.FeedsAggregator.service;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import edu.cpp.campusapps.FeedsAggregator.dao.uPortalGroupsDao;
import edu.cpp.campusapps.FeedsAggregator.properties.AggregatedFeedProperties;
import edu.cpp.campusapps.FeedsAggregator.properties.CategoriesProperties;
import edu.cpp.campusapps.FeedsAggregator.properties.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AggregatedFeedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AggregatedFeedProperties aggregatedFeedProperties;

    @Autowired
    private CategoriesProperties categoriesProperties;

    @Autowired
    private FeedService service;

    @Autowired
    private uPortalGroupsDao groupDao;

    @Value("${maxAge:4}")
    private long maxAge;

    @Value("${fallbackImage:}")
    private String fallbackImage;

    private MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    @Value("${portalBaseUrl:http://localhost:8080/uPortal}")
    private String portalBaseUrl;

    @PostConstruct
    public void init() {
        this.fileTypeMap.addMimeTypes("image/png png");
    }

    /**
     * aggregateFeedsByGroups determines the user's groups from uPortal and
     * adds the appropriate feed categories for aggregation.
     */
    public SyndFeed aggregateFeedsByGroups(HttpServletRequest request) throws Exception {
        String oidc = request.getHeader(HttpHeaders.AUTHORIZATION);

        List<String> groups = this.groupDao.getGroups(oidc);

        List<String> categories = new ArrayList<>();
        categories.add("general");

        Iterator it = categoriesProperties.getCategories().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            String categoryName = pair.getKey().toString();
            Category category = (Category) pair.getValue();

            for (String group : groups) {
                boolean addCategory = category.getGroups().stream().anyMatch(requisiteGroup -> group.equals(requisiteGroup));

                if (addCategory && !categories.contains(categoryName)) {
                    categories.add(categoryName);
                }
            }
        }

        if(logger.isDebugEnabled()) {
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            logger.debug("Determined the following categories for {}: ", auth.getPrincipal());

            categories.forEach(category -> logger.debug("{}", category));
        }

        return this.aggregateFeeds(categories);
    }

    public SyndFeed aggregateFeeds(String strCategories) throws Exception {
        List<String> categories = new ArrayList<>();

        categories.add("general");

        if (strCategories != null) {
            for (String category : strCategories.split(",")) {
                if (categoriesProperties.getCategories().containsKey(category)) {
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

        List<SyndEntry> entries = new ArrayList<>();
        feed.setEntries(entries);

        // https://stackoverflow.com/a/23885950
        Date cutoffDate =
                Date.from(
                        LocalDateTime.now()
                                .minusWeeks(maxAge)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        for (String category : categories) {
            List<String> feedUrls = categoriesProperties.getCategories().get(category).getFeeds();

            for (String feedUrl : feedUrls) {
                List<SyndEntry> feedEntries = this.service.get(feedUrl);

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
                        boolean hasValidEnclosure = false;

                        for (SyndEnclosure enclosure : entry.getEnclosures()) {
                            if (enclosure.getUrl().trim().isEmpty()) {
                                if (!this.fallbackImage.isEmpty()) {
                                    enclosure.setUrl(this.fallbackImage);
                                }
                                else {
                                    continue;
                                }
                            }

                            if (enclosure.getType() != null && !enclosure.getType().isEmpty()) {
                                continue;
                            }

                            // This is making a broad assumption that the images are available on an https endpoint
                            String enclosureUrl = enclosure.getUrl().replace("http://", "https://");
                            enclosure.setUrl(enclosureUrl);

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

                // IntelliJ IDEA wants to replace this with a Comparator.comparing, but it's probably best to leave it
                // as is for now
                Collections.sort(
                        entries,
                        (SyndEntry e1, SyndEntry e2) ->
                                e1.getPublishedDate().compareTo(e2.getPublishedDate()));
            }
        }

        Collections.reverse(entries);

        logger.debug(entries.size() + " entries");

        return feed;
    }
}
