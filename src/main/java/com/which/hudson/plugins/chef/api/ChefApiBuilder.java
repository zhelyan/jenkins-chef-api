package com.which.hudson.plugins.chef.api;

import com.which.hudson.plugins.chef.credentials.ChefCredentials;
import com.which.hudson.plugins.chef.util.ChefConfigParser;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.jclouds.ContextBuilder;
import org.jclouds.chef.ChefApi;
import org.jclouds.chef.ChefApiMetadata;
import org.jclouds.chef.ChefContext;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds {@link ChefApi} object from knife.rb, {@link ChefConfig} object or by passing the details directly
 */
public enum ChefApiBuilder {
    INSTANCE;


//
//    static {
//        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        });
//    }

    public enum Chef {
        ENTERPRISE("enterprise"),
        PRIVATE("chef");
        private String license;

        private Chef(String type) {
            license = type;
        }

        public String getLicense() {
            return license;
        }
    }

    /**
     * @param knifeConfig knife.rb
     * @return {@link ChefApi}
     * @throws ConfigurationException                                                       if anything goes wrong on the api side /authentication etc/
     * @throws IOException                                                                  if knife.rb cannot be read
     * @throws com.which.hudson.plugins.chef.util.ChefConfigParser.ParseConfigException if knife.rb cannot be parsed
     */
    public ChefApi build(File knifeConfig) throws ConfigurationException, IOException, ChefConfigParser.ParseConfigException {
        ChefConfig config = ChefConfigParser.parse(knifeConfig);
        return build(config);
    }

    /**
     * Builds the API using the dedicated {@link ChefConfig} config wrapper
     *
     * @param config {@link ChefConfig}
     * @return {@link ChefApi}
     */
    public ChefApi build(ChefConfig config) throws ConfigurationException {
        Chef type = config.getURL().contains("organizations") ? Chef.ENTERPRISE : Chef.PRIVATE;
        return build(type, config.getURL(), config.getClient(), config.getCredential());
    }

    /**
     * Builds the API by looking up the provided credential ID from the list of all available (@link ChefCredentials )
     *
     * @param credentialID
     * @return {@link ChefApi}
     */
    public ChefApi build(String credentialID) throws ConfigurationException {
        ChefCredentials selectedCredential = null;
        List<ChefCredentials> credentials = (List<ChefCredentials>) CredentialsProvider.lookupCredentials(ChefCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, new LinkedList<DomainRequirement>());
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
            throw new ChefApiBuilder.ConfigurationException("The Chef credentials used to configure this plugin don't exist. Edit the job and choose new credentials");
        }
        return ChefApiBuilder.INSTANCE.build(selectedCredential.getChefConfig());
    }

    /**
     * Build the API using parameters
     *
     * @param type       {@link Chef} the Chef server type /reserved for future use/
     * @param url        the url of the Chef server
     * @param clientName client name
     * @param clientPem  contents of the client key
     * @return
     */
    public ChefApi build(Chef type, String url, String clientName, String clientPem) throws ConfigurationException {
        ChefContext context = null;
        try {
            context = ContextBuilder.newBuilder(new ChefApiMetadata()) //
                    .endpoint(url)
                    .credentials(clientName, clientPem)
                    .buildView(ChefContext.class);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return context.unwrapApi(ChefApi.class);
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
