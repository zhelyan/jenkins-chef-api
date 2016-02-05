package com.which.hudson.plugins.chef.credentials.impl;

import com.which.hudson.plugins.chef.api.ChefApiBuilder;
import com.which.hudson.plugins.chef.api.ChefConfig;
import com.which.hudson.plugins.chef.credentials.ChefCredentials;
import com.which.hudson.plugins.chef.util.ChefConfigParser;
import com.cloudbees.plugins.credentials.BaseCredentials;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Hudson;
import hudson.util.FormValidation;
import org.jclouds.chef.ChefApi;
import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.File;


public class ChefCredentialsImpl extends BaseCredentials implements ChefCredentials {
    /**
     * Our ID.
     */
    @NonNull
    private final String id;

    /**
     * Our description.
     */
    @NonNull
    private final String description;

    private ChefConfig chefConfig;
    private String configFile;

    @DataBoundConstructor
    public ChefCredentialsImpl(@CheckForNull CredentialsScope scope,
                               @CheckForNull String id, @CheckForNull String description, @CheckForNull String configFile){
        super(scope);
        this.id = IdCredentials.Helpers.fixEmptyId(id);
        try{
            File cfg = new File(configFile);
            this.chefConfig = ChefConfigParser.parse(cfg);
        } catch(ChefConfigParser.ParseConfigException e){
            chefConfig = null;
            e.printStackTrace();
        }catch(Exception e){
            //ummm ...
            e.printStackTrace();
        }
        //save file path/descr even if wrong
        this.configFile = configFile;
        this.description = chefConfig !=null? String.format("Client: %s, URL: %s", chefConfig.getClient(), chefConfig.getURL()) : "*** Not configured ***";
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    public String getConfigFile() {
        return configFile;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public ChefConfig getChefConfig() {
        return chefConfig;
    }

    @NonNull
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        return IdCredentials.Helpers.equals(this, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return IdCredentials.Helpers.hashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends CredentialsDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return new Localizable(ResourceBundleHolder.get(ChefCredentialsImpl.class), "DisplayName").toString();
        }

        public FormValidation doCheckConfigFile(@QueryParameter("configFile") @CheckForNull String knifeConfigPath){
            return FormValidation.validateRequired(knifeConfigPath);
        }

        public FormValidation doTestConfigFile(@QueryParameter("configFile") @CheckForNull String knifeConfigPath) {
            ChefConfig knifeConfig;
            try {
                knifeConfig = ChefConfigParser.parse(new File(knifeConfigPath));
                ChefApi chefApi = ChefApiBuilder.build(knifeConfig, null);
                chefApi.listEnvironments();
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }

            return FormValidation.ok("Success, client name:: %s, Chef URL:: %s", knifeConfig.getClient(), knifeConfig.getURL());
        }

    }



}
