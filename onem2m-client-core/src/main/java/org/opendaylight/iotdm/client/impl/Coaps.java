package org.opendaylight.iotdm.client.impl;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.opendaylight.iotdm.client.Request;
import org.opendaylight.iotdm.client.Response;
import org.opendaylight.iotdm.client.exception.Onem2mNoOperationError;
import org.opendaylight.iotdm.constant.OneM2M;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.californium.core.Utils;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

/**
 * Created by ychau on 7/20/16.
 */

public class Coaps extends Coap {

    static {
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel(Level.FINE);
    }

    private static final String TRUST_STORE_PASSWORD = "rootPass";
    private static final String KEY_STORE_PASSWORD = "endPass";
    private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";

    private DTLSConnector dtlsConnector;

    @Override
    public Response send(Request request) {
//        org.eclipse.californium.core.coap.Request coapRequest = new CoapRequestBuilder(request).build());
        oneM2MCoapRequest coapRequest = new CoapRequestBuilder(request).build();
        org.eclipse.californium.core.coap.Response coapResponse;

        //Set up DTLS
        try {
            // load key store
            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream in = getClass().getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
            keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());

            // load trust store
            KeyStore trustStore = KeyStore.getInstance("JKS");
            InputStream inTrust = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
            trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

            // You can load multiple certificates if needed
            Certificate[] trustedCertificates = new Certificate[1];
            trustedCertificates[0] = trustStore.getCertificate("root");

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
//            builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));

            builder.setIdentity((PrivateKey) keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
                    keyStore.getCertificateChain("client"), true);

            builder.setTrustStore(trustedCertificates);
            dtlsConnector = new DTLSConnector(builder.build());

        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Could not load the keystore");
            e.printStackTrace();
        }
        coapRequest.send(dtlsConnector);

        try {
            coapResponse = coapRequest.waitForResponse(request.getTimeout());
            Objects.requireNonNull(coapResponse);
        } catch (InterruptedException e) {
            throw new AssertionError(e.getMessage());
        } catch (NullPointerException e) {
            throw new AssertionError("Coap response is null");
        }
        System.out.println(coapResponse.getCode());
        System.out.println(coapResponse.getOptions());
        System.out.println("\nADVANCED\n");
        System.out.println(Utils.prettyPrint(coapResponse));
        return new ResponseBuilder(coapResponse).build();
    }

    public static class CoapRequestBuilder {
        oneM2MCoapRequest coapRequest = null;

        public CoapRequestBuilder(Request request) {
            if (request == null) return;

            RequestHelper requestHelper = new RequestHelper(request);
            switch (OneM2M.Operation.getEnum(requestHelper.getOp())) {
                case CREATE:
                    coapRequest = oneM2MCoapRequest.buildPost();
                    coapRequest.setPayload(requestHelper.getPayload());
                    break;
                case RETRIEVE:
                    coapRequest = oneM2MCoapRequest.buildGet();
                    break;
                case UPDATE:
                    coapRequest = oneM2MCoapRequest.buildPut();
                    coapRequest.setPayload(requestHelper.getPayload());
                    break;
                case DELETE:
                    coapRequest = oneM2MCoapRequest.buildDelete();
                    break;
                case NOTIFY:
                    coapRequest = oneM2MCoapRequest.buildPost();
                    break;
                default:
                    throw new Onem2mNoOperationError();
            }
            coapRequest.setConfirmable(true);
            OptionSet optionSet = coapRequest.getOptions();
            optionSet.setUriPath(OneM2M.Path.toToPathMapping(requestHelper.getPath()));
            optionSet.setUriHost(requestHelper.getHost());
            optionSet.setUriPort(requestHelper.getPort());
            addQuery(optionSet, requestHelper.getQuery());
            addOption(optionSet, requestHelper.getHeader());
            try {
                coapRequest.setDestination(InetAddress.getByName(requestHelper.getHost()));
                coapRequest.setDestinationPort(requestHelper.getPort());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        private void addQuery(OptionSet os, Map<String, Set<String>> query) {
            for (Map.Entry<String, Set<String>> entry : query.entrySet()) {
                String key = entry.getKey();
                String value = RequestHelper.concatQuery(entry.getValue());
                os.addUriQuery(key + "=" + value);
            }
        }

        private void addOption(OptionSet os, Map<String, Set<String>> map) {
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                int key = OneM2M.CoAP.Option.map2Int(entry.getKey());
                String value = RequestHelper.concatQuery(entry.getValue());
                os.addOption(new Option(key, value));
            }
        }

        //        public org.eclipse.californium.core.coap.Request build() {
        public oneM2MCoapRequest build(){
            return coapRequest;
        }
    }
}
