package org.opendaylight.iotdm.client.impl;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;

/**
 * Created by ychau on 4/18/16.
 */
public class oneM2MCoapRequest extends org.eclipse.californium.core.coap.Request{
    private int port;

    public oneM2MCoapRequest(CoAP.Code code) {
            super(code);
        }

        public oneM2MCoapRequest(CoAP.Code code, CoAP.Type type) {
            super(code, type);
        }

        private void validateBeforeSending() {
            if (getDestination() == null)
                throw new NullPointerException("Destination is null");
            if (getDestinationPort() == 0)
                throw new NullPointerException("Destination port is 0");
        }

        public oneM2MCoapRequest send(Connector connector) {
            validateBeforeSending();
            port = getOptions().getUriPort();
            if (CoAP.COAP_SECURE_URI_SCHEME.equals(getScheme()) || port == 5684) {
                // This is the case when secure coap is supposed to be used
                EndpointManager.getEndpointManager().setDefaultSecureEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));
                EndpointManager.getEndpointManager().getDefaultSecureEndpoint().sendRequest(this);
            } else {
                // This is the normal case
                EndpointManager.getEndpointManager().getDefaultEndpoint().sendRequest(this);
            }
            return this;
        }

        public static oneM2MCoapRequest buildGet() { return new oneM2MCoapRequest(CoAP.Code.GET); }

        /**
         * Convenience factory method to construct a POST request and equivalent to
         * <code>new Request(Code.POST);</code>
         *
         * @return a new POST request
         */
        public static oneM2MCoapRequest buildPost() { return new oneM2MCoapRequest(CoAP.Code.POST); }

        /**
         * Convenience factory method to construct a PUT request and equivalent to
         * <code>new Request(Code.PUT);</code>
         *
         * @return a new PUT request
         */
        public static oneM2MCoapRequest buildPut() { return new oneM2MCoapRequest(CoAP.Code.PUT); }

        /**
         * Convenience factory method to construct a DELETE request and equivalent
         * to <code>new Request(Code.DELETE);</code>
         *
         * @return a new DELETE request
         */
        public static oneM2MCoapRequest buildDelete() { return new oneM2MCoapRequest(CoAP.Code.DELETE); }

}
