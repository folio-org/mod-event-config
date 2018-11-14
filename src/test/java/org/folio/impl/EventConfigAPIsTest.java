package org.folio.impl;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.jaxrs.model.EventEntries;
import org.folio.rest.jaxrs.model.Template;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;
import java.util.Objects;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;

@RunWith(VertxUnitRunner.class)
public class EventConfigAPIsTest {

  private static final String TENANT_ID = "diku";
  private static final String HTTP_PORT = "http.port";
  private static final String OKAPI_TOKEN_VAL = "test_token";
  private static final String OKAPI_URL = "http://localhost:%s";
  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String SNAPSHOTS_TABLE_NAME = "event_configurations";
  private static final String RESPONSE_KEY = "message";
  private static final String EXTERNAL_DATABASE_VAL = "embedded";

  private static final String EXPECTED_ERROR_MESSAGE = "Event Config with ID: `%s` was not found in the db";
  private static final Logger logger = LoggerFactory.getLogger(EventConfigAPIsTest.class);

  private static String restPath;
  private static RequestSpecification request;
  private static Vertx vertx;
  private static String useExternalDatabase;

  @Rule
  public Timeout timeout = Timeout.seconds(200);

  @BeforeClass
  public static void setUpClass(final TestContext context) throws Exception {
    Async async = context.async();
    vertx = Vertx.vertx();
    int port = NetworkUtils.nextFreePort();
    Headers headers = new Headers(
      new Header(OKAPI_HEADER_TENANT, TENANT_ID),
      new Header(OKAPI_HEADER_TOKEN, OKAPI_TOKEN_VAL));
    restPath = System.getProperty("org.folio.event.config.rest.path", "/eventConfig");
    useExternalDatabase = System.getProperty("org.folio.event.config.test.database", EXTERNAL_DATABASE_VAL);
    request = RestAssured.given()
      .port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers);

    switch (useExternalDatabase) {
      case "environment":
        logger.debug("Using environment settings");
        break;
      case "external":
        String postgresConfigPath = System.getProperty(
          "org.folio.event.config.test.config",
          "/postgres-conf-local.json");
        PostgresClient.setConfigFilePath(postgresConfigPath);
        break;
      case EXTERNAL_DATABASE_VAL:
        PostgresClient.setIsEmbedded(true);
        PostgresClient.getInstance(vertx).startEmbeddedPostgres();
        break;
      default:
        String message = "No understood database choice made." +
          "Please set org.folio.source.storage.test.database" +
          "to 'external', 'environment' or 'embedded'";
        throw new IllegalArgumentException(message);
    }

    TenantClient tenantClient = new TenantClient(String.format(OKAPI_URL, port), TENANT_ID, OKAPI_TOKEN_VAL);
    DeploymentOptions restDeploymentOptions = new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, port));
    vertx.deployVerticle(RestVerticle.class.getName(), restDeploymentOptions, res -> {
      try {
        tenantClient.postTenant(null, res2 -> async.complete());
      } catch (Exception e) {
        // none
      }
    });
  }

  @AfterClass
  public static void tearDownClass(final TestContext context) {
    Async async = context.async();
    vertx.close(context.asyncAssertSuccess(res -> {
      if (useExternalDatabase.equals(EXTERNAL_DATABASE_VAL)) {
        PostgresClient.stopEmbeddedPostgres();
      }
      async.complete();
    }));
  }


  @Before
  public void setUp(TestContext context) {
    PostgresClient.getInstance(vertx, TENANT_ID).delete(SNAPSHOTS_TABLE_NAME, new Criterion(), event -> {
      if (event.failed()) {
        logger.error(event.cause());
        context.fail(event.cause());
      }
    });
  }

  @Test
  public void testFullRestAPI() {
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), "test", true, new JsonArray());

    // create a new event config
    Response response = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response();
    JsonObject actualEntity = new JsonObject(response.getBody().print());
    String actualId = actualEntity.getString("id");
    expectedEntity.put("id", actualId);

    // get the event config by id
    Response responseGet = requestGetEventById(actualId)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    JsonObject actualEntityGet = new JsonObject(responseGet.getBody().print());
    assertEquals(expectedEntity, actualEntityGet);

    // update the event config
    Response responsePut = requestPutEventConfig(actualId, expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    JsonObject expectedEntityPut = new JsonObject(responsePut.getBody().print());
    assertEquals(expectedEntity, expectedEntityPut);
  }

  @Test
  public void testPostEventConfig() {
    String id = UUID.randomUUID().toString();
    JsonObject expectedEntity = getJsonEntity(id, "test name", false, new JsonArray());
    String expectedDeleteMessage = "Configuration with id: '%s' was successfully deleted";

    // create a new event config
    Response response = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response();

    JsonObject actualEntity = new JsonObject(response.getBody().print());
    String actualId = actualEntity.getString("id");
    expectedEntity.put("id", actualId);
    assertEquals(expectedEntity, actualEntity);

    Response responseDelete = requestDeleteEntityById(actualId)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();
    String actualMessage = getResponseMessage(responseDelete);
    assertEquals(String.format(expectedDeleteMessage, actualId), actualMessage);
  }

  @Test
  public void testPostHtmlEventConfig() {
    JsonArray templates = createTemplates("email", "text/html");
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), "test", true, templates);

    // create a new event config
    Response response = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response();

    JsonObject actualEntity = new JsonObject(response.getBody().print());
    String actualId = actualEntity.getString("id");
    expectedEntity.put("id", actualId);
    assertEquals(expectedEntity, actualEntity);
  }

  @Test
  public void testPostTextEventConfig() {
    JsonArray templates = createTemplates("sms", "text/plain");
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), "test", true, templates);

    // create a new event config
    Response response = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response();

    String expected = new JsonObject(response.getBody().print())
      .getJsonArray("templates")
      .getJsonObject(0)
      .getString("outputFormat");
    assertEquals(expected, "text/plain");
  }

  @Test
  public void testGetEventEntries() {
    EventEntries eventEntries = new EventEntries().withTotalRecords(0);
    JsonObject expectedEntriesJson = JsonObject.mapFrom(eventEntries);

    Response response = requestGetEventEntries()
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();

    JsonObject actualEntries = new JsonObject(response.getBody().print());
    assertEquals(expectedEntriesJson, actualEntries);
  }

  @Test
  public void testGetAllEventEntries() {
    requestPostEventConfig(getJsonEntity(null, RandomStringUtils.randomAlphabetic(10), false, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_CREATED);
    requestPostEventConfig(getJsonEntity(null, RandomStringUtils.randomAlphabetic(20), false, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    Response response = requestGetEventEntries()
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();

    JsonObject actualEntries = new JsonObject(response.getBody().print());
    int totalRecords = actualEntries.getInteger("totalRecords");
    assertEquals(2, totalRecords);
  }

  @Test
  public void testGetEventConfigById() {
    String id = UUID.randomUUID().toString();
    Response response = requestGetEventById(id)
      .then()
      .statusCode(HttpStatus.SC_NOT_FOUND)
      .extract()
      .response();

    String actualMessage = getResponseMessage(response);
    assertEquals(String.format(EXPECTED_ERROR_MESSAGE, id), actualMessage);
  }

  @Test
  public void testPutEventConfigById() {
    String id = UUID.randomUUID().toString();
    JsonObject entity = getJsonEntity("0002", RandomStringUtils.randomAlphabetic(20), true, new JsonArray());

    Response response = requestPutEventConfig(id, entity)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST)
      .extract()
      .response();

    String actualMessage = getResponseMessage(response);
    assertEquals(String.format(EXPECTED_ERROR_MESSAGE, id), actualMessage);
  }

  @Test
  public void testDeleteEventConfigById() {
    String id = UUID.randomUUID().toString();
    Response response = requestDeleteEntityById(id)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST)
      .extract()
      .response();

    String actualMessage = getResponseMessage(response);
    assertEquals(String.format(EXPECTED_ERROR_MESSAGE, id), actualMessage);
  }

  private JsonObject getJsonEntity(String id, String name, boolean active, JsonArray templates) {
    JsonObject entries = new JsonObject();
    if (!Objects.isNull(id)) {
      entries.put("id", id);
    }
    return entries
      .put("name", name)
      .put("active", active)
      .put("templates", templates);
  }

  private Response requestPostEventConfig(JsonObject expectedEntity) {
    return request.body(expectedEntity.toString())
      .when()
      .post(restPath);
  }

  private Response requestGetEventById(String expectedId) {
    return request
      .when()
      .get(String.format(PATH_TEMPLATE, restPath, expectedId));
  }

  private Response requestGetEventEntries() {
    return request
      .when()
      .get(restPath);
  }

  private Response requestPutEventConfig(String id, JsonObject entity) {
    return request.body(entity.toString())
      .when()
      .put(String.format(PATH_TEMPLATE, restPath, id));
  }

  private Response requestDeleteEntityById(String id) {
    return request.when()
      .delete(String.format(PATH_TEMPLATE, restPath, id));
  }

  private String getResponseMessage(Response response) {
    return new JsonObject(response.getBody().print()).getString(RESPONSE_KEY);
  }

  private JsonArray createTemplates(String deliveryChannel, String outputFormat) {
    Template template = new Template()
      .withTemplateId(UUID.randomUUID().toString())
      .withDeliveryChannel(deliveryChannel)
      .withOutputFormat(outputFormat);
    return new JsonArray().add(JsonObject.mapFrom(template));
  }
}
