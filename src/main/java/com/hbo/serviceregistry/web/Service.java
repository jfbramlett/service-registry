package com.hbo.serviceregistry.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object holding our service response definition.
 */
public class Service {
    private final String url;
    private final String name;

    /**
     * Constructor.
     *
     * @param name The service name
     * @param url The url for the service
     */
    public Service(@JsonProperty("name") final String name, @JsonProperty("url") final String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Gets the service name.
     *
     * @return String The service name
     */
    public String getName() {
        return name;
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
