package org.folio.impl;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

@RunWith(VertxUnitRunner.class)
public class EventConfigAPIsTest {

  private static final String HTTP_PORT = "http.port";
  private static final String REST_PATH = "/eventConfig";
  private static final String OKAPI_TENANT_VAL = "test_tenant";
  private static final String OKAPI_TOKEN_VAL = "test_token";
  private static final String PATH_TEMPLATE = "%s/%s";

  private static Headers headers;
  private static Vertx vertx;
  private static int port;

  @BeforeClass
  public static void setUpClass(final TestContext context) {
    Async async = context.async();
    vertx = Vertx.vertx();
    port = NetworkUtils.nextFreePort();
    headers = new Headers(new Header(OKAPI_HEADER_TENANT, OKAPI_TENANT_VAL), new Header(OKAPI_HEADER_TOKEN, OKAPI_TOKEN_VAL));

    DeploymentOptions restDeploymentOptions = new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, port));
    vertx.deployVerticle(RestVerticle.class.getName(), restDeploymentOptions, res -> async.complete());
  }

  @AfterClass
  public static void tearDownClass(final TestContext context) {
    Async async = context.async();
    vertx.close(context.asyncAssertSuccess(res -> async.complete()));
  }

  @Test
  public void testFullRestAPI() {
    String id = UUID.randomUUID().toString();
    JsonObject expectedEntity = getJsonEntity(id, "test", true, new JsonArray());

    // create a new event config
    requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_OK);

    // get the event config by id
    Response responseGetEvent = requestGetEventById(id)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    JsonObject actualEntity = new JsonObject(responseGetEvent.getBody().print());
    assertEquals(expectedEntity, actualEntity);

    // update the event config
    expectedEntity = getJsonEntity(id, "new name", false, new JsonArray());
    Response responsePutEvent = requestPutEventConfig(id, expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    actualEntity = new JsonObject(responsePutEvent.getBody().print());
    assertEquals(expectedEntity, actualEntity);

    // check updated event config
    responseGetEvent = requestGetEventById(id)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    actualEntity = new JsonObject(responseGetEvent.getBody().print());
    assertEquals(expectedEntity, actualEntity);

    // delete the event config by id
    requestDeleteEntityById(id)
      .then()
      .statusCode(HttpStatus.SC_OK);

    // check that the event config has been deleted
    requestGetEventById(id)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void testPostEventConfig() {
    String id = UUID.randomUUID().toString();
    JsonObject expectedEntity = getJsonEntity(id, "test name", false, new JsonArray());

    Response response = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();

    JsonObject actualEntity = new JsonObject(response.getBody().print());
    assertEquals(expectedEntity, actualEntity);

    requestDeleteEntityById(id)
      .then()
      .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void testGetEventConfigById() {
    String id = UUID.randomUUID().toString();
    requestGetEventById(id)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void testPutEventConfigById() {
    String id = UUID.randomUUID().toString();
    JsonObject entity = getJsonEntity("0002", "test", true, new JsonArray());

    requestPutEventConfig(id, entity)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void testDeleteEventConfigById() {
    String id = UUID.randomUUID().toString();
    requestDeleteEntityById(id)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  private JsonObject getJsonEntity(String id, String name, boolean active, JsonArray templates) {
    return new JsonObject()
      .put("id", id)
      .put("name", name)
      .put("active", active)
      .put("templates", templates);
  }

  private Response requestPostEventConfig(JsonObject expectedEntity) {
    return RestAssured.given()
      .port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers)
      .body(expectedEntity.toString())
      .when()
      .post(REST_PATH);
  }

  private Response requestGetEventById(String expectedId) {
    return RestAssured.given()
      .port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers)
      .when()
      .get(String.format(PATH_TEMPLATE, REST_PATH, expectedId));
  }

  private Response requestPutEventConfig(String id, JsonObject entity) {
    return RestAssured.given().port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers)
      .body(entity.toString())
      .when()
      .put(String.format(PATH_TEMPLATE, REST_PATH, id));
  }

  private Response requestDeleteEntityById(String id) {
    return RestAssured.given()
      .port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers)
      .when()
      .delete(String.format(PATH_TEMPLATE, REST_PATH, id));
  }
}
