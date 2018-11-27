package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventEntries;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.rest.jaxrs.model.Template;
import org.folio.rest.jaxrs.resource.EventConfig;
import org.folio.services.StorageService;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.folio.util.EventConfigUtils.*;

public class EventConfigAPIs implements EventConfig {

  private static final String ERROR_RUNNING_VERTICLE = "Error running on verticle for `%s`: %s";
  private static final String ERROR_EVENT_CONFIG_NOT_FOUND = "Event Config with ID: `%s` was not found in the db";

  private final Logger logger = LoggerFactory.getLogger(EventConfigAPIs.class);

  private StorageService service;
  private String tenantId;

  public EventConfigAPIs(Vertx vertx, String tenantId) {
    this.tenantId = tenantId;
    initStorageService(vertx);
  }

  private void initStorageService(Vertx vertx) {
    this.service = StorageService.createProxy(vertx, EVENT_CONFIG_PROXY_ADDRESS);
  }

  /**
   * Create new event config
   *
   * @param entity       the event configuration {@link EventEntity}
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler}
   *                     which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void postEventConfig(EventEntity entity, Map<String, String> headers,
                              Handler<AsyncResult<Response>> asyncHandler, Context context) {
    try {
      List<Template> templates = entity.getTemplates();
      templates.forEach(template -> template.setOutputFormat(findOutputFormat(template)));
      eventNameToUpperCase(entity);
      JsonObject entityJson = JsonObject.mapFrom(entity);
      context.runOnContext(contextHandler -> service.createEventConfig(tenantId, entityJson, serviceHandler -> {
        if (serviceHandler.failed()) {
          String errorMessage = serviceHandler.cause().getMessage();
          asyncHandler.handle(createFutureResponse(PostEventConfigResponse.respond400WithTextPlain(errorMessage)));
          return;
        }

        EventEntity eventEntity = getResponseEntity(serviceHandler, EventEntity.class);
        asyncHandler.handle(createFutureResponse(PostEventConfigResponse.respond201WithApplicationJson(eventEntity)));
      }));
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_RUNNING_VERTICLE, "createEventConfig", ex.getMessage());
      logger.error(errorMessage, ex);
      asyncHandler.handle(createFutureResponse(PostEventConfigResponse.respond500WithTextPlain(errorMessage)));
    }
  }

  /**
   * Get all event configs
   *
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler}
   *                     which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void getEventConfig(String query, Map<String, String> okapiHeaders,
                             Handler<AsyncResult<Response>> asyncHandler, Context context) {
    try {
      context.runOnContext(contextHandler -> service.findAllEventConfigurations(tenantId, query,
        serviceHandler -> {
          if (serviceHandler.failed()) {
            String errorMessage = serviceHandler.cause().getMessage();
            asyncHandler.handle(createFutureResponse(
              GetEventConfigResponse.respond400WithTextPlain(errorMessage)));
            return;
          }
          EventEntries responseEntries = getResponseEntity(serviceHandler, EventEntries.class);
          asyncHandler.handle(createFutureResponse(
            GetEventConfigResponse.respond200WithApplicationJson(responseEntries)));
        }));
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_RUNNING_VERTICLE, "findAllEventConfigurations", ex.getMessage());
      logger.error(errorMessage, ex);
      asyncHandler.handle(createFutureResponse(
        GetEventConfigResponse.respond500WithTextPlain(errorMessage)));
    }
  }

  /**
   * Get event config by id
   *
   * @param id           the configuration event ID
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler}
   *                     which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void getEventConfigById(String id, Map<String, String> headers,
                                 Handler<AsyncResult<Response>> asyncHandler, Context context) {
    try {
      context.runOnContext(contextHandler -> service.findEventConfigById(tenantId, id, serviceHandler -> {
        if (serviceHandler.failed()) {
          String errorMessage = serviceHandler.cause().getMessage();
          asyncHandler.handle(createFutureResponse(GetEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
          return;
        }
        JsonObject jsonObject = serviceHandler.result();
        Boolean isNotFound = Optional.ofNullable(jsonObject.getBoolean(VALUE_IS_NOT_FOUND)).orElse(false);
        if (isNotFound) {
          String message = String.format(ERROR_EVENT_CONFIG_NOT_FOUND, id);
          logger.debug(message);
          EventResponse eventResponse = new EventResponse().withMessage(message);
          asyncHandler.handle(createFutureResponse(
            GetEventConfigByIdResponse.respond404WithApplicationJson(eventResponse)));
          return;
        }

        EventEntity eventEntity = getResponseEntity(serviceHandler, EventEntity.class);
        asyncHandler.handle(createFutureResponse(GetEventConfigByIdResponse.respond200WithApplicationJson(eventEntity)));
      }));
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_RUNNING_VERTICLE, "findEventConfigById", ex.getMessage());
      logger.error(errorMessage, ex);
      asyncHandler.handle(createFutureResponse(GetEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
    }
  }

  /**
   * Update event config
   *
   * @param id           the configuration event ID
   * @param entity       the event configuration {@link EventEntity}
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler}
   *                     which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void putEventConfigById(String id, EventEntity entity, Map<String, String> headers,
                                 Handler<AsyncResult<Response>> asyncHandler, Context context) {
    try {
      eventNameToUpperCase(entity);
      final JsonObject entityJson = JsonObject.mapFrom(entity);
      context.runOnContext(contextHandler -> service.updateEventConfig(tenantId, id, entityJson, serviceHandler -> {
        if (serviceHandler.failed()) {
          String errorMessage = serviceHandler.cause().getMessage();
          asyncHandler.handle(createFutureResponse(PutEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
          return;
        }
        if (Objects.isNull(serviceHandler.result())) {
          String message = String.format(ERROR_EVENT_CONFIG_NOT_FOUND, id);
          logger.debug(message);
          EventResponse eventResponse = new EventResponse().withMessage(message);
          asyncHandler.handle(createFutureResponse(
            PutEventConfigByIdResponse.respond400WithApplicationJson(eventResponse)));
          return;
        }

        EventEntity eventEntity = getResponseEntity(serviceHandler, EventEntity.class);
        asyncHandler.handle(createFutureResponse(
          PutEventConfigByIdResponse.respond200WithApplicationJson(eventEntity)));
      }));
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_RUNNING_VERTICLE, "updateEventConfig", ex.getMessage());
      logger.error(errorMessage, ex);
      asyncHandler.handle(createFutureResponse(PutEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
    }
  }

  /**
   * Delete event config
   *
   * @param id           the configuration event ID
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler}
   *                     which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void deleteEventConfigById(String id, Map<String, String> headers,
                                    Handler<AsyncResult<Response>> asyncHandler, Context context) {
    try {
      context.runOnContext(contextHandler -> service.deleteEventConfigById(tenantId, id, serviceHandler ->
        {
          if (serviceHandler.failed()) {
            String errorMessage = serviceHandler.cause().getMessage();
            asyncHandler.handle(createFutureResponse(
              DeleteEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
            return;
          }
          if (Objects.isNull(serviceHandler.result())) {
            String message = String.format(ERROR_EVENT_CONFIG_NOT_FOUND, id);
            logger.debug(message);
            EventResponse eventResponse = new EventResponse().withMessage(message);
            asyncHandler.handle(createFutureResponse(
              DeleteEventConfigByIdResponse.respond400WithApplicationJson(eventResponse)));
            return;
          }

          EventResponse eventResponse = getResponseEntity(serviceHandler, EventResponse.class);
          asyncHandler.handle(createFutureResponse(
            DeleteEventConfigByIdResponse.respond200WithApplicationJson(eventResponse)));
        }
      ));
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_RUNNING_VERTICLE, "deleteEventConfigById", ex.getMessage());
      logger.error(errorMessage, ex);
      asyncHandler.handle(createFutureResponse(DeleteEventConfigByIdResponse.respond500WithTextPlain(errorMessage)));
    }
  }

  private String findOutputFormat(Template template) {
    String outputFormat = template.getOutputFormat();
    if (StringUtils.isBlank(outputFormat)) {
      return TEXT_PLAIN;
    }
    if (outputFormat.trim().equalsIgnoreCase(TEXT_HTML)) {
      return TEXT_HTML;
    }
    return TEXT_PLAIN;
  }

  private void eventNameToUpperCase(EventEntity entity) {
    String configName = entity.getName().trim().toUpperCase();
    entity.setName(configName);
  }
}
