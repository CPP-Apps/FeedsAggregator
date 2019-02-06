package edu.cpp.campusapps.FeedsAggregator.controller;

import edu.cpp.campusapps.FeedsAggregator.model.CacheControllerV0Response;
import edu.cpp.campusapps.FeedsAggregator.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/v0/cache")
public class CacheControllerV0 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheService service;

    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
    public CacheControllerV0Response evictFeed(HttpServletRequest request, HttpServletResponse response) {
        String feedUrl = request.getParameter("feed");

        CacheControllerV0Response apiResponse = new CacheControllerV0Response();

        if (!this.service.evictFeed(request)) {
            apiResponse.setStatus(500);
            apiResponse.setMessage(String.format("Failed to remove %s from cache", feedUrl));

            response.setStatus(500);

            return apiResponse;
        }

        apiResponse.setStatus(200);
        apiResponse.setMessage(String.format("Removed %s from cache", feedUrl));

        return apiResponse;
    }
}
