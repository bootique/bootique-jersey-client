package io.bootique.jersey.client;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * @since 0.25
 */
@BQConfig
public class TrustStoreFactory {

    ResourceFactory location;
    String password;


    @BQConfigProperty
    public void setLocation(ResourceFactory location) {
        this.location = location;
    }

    @BQConfigProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public KeyStore createTrustStore() {

        URL url = getLocationUrl();
        char[] passwordChars = getPasswordChars();

        try (InputStream in = url.openStream()) {

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(in, passwordChars);
            return trustStore;

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Error loading client trust store from " + url, e);
        }
    }

    private char[] getPasswordChars() {
        String password = this.password != null ? this.password : "changeit";
        return password.toCharArray();
    }

    private URL getLocationUrl() {
        Objects.requireNonNull(location, "TrustStore 'location' is not specified");
        return this.location.getUrl();
    }
}
