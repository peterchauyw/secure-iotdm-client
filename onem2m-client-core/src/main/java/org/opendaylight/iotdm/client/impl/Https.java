package org.opendaylight.iotdm.client.impl;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.opendaylight.iotdm.client.Request;
import org.opendaylight.iotdm.client.Response;
import org.opendaylight.iotdm.client.exception.Onem2mNoOperationError;

import org.opendaylight.iotdm.constant.OneM2M;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Created by Peter Chau on 07/20/16.
 */
public class Https extends Http {

    private static final String TRUST_STORE_PASSWORD = "storepwd";
    private static final String KEY_STORE_PASSWORD = "storepwd";
    private static final String KEY_STORE_LOCATION = "src/test/java/org/opendaylight/iotdm/client/certs/jettykeystore";
    private static final String TRUST_STORE_LOCATION = "src/test/java/org/opendaylight/iotdm/client/certs/jettykeystore";

    public Https(){
            SslContextFactory sslContextFactory = new SslContextFactory(KEY_STORE_LOCATION);
            sslContextFactory.setEndpointIdentificationAlgorithm("");
            sslContextFactory.setKeyStorePassword(KEY_STORE_PASSWORD);
            sslContextFactory.setTrustStorePath(TRUST_STORE_LOCATION);
            sslContextFactory.setTrustStorePassword(TRUST_STORE_PASSWORD);
            sslContextFactory.setKeyManagerPassword("keypwd");
            QueuedThreadPool clientThreads = new QueuedThreadPool();
            clientThreads.setName("client");
            httpClient = new HttpClient(sslContextFactory);
            httpClient.setExecutor(clientThreads);

    }
    @Override
    public Response send(Request request) {

        org.eclipse.jetty.client.api.Request httpRequest = new HttpRequestBuilder(request).build();

        ContentResponse contentResponse;
        try {
            contentResponse = httpRequest.send();
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }

        Response response = new ResponseBuilder(contentResponse).build();
        return response;
    }

    public static class HttpRequestBuilder {
        org.eclipse.jetty.client.api.Request httpRequest;

        public HttpRequestBuilder(Request request) {
            if (request == null) return;

            RequestHelper requestHelper = new RequestHelper(request);
            httpRequest = httpClient.newRequest(requestHelper.getHost(), requestHelper.getPort())
                    .timeout(requestHelper.getTimeout(), TimeUnit.MILLISECONDS);

            requestHelper.getQuery().remove(OneM2M.Name.RESOURCE_TYPE);
            addQuery(httpRequest, requestHelper.getQuery());
            addHeader(httpRequest.getHeaders(), requestHelper.getHeader());
            httpRequest.accept(requestHelper.getAcceptMIME());
            httpRequest.path(OneM2M.Path.toToPathMapping(requestHelper.getPath()));
            httpRequest.scheme(HttpScheme.HTTPS.toString());

            switch (OneM2M.Operation.getEnum(requestHelper.getOp())) {
                case CREATE:
                    httpRequest.method(CREATE_IN_HTTP);
                    httpRequest.content(new StringContentProvider(requestHelper.getPayload()));
                    httpRequest.header(OneM2M.Http.Header.CONTENT_TYPE, String.format("%s;%s=%s", requestHelper.getContentMIME(), OneM2M.Name.RESOURCE_TYPE, request.getRequestPrimitive().getTy()));
                    break;
                case RETRIEVE:
                    httpRequest.method(RETRIEVE_IN_HTTP);
                    httpRequest.header(OneM2M.Http.Header.CONTENT_TYPE, requestHelper.getContentMIME());
                    break;
                case UPDATE:
                    httpRequest.method(UPDATE_IN_HTTP);
                    httpRequest.content(new StringContentProvider(requestHelper.getPayload()));
                    httpRequest.header(OneM2M.Http.Header.CONTENT_TYPE, requestHelper.getContentMIME());
                    break;
                case DELETE:
                    httpRequest.method(DELETE_IN_HTTP);
                    httpRequest.header(OneM2M.Http.Header.CONTENT_TYPE, requestHelper.getContentMIME());
                    break;
                case NOTIFY:
                    httpRequest.method(NOTIFY_IN_HTTP);
                    httpRequest.header(OneM2M.Http.Header.CONTENT_TYPE, requestHelper.getContentMIME());
                    break;
                default:
                    throw new Onem2mNoOperationError();
            }
        }

        private void addHeader(HttpFields httpFields, Map<String, Set<String>> map) {
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                String key = OneM2M.Http.Header.map(entry.getKey());
                String value = RequestHelper.concatQuery(entry.getValue());
                httpFields.add(key, value);
            }
        }

        private void addQuery(org.eclipse.jetty.client.api.Request request, Map<String, Set<String>> map) {
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = RequestHelper.concatQuery(entry.getValue());
                request.param(key, value);
            }
        }

        public org.eclipse.jetty.client.api.Request build() {
            return httpRequest;
        }
    }

}

