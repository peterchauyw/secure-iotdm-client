package org.opendaylight.iotdm.client.impl;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.onem2m.xml.protocols.Ae;
import org.opendaylight.iotdm.client.Request;
import org.opendaylight.iotdm.client.Response;
import org.opendaylight.iotdm.client.api.Client;
import org.opendaylight.iotdm.constant.OneM2M;

import java.util.concurrent.TimeUnit;


/**
 * Created by wenxshi on 11/3/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HttpIT {
    static Client client=new Http();
    ITCase itCase=new ITCase(client,RestConf.HOST,8282,1000);

//    @BeforeClass
//    public static void suitUp() {
//        ITCase.suitUp(RestConf.HOST);
//        client.start();
//    }
//
//    @AfterClass
//    public static void suitDown() {
//        client.stop();
//        ITCase.suitDown(RestConf.HOST);
//    }

    @Test
    public void test_1_create_ae() {itCase.create_ae(); }

    @Test
    public void test_2_update_ae() {
        itCase.update_ae();
    }

    @Test
    public void test_3_retrieve_ae() {
        itCase.retrieve_ae();
    }

    @Test
    public void test_4_delete_ae() {
        itCase.delete_ae();
    }

    @Test
    public void test_5_create_ae_without_request_mandatory_attribute_request_identifier_and_result_expect_error(){
        itCase.create_ae_without_mandatory_attribute_request_identifier_and_result_expect_error();
    }

    @Test
    public void test_6_create_ae_without_request_mandatory_attribute_from_and_result_expect_error(){
        itCase.create_ae_without_mandatory_attribute_from_and_result_expect_error();
    }

    @Test
    public void test_7_create_ae_without_mandatory_attribute_result_type_and_result_expect_error(){
        itCase.create_ae_without_mandatory_attribute_resource_type_and_result_expect_error();
    }

    @Test
    public void test_8_create_ae_with_wrong_resource_type_and_result_expect_error(){
       itCase.create_ae_with_wrong_resource_type_and_result_expect_error();
    }

    @Test
    public void test_9_update_ae_with_resource_type_and_result_expect_error(){
        itCase.update_ae_with_resource_type_and_result_expect_error();
    }

    @Test
    public void createAE() {
        Client client = new Http();
        //For coap, please uncomment code below
        //Client client=new Coap();
        Request request = new Request()
                .host("localhost")
                .to("ODL-oneM2M-Cse")
                .operation(OneM2M.Operation.CREATE)
                .resourceType(OneM2M.ResourceType.AE)
                .from("localhost")
                .requestIdentifier("1234")
                .name("AE");

        //For Http, uncomment below
        request.port(8282);

        //For coap, uncomment below
        //request.port(5683);

        //Add the data want to create
        Ae ae = new Ae();
        ae.setOr("http://ontology/ref");
        ae.setRr(true);
        ae.setApi("testAppId");
        ae.setApn("testAppName");

        request.addPrimitiveContent(ae);
        client.start();
        Response response = client.send(request);
        System.out.println(response);
        client.stop();
    }

    @Test
    public void SecureCreateAE() {
        Client client = new Http(true);
        //For coap, please uncomment code below
//        Client client=new Coap();
        Request request = new Request()
                .host("localhost")
                .to("ODL-oneM2M-Cse")
                .operation(OneM2M.Operation.CREATE)
                .resourceType(OneM2M.ResourceType.AE)
                .from("localhost")
                .requestIdentifier("1234")
                .name("AE");

        //For Http, uncomment below
        request.port(8443);

        //For coap, uncomment below
        //request.port(5683);

        //Add the data want to create
        Ae ae = new Ae();
        ae.setOr("http://ontology/ref");
        ae.setRr(true);
        ae.setApi("testAppId");
        ae.setApn("testAppName");

        request.addPrimitiveContent(ae);
        client.start();
        request.timeout(5000, TimeUnit.MILLISECONDS);
        Response response = client.send(request);
        System.out.println(response);
        client.stop();
    }

}
