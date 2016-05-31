package org.opendaylight.iotdm.client.impl;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.network.EndpointManager;
import org.onem2m.xml.protocols.PrimitiveContent;
import org.opendaylight.iotdm.client.Request;
import org.opendaylight.iotdm.client.Response;
import org.opendaylight.iotdm.client.api.Client;
import org.opendaylight.iotdm.client.exception.Onem2mNoOperationError;
import org.opendaylight.iotdm.client.util.Json;
import org.opendaylight.iotdm.constant.OneM2M;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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


import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.ScandiumLogger;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

/**
 * Created by wenxshi on 10/22/15.
 */
public class Coap implements Client {


    static {
        ScandiumLogger.initialize();
        ScandiumLogger.setLevel(Level.FINE);
    }


    private static final String TRUST_STORE_PASSWORD = "rootPass";
    private static final String KEY_STORE_PASSWORD = "endPass";
    private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";

    private DTLSConnector dtlsConnector;



//    private CoapServer server=new CoapServer(5111);

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

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
                builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));
                builder.setIdentity((PrivateKey) keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
                        keyStore.getCertificateChain("client"), true);
                builder.setTrustStore(trustedCertificates);
                dtlsConnector = new DTLSConnector(builder.build());


            } catch (GeneralSecurityException | IOException e) {
                System.err.println("Could not load the keystore");
                e.printStackTrace();
            }

//            CoapClient client = new CoapClient("coap", request.getHost(), request.getPort(), request.getRequestPrimitive().getTo());
//
//            StringBuilder builder = new StringBuilder()
//                    .append("coap").append("://").append(request.getHost()).append(":").append(request.getPort());
//            String path = request.getRequestPrimitive().getTo();
//            if (path.substring(0,1) == "/"){
//                builder.append(path);
//            }
//            else{
//                builder.append("/").append(path);
//            }
//            String uri = builder.toString();
//            CoapClient client = new CoapClient(uri);
//            client.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));

//            CoapResponse response;
//
//
//            if (request.getRequestPrimitive().getOp() == BigInteger.valueOf(2)) {
//                response = client.get();
//                System.out.println(response.getCode());
//                System.out.println(response.getOptions());
//                System.out.println(response.getResponseText());
//                System.out.println("\nADVANCED\n");
//                System.out.println(Utils.prettyPrint(response));
//
//            }else{
//                response = client.advanced(coapRequest);
//            }
//
//            coapResponse = response.advanced();

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
//        org.eclipse.californium.core.coap.Request coapRequest = null;
        oneM2MCoapRequest coapRequest = null;

        public CoapRequestBuilder(Request request) {
            if (request == null) return;

            RequestHelper requestHelper = new RequestHelper(request);
            switch (OneM2M.Operation.getEnum(requestHelper.getOp())) {
                case CREATE:
//                    coapRequest = org.eclipse.californium.core.coap.Request.newPost();
                    coapRequest = oneM2MCoapRequest.buildPost();
                    coapRequest.setPayload(requestHelper.getPayload());
                    break;
                case RETRIEVE:
//                    coapRequest = org.eclipse.californium.core.coap.Request.newGet();
                    coapRequest = oneM2MCoapRequest.buildGet();
                    break;
                case UPDATE:
//                    coapRequest = org.eclipse.californium.core.coap.Request.newPut();
                    coapRequest = oneM2MCoapRequest.buildPut();
                    coapRequest.setPayload(requestHelper.getPayload());
                    break;
                case DELETE:
//                    coapRequest = org.eclipse.californium.core.coap.Request.newDelete();
                    coapRequest = oneM2MCoapRequest.buildDelete();
                    break;
                case NOTIFY:
//                    coapRequest = org.eclipse.californium.core.coap.Request.newPost();
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

    public static class ResponseBuilder {
        Response response = null;

        public ResponseBuilder(org.eclipse.californium.core.coap.Response coapResponse) {
            if (coapResponse == null) return;

            OneM2M.ResponseStatusCodes responseStatusCode = null;
            String requestIdentifier = null;
            PrimitiveContent primitiveContent = null;
            String to = null;
            String from = null;
            OneM2M.Time originatingTimestamp = null;
            OneM2M.Time resultExpirationTimestamp = null;
            OneM2M.StdEventCats eventCategory = null;

            OptionSet os = coapResponse.getOptions();
            for (Option option : os.asSortedList()) {
                int key = option.getIntegerValue();
                switch (key) {
                    case OneM2M.CoAP.Option.ONEM2M_RSC:
                        responseStatusCode = OneM2M.ResponseStatusCodes.getEnum(BigInteger.valueOf(option.getIntegerValue()));
                        break;
                    case OneM2M.CoAP.Option.ONEM2M_RQI:
                        requestIdentifier = option.getStringValue();
                        break;
                    case OneM2M.CoAP.Option.ONEM2M_FR:
                        from = option.getStringValue();
                        break;
                    case OneM2M.CoAP.Option.ONEM2M_OT:
                        originatingTimestamp = new OneM2M.Time(option.getStringValue());
                        break;
                    case OneM2M.CoAP.Option.ONEM2M_RSET:
                        resultExpirationTimestamp = new OneM2M.Time(option.getStringValue());
                        break;
                    case OneM2M.CoAP.Option.ONEM2M_EC:
                        eventCategory = OneM2M.StdEventCats.getEnum(new BigInteger(option.getStringValue()));
                }
            }
            String content = coapResponse.getPayloadString();
            primitiveContent = Json.newInstance().fromJson(content, PrimitiveContent.class);
            response = new Response(responseStatusCode, requestIdentifier, primitiveContent, to, from, originatingTimestamp, resultExpirationTimestamp, eventCategory);
        }

        public Response build() {
            return response;
        }
    }
}
