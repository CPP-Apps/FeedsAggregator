# FeedsAggregator

FeedsAggregator is a microservice designed to be bundled with uPortal and used
with a content carousel web component.

The service periodically caches all of the RSS feeds and provides the user with
a personalized feed based on their uPortal group memberships.

## Configuration

`feeds-aggregator.properties` has configuration properties for various things.
Implementers can override the properties by placing overridden values in
`${PORTAL_HOME}/feeds-aggregator.properties`.

## Endpoints

- /api/v0/aggregated-feed
