package org.folio.impl;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.postgres.testing.PostgresTesterContainer;
import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.jaxrs.model.EventConfigCollection;
import org.folio.rest.jaxrs.model.Template;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.jaxrs.model.TenantJob;
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
import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(VertxUnitRunner.class)
public class EventConfigAPIsTest {

  private static final String TENANT_ID = "diku";
  private static final String HTTP_PORT = "http.port";
  private static final String OKAPI_TOKEN_VAL = "test_token";
  private static final String OKAPI_URL = "http://localhost:%s";
  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String SNAPSHOTS_TABLE_NAME = "event_configurations";
  private static final String EXTERNAL_DATABASE_VAL = "embedded";

  private static final String EXPECTED_ERROR_MESSAGE = "Not found";
  private static final Logger logger = LogManager.getLogger(EventConfigAPIsTest.class);

  private static String restPath;
  private static RequestSpecification request;
  private static RequestSpecification requestGet;
  private static Vertx vertx;
  private static String useExternalDatabase;
  private static int port;

  @Rule
  public Timeout timeout = Timeout.seconds(200);

  @BeforeClass
  public static void setUpClass(final TestContext context) {
    Async async = context.async();
    vertx = Vertx.vertx();
    port = NetworkUtils.nextFreePort();
    Headers headers = new Headers(
      new Header(OKAPI_HEADER_TENANT, TENANT_ID),
      new Header(OKAPI_HEADER_TOKEN, OKAPI_TOKEN_VAL));
    restPath = System.getProperty("org.folio.event.config.rest.path", "/eventConfig");
    useExternalDatabase = System.getProperty("org.folio.event.config.test.database", EXTERNAL_DATABASE_VAL);
    request = RestAssured.given()
      .port(port)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(headers);
    requestGet = RestAssured.given()
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
        PostgresClient.setPostgresTester(new PostgresTesterContainer());
        break;
      default:
        String message = "No understood database choice made." +
          "Please set org.folio.source.storage.test.database" +
          "to 'external', 'environment' or 'embedded'";
        throw new IllegalArgumentException(message);
    }

    TenantClient tenantClient = new TenantClient(String.format(OKAPI_URL, port), TENANT_ID, OKAPI_TOKEN_VAL);
    DeploymentOptions restDeploymentOptions = new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, port));
    vertx.deployVerticle(RestVerticle.class.getName(), restDeploymentOptions)
      .onComplete(res -> {
        try {
          TenantAttributes t = new TenantAttributes().withModuleTo("mod-event-config-1.0.0");
          tenantClient.postTenant(t, postResult -> {
            if (postResult.failed()) {
              Throwable cause = postResult.cause();
              logger.error(cause);
              context.fail(cause);
              return;
            }

            final HttpResponse<Buffer> postResponse = postResult.result();
            assertThat(postResponse.statusCode(), is(201));

            String jobId = postResponse.bodyAsJson(TenantJob.class).getId();

            tenantClient.getTenantByOperationId(jobId, 10000, getResult -> {
              if (getResult.failed()) {
                Throwable cause = getResult.cause();
                logger.error(cause.getMessage());
                context.fail(cause);
                return;
              }

              final HttpResponse<Buffer> getResponse = getResult.result();
              assertThat(getResponse.statusCode(), is(200));
              assertThat(getResponse.bodyAsJson(TenantJob.class).getComplete(), is(true));
              async.complete();
            });
          });
        } catch (Exception e) {
          // none
        }
      });
  }

  @AfterClass
  public static void tearDownClass(final TestContext context) {
    Async async = context.async();
    vertx.close()
      .onComplete(context.asyncAssertSuccess(res -> {
        if (useExternalDatabase.equals(EXTERNAL_DATABASE_VAL)) {
          PostgresClient.stopPostgresTester();
        }
        async.complete();
      }));
  }

  @Before
  public void setUp(TestContext context) {
    Async async = context.async();
    PostgresClient.getInstance(vertx, TENANT_ID).delete(SNAPSHOTS_TABLE_NAME, new Criterion(), event -> {
      if (event.failed()) {
        logger.error(event.cause());
        context.fail(event.cause());
      } else {
        async.complete();
      }
    });
  }

  @Test
  public void testFullRestAPI() {
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), "RESET_PASSWORD",
      true, new JsonArray());

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
    requestPutEventConfig(actualId, expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void testPostEventConfig() {
    String id = UUID.randomUUID().toString();
    JsonObject expectedEntity = getJsonEntity(id, "RESET_PASSWORD", false, new JsonArray());

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

    requestDeleteEntityById(actualId)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void testPostHtmlEventConfig() {
    JsonArray templates = createTemplates("email", "text/html");
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), "new_pwd", true, templates);
    expectedEntity.put("name", "NEW_PWD");

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
    assertEquals("text/plain", expected);
  }

  @Test
  public void testGetEventEntries() {
    EventConfigCollection eventEntries = new EventConfigCollection().withTotalRecords(0);
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
    requestPostEventConfig(getJsonEntity(null, secure().nextAlphabetic(10),
      false, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    requestPostEventConfig(getJsonEntity(null, secure().nextAlphabetic(20),
      true, new JsonArray()))
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

    String actualMessage = response.asString();
    assertEquals(EXPECTED_ERROR_MESSAGE, actualMessage);
  }

  @Test
  public void testPutEventConfigById() {
    String id = UUID.randomUUID().toString();
    JsonObject entity = getJsonEntity("0002", secure().nextAlphabetic(20),
      true, new JsonArray());

    Response response = requestPutEventConfig(id, entity)
      .then()
      .statusCode(HttpStatus.SC_NOT_FOUND)
      .extract()
      .response();

    String actualMessage = response.asString();
    assertEquals(EXPECTED_ERROR_MESSAGE, actualMessage);
  }

  @Test
  public void testDeleteEventConfigById() {
    String id = UUID.randomUUID().toString();
    Response response = requestDeleteEntityById(id)
      .then()
      .statusCode(HttpStatus.SC_NOT_FOUND)
      .extract()
      .response();

    String actualMessage = response.asString();
    assertEquals(EXPECTED_ERROR_MESSAGE, actualMessage);
  }

  @Test
  public void testUniqueNameOfConfiguration() {
    String configName = "RESET_PASSWORD";
    requestPostEventConfig(getJsonEntity(UUID.randomUUID().toString(), configName,
      false, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    Response response = requestPostEventConfig(getJsonEntity(UUID.randomUUID().toString(), configName,
      false, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST)
      .extract()
      .response();
    String body = response.getBody().print();
    assertTrue(body.contains("value already exists in table"));
  }

  @Test
  public void testGetEventConfigByName() {
    String configName = "CREATE_PASSWORD";
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), configName, true, new JsonArray());

    String responseBody = requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response()
      .getBody().print();
    String actualId = new JsonObject(responseBody).getString("id");
    expectedEntity.put("id", actualId);

    requestPostEventConfig(getJsonEntity(UUID.randomUUID().toString(), "NEW", true, new JsonArray()))
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    Response response = requestGetEventByName(configName)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();

    JsonObject eventEntries = new JsonObject(response.getBody().print());
    int totalRecords = eventEntries.getInteger("totalRecords");
    assertEquals(1, totalRecords);
    assertEquals(expectedEntity, eventEntries.getJsonArray("eventEntity").getJsonObject(0));
  }

  @Test
  public void testGetEventConfigByIncorrectName() {
    String configName = "password";
    JsonObject expectedEntity = getJsonEntity(UUID.randomUUID().toString(), configName, true, new JsonArray());

    requestPostEventConfig(expectedEntity)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .response();

    Response response = requestGetEventByName("INCORRECT")
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .response();

    JsonObject actualEntries = new JsonObject(response.getBody().print());
    int totalRecords = actualEntries.getInteger("totalRecords");
    assertEquals(0, totalRecords);
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

  @Test
  public void testInternalServerError() {
    RestAssured.given()
      .port(port)
      .header(OKAPI_HEADER_TENANT, "invalid-tenant")
      .when()
      .get(restPath)
      .then()
      .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testInvalidSQL() {
    requestGet
      .get(restPath + "?query=invalid_sql")
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
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
    return requestGet
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

  private Response requestGetEventByName(String name) {
    return request
      .when()
      .get(String.format(PATH_TEMPLATE, restPath, "?query=name==" + name));
  }

  private JsonArray createTemplates(String deliveryChannel, String outputFormat) {
    Template template = new Template()
      .withTemplateId(UUID.randomUUID().toString())
      .withDeliveryChannel(deliveryChannel)
      .withOutputFormat(outputFormat);
    return new JsonArray().add(JsonObject.mapFrom(template));
  }
}
