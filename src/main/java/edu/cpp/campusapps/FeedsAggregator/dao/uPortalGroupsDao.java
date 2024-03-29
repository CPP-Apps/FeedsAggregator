package edu.cpp.campusapps.FeedsAggregator.dao;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@Repository
public class uPortalGroupsDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${portalBaseUrl:http://localhost:8080/uPortal}")
    private String portalBaseUrl;

    public List<String> getGroups(HttpServletRequest request) {
        String oidc = request.getHeader(HttpHeaders.AUTHORIZATION);

        return this.getGroups(oidc);
    }

    public List<String> getGroups(String oidc) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, oidc);

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<JsonNode> groupsApiResponse =
                restTemplate.exchange(
                        portalBaseUrl + "/api/groups", HttpMethod.GET, entity, JsonNode.class);

        JsonNode groupsNode = groupsApiResponse.getBody().get("groups");

        List<String> groups = new ArrayList<>();

        if (groupsNode.isArray()) {
            for (JsonNode group : groupsNode) {
                groups.add(group.get("name").textValue());
            }
        }

        return groups;
    }
}
