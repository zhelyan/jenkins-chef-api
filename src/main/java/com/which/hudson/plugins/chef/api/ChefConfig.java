package com.which.hudson.plugins.chef.api;

/**
 * Simple wrapper around knife.rb configuration allowing us to persist the config on Jenkins global config page
 * <p/>
 * Created with IntelliJ IDEA.
 * User: zhelyan.panchev
 * Date: 18/10/13
 * Time: 18:28
 */
public class ChefConfig {
    private final String client;
    private final String URL;
    private final String credential;

    public ChefConfig(String client, String URL, String credential) {
        this.client = client;
        this.URL = URL;
        this.credential = credential;
    }

    public String getClient() {
        return client;
    }

    public String getURL() {
        return URL;
    }

    public String getCredential() {
        return credential;
    }


}
