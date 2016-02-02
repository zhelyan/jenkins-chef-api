package com.which.hudson.plugins.chef.util;

import com.which.hudson.plugins.chef.api.ChefConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import hudson.Platform;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zhelyan.panchev
 * Date: 18/10/13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class ChefConfigParser {
    private static final Pattern pemfilePattern = Pattern.compile("(?<=(/|\\\\|\"|'))(\\w+-?_?\\w+\\.pem)");

    public enum ConfigKey {
        URL("chef_server_url"), CLIENT("node_name"), CREDENTIAL("client_key");
        private final String key;

        private ConfigKey(String key) {
            this.key = key;
        }
    }

    public static final String defaultKnifeConfigLoc = Platform.current() == Platform.UNIX ? "/etc/chef/knife.rb" : "c:\\chef\\knife.rb";

    //boo
    private ChefConfigParser() {
    }

    /**
     * Builds a {@link com.which.hudson.plugins.chef.api.ChefConfig} object holding all necessary config details as needed by the {@link com.which.hudson.plugins.chef.api.ChefApiBuilder}
     *
     * @param config the knife config file to use /knife.rb/
     * @return new {@link com.which.hudson.plugins.chef.api.ChefConfig} object
     * @throws ParseConfigException
     * @throws IOException
     */
    public static synchronized ChefConfig parse(File config) throws ParseConfigException {
        Properties props = new Properties();
        try {
            props.load(new FileReader(config));
            for (ConfigKey entry : ConfigKey.values()) {
                if (props.get(entry.key) == null) {
                    throw new ParseConfigException("Missing required knife.rb property:: " + entry.key);
                }
            }
        } catch (IOException e) {
            throw new ParseConfigException(e);
        }
        String client = normalizedValue((String) props.get(ConfigKey.CLIENT.key));
        String url = normalizedValue((String) props.get(ConfigKey.URL.key));
        String credential = getClientKey(config, (String) props.get(ConfigKey.CREDENTIAL.key));

        return new ChefConfig(client, url, credential);
    }

    private static String normalizedValue(String value) {
        return value.replaceAll("\"", "");
    }

    private static String getClientKey(File configFile, String value) throws ParseConfigException {
        try {
            Matcher matcher = pemfilePattern.matcher(value);
            if (matcher.find()) {
                File pem = new File(configFile.getParent(), matcher.group());
                return Files.toString(pem, Charsets.UTF_8);
            }
        } catch (IOException e) {
            throw new ParseConfigException("Cannot load .pem:: " + ExceptionUtils.getRootCauseMessage(e));
        }
        throw new ParseConfigException("Cannot extract .pem from knife config file:: " + configFile.getAbsolutePath());
    }

    public static class ParseConfigException extends Exception {
        public ParseConfigException(String s) {
            super(s);
        }

        public ParseConfigException(Throwable t) {
            super(t);
        }
    }


}
