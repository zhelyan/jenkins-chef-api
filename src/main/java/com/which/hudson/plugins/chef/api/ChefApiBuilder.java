package com.which.hudson.plugins.chef.api;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.which.hudson.plugins.chef.credentials.ChefCredentials;
import com.which.hudson.plugins.chef.util.ChefConfigParser;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.ChefApiMetadata;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds {@link ChefApi} object from knife.rb, {@link ChefConfig} object or by passing the details directly
 */
public class ChefApiBuilder {

    private ChefApiBuilder() {
        super();
    }

    /**
     * @param knifeConfig knife.rb
     * @return {@link ChefApi}
     * @throws ConfigurationException                                                   if anything goes wrong on the api side /authentication etc/
     * @throws IOException                                                              if knife.rb cannot be read
     * @throws com.which.hudson.plugins.chef.util.ChefConfigParser.ParseConfigException if knife.rb cannot be parsed
     */
    public static ChefApi build(File knifeConfig, List<com.google.inject.Module> modules) throws ConfigurationException, IOException, ChefConfigParser.ParseConfigException {
        ChefConfig config = ChefConfigParser.parse(knifeConfig);
        return build(config, modules);
    }

    /**
     * @param knifeConfig knife.rb
     * @return {@link ChefApi}
     * @throws ConfigurationException                                                   if anything goes wrong on the api side /authentication etc/
     * @throws IOException                                                              if knife.rb cannot be read
     * @throws com.which.hudson.plugins.chef.util.ChefConfigParser.ParseConfigException if knife.rb cannot be parsed
     */
    public static ChefApi build(File knifeConfig) throws ConfigurationException, IOException, ChefConfigParser.ParseConfigException {
        return build(knifeConfig, null);
    }

    /**
     * Builds the API using the dedicated {@link ChefConfig} config wrapper
     *
     * @param config  {@link ChefConfig}
     * @param modules If no Modules are specified, the default logging and http transports will be installed.
     * @return {@link ChefApi}
     */
    public static ChefApi build(ChefConfig config, List<com.google.inject.Module> modules) throws ConfigurationException {
        return build(config.getURL(), config.getClient(), config.getCredential(), modules);
    }


    /**
     * Given a unique credentialID string will lookup {@link ChefCredentials} and use it to build an API
     * for this credential's Chef server
     *
     * @param credentialID {@link ChefCredentials}
     * @param modules      If no Modules are specified, the default logging and http transports will be installed.
     * @return {@link ChefApi}
     */
    public static ChefApi build(String credentialID, List<com.google.inject.Module> modules) throws ConfigurationException {
        ChefCredentials selectedCredential = getChefCredentials(credentialID);
        return ChefApiBuilder.build(selectedCredential.getChefConfig(), modules);
    }

    /**
     * Searches for the provided credential ID in the list of all available (@link ChefCredentials ) and builds an API
     * for the credential's Chef server
     *
     * @param credentialID
     * @return {@link ChefApi}
     */
    public static ChefApi build(String credentialID) throws ConfigurationException {
        ChefCredentials selectedCredential = getChefCredentials(credentialID);
        return ChefApiBuilder.build(selectedCredential.getChefConfig(), null);
    }


    /**
     * Build the API using parameters
     *
     * @param url        the url of the Chef server
     * @param clientName client name
     * @param clientPem  contents of the client key
     * @return
     */
    public static ChefApi build(String url, String clientName, String clientPem, List<com.google.inject.Module> modules) throws ConfigurationException {
        ContextBuilder builder = null;
        try {
            builder = ContextBuilder.newBuilder(new ChefApiMetadata()) //
                    .endpoint(url)
                    .credentials(clientName, clientPem);
            if (modules != null) {
                builder.modules(modules);
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return builder.buildApi(ChefApi.class);

    }

    /**
     * Searches for the provided credential ID in the list of all available (@link ChefCredentials )
     *
     * @param credentialID Unique credential ID
     * @return {@link ChefCredentials}
     * @throws ConfigurationException
     */
    public static ChefCredentials getChefCredentials(String credentialID) throws ConfigurationException {
        ChefCredentials selectedCredential = null;
        List<ChefCredentials> credentials = CredentialsProvider.lookupCredentials(ChefCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, new LinkedList<DomainRequirement>());
        if (credentials.isEmpty()) {
            throw new ConfigurationException("No Chef credentials found.");
        }
        for (ChefCredentials credential : credentials) {
            if (credential.getId().equals(credentialID)) {
                selectedCredential = credential;
                break;
            }
        }
        if (selectedCredential == null) {
            throw new ConfigurationException("The Chef credentials used to configure this plugin don't exist. Edit the job and choose new credentials");
        }
        return selectedCredential;
    }

    public static class ConfigurationException extends Exception {

        public ConfigurationException(String s) {
            super(s);
        }

        public ConfigurationException(Throwable e) {
            super(e);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
