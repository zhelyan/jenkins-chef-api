package com.which.hudson.plugins.chef.credentials;

import com.which.hudson.plugins.chef.api.ChefConfig;
import com.which.hudson.plugins.chef.credentials.impl.ChefCredentialsImpl;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.Recommended;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

/**
 * Credentials that have an ID, description and username. Most credentials that have a username should aim to implement
 * this interface.
 *
 * @since 1.6
 */
@Recommended(since = "1.6")
@NameWith(value = ChefCredentials.NameProvider.class, priority = 16)
public interface ChefCredentials  extends StandardCredentials {

    public ChefConfig getChefConfig();

     /**
     * Our name provider.
     *
     * @since 1.7
     */
    public static class NameProvider extends CredentialsNameProvider<ChefCredentials> {

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getName(@NonNull ChefCredentials c) {
            return new Localizable(ResourceBundleHolder.get(ChefCredentialsImpl.class), "DisplayName").toString();
        }
    }
}