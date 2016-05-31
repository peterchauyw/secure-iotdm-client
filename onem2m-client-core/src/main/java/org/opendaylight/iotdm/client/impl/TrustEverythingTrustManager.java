package org.opendaylight.iotdm.client.impl;

import javax.net.ssl.X509TrustManager;

/**
 * Created by ychau on 4/26/16.
 */
public class TrustEverythingTrustManager implements X509TrustManager {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }
}