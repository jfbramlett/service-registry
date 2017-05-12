package com.hbo.serviceregistry.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object holding our service response definition.
 */
public class Service {
    private final String url;

    /**
     * Constructor.
     *
     * @param url The url for the service
     */
    public Service(@JsonProperty("url") final String url) {
        this.url = url;
    }

    /**
     * Gets the URL for the service.
     *
     * @return String The url for the service
     */
    public String getUrl() {
        return url;
    }
}
