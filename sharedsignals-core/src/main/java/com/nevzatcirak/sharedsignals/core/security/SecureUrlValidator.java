package com.nevzatcirak.sharedsignals.core.security;

import com.nevzatcirak.sharedsignals.api.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

public class SecureUrlValidator {

    private static final Logger log = LoggerFactory.getLogger(SecureUrlValidator.class);

    public static void validate(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new InvalidConfigurationException("Endpoint URL cannot be empty");
        }

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            if (!"https".equalsIgnoreCase(url.getProtocol()) && !"http".equalsIgnoreCase(url.getProtocol())) {
                 throw new InvalidConfigurationException("Only HTTPS (or HTTP for dev) protocols are supported.");
            }

            InetAddress address = InetAddress.getByName(url.getHost());

            if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                log.warn("SECURITY WARNING: Allowing event transmission to local/private IP: {}", urlString);
                // In PROD: throw new InvalidConfigurationException("Destination URL is not allowed (Private/Local IP).");
            }

        } catch (Exception e) {
            throw new InvalidConfigurationException("Invalid Endpoint URL: " + e.getMessage());
        }
    }
}
