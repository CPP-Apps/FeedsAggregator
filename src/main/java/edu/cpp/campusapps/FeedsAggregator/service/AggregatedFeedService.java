package edu.cpp.campusapps.FeedsAggregator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rometools.rome.feed.synd.*;
import edu.cpp.campusapps.FeedsAggregator.util.Category;
import edu.cpp.campusapps.FeedsAggregator.util.AggregatedFeedProperties;
import edu.cpp.campusapps.FeedsAggregator.util.CategoriesProperties;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AggregatedFeedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AggregatedFeedProperties aggregatedFeedProperties;

    @Autowired
    private CategoriesProperties categoriesProperties;

    @Autowired
    private FeedService service;

    @Value("${maxAge:4}")
    private long maxAge;

    private final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    @Value("${portalBaseUrl:http://localhost:8080/uPortal}")
    private String portalBaseUrl;

    public SyndFeed aggregateFeeeds(HttpServletRequest request) throws Exception {
        String oidc = request.getHeader(HttpHeaders.AUTHORIZATION);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, oidc);

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<JsonNode> groupsApiResponse = restTemplate.exchange(portalBaseUrl + "/api/groups", HttpMethod.GET, entity, JsonNode.class);

        JsonNode groupsNode = groupsApiResponse.getBody().get("groups");

        List<String> groups = new ArrayList<>();

        if (groupsNode.isArray()) {
            for (JsonNode group : groupsNode) {
                groups.add(group.get("name").textValue());
            }
        }

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
            List<String> feedUrls = categoriesProperties.getCategories().get(category).getFeeds();

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
