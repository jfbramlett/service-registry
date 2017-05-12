package com.hbo.serviceregistry.web;

/**
 * Value object represent the result of our service reload.
 */
public class ServiceReload {
    private final int originalRegistrations;
    private final int newRegistrations;
    private final String url;

    /**
     * Constructor.
     *
     * @param url The url for the data being loaded
     * @param originalRegistrations The number of service registrations before the reload
     * @param newRegistrations The number of service registrations after the reload
     */
    public ServiceReload(final String url, final int originalRegistrations, final int newRegistrations) {
        this.url = url;
        this.originalRegistrations = originalRegistrations;
        this.newRegistrations = newRegistrations;
    }

    /**
     * Gets the url for the source of our service registrations.
     *
     * @return String The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the number of registrations that we had before reload.
     *
     * @return int The number of registrations
     */
    public int getOriginalRegistrations() {
        return originalRegistrations;
    }

    /**
     * Gets the number of registrations that we had after reload.
     *
     * @return int The number of registrations
     */
    public int getNewRegistrations() {
        return newRegistrations;
    }
}
