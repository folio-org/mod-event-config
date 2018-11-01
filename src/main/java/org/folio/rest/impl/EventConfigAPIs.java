package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.rest.jaxrs.resource.EventConfig;
import org.folio.services.StorageService;
import org.folio.services.impl.StubStorageServiceImpl;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Objects;

import static org.folio.util.EventConfigUtils.createFutureResponse;

public class EventConfigAPIs implements EventConfig {

  private static final String EVENT_ENTITY_NOT_FOUND = "EventEntity not found!";
  private final Logger logger = LoggerFactory.getLogger(EventConfigAPIs.class);
  private StorageService service;

  public EventConfigAPIs() {
    this.service = new StubStorageServiceImpl();
  }

  /**
   * Create new event config
   *
   * @param entity       the event configuration {@link EventEntity}
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler} which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void postEventConfig(EventEntity entity, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    EventEntity eventEntity = service.save(entity);
    asyncHandler.handle(createFutureResponse(PostEventConfigResponse.respond200WithApplicationJson(eventEntity)));
  }

  /**
   * Get event config by id
   *
   * @param id           the configuration event ID
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler} which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void getEventConfigById(String id, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    EventEntity eventEntity = service.findById(id);
    if (Objects.isNull(eventEntity)) {
      logger.error(EVENT_ENTITY_NOT_FOUND);
      asyncHandler.handle(createFutureResponse(GetEventConfigByIdResponse.respond400WithTextPlain(EVENT_ENTITY_NOT_FOUND)));
      return;
    }
    asyncHandler.handle(createFutureResponse(GetEventConfigByIdResponse.respond200WithApplicationJson(eventEntity)));
  }

  /**
   * Update event config
   *
   * @param id           the configuration event ID
   * @param entity       the event configuration {@link EventEntity}
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler} which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void putEventConfigById(String id, EventEntity entity, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    EventEntity eventEntity = service.update(id, entity);
    if (Objects.isNull(eventEntity)) {
      logger.error(EVENT_ENTITY_NOT_FOUND);
      asyncHandler.handle(createFutureResponse(PutEventConfigByIdResponse.respond400WithTextPlain(EVENT_ENTITY_NOT_FOUND)));
      return;
    }
    asyncHandler.handle(createFutureResponse(PutEventConfigByIdResponse.respond200WithApplicationJson(eventEntity)));
  }

  /**
   * Delete event config
   *
   * @param id           the configuration event ID
   * @param asyncHandler an AsyncResult<Response> Handler {@link Handler} which must be called as follows in the final callback (most internal callback) of the function
   */
  @Override
  public void deleteEventConfigById(String id, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    EventResponse eventResponse = service.delete(id);
    if (Objects.isNull(eventResponse)) {
      logger.error(EVENT_ENTITY_NOT_FOUND);
      asyncHandler.handle(createFutureResponse(DeleteEventConfigByIdResponse.respond400WithTextPlain(EVENT_ENTITY_NOT_FOUND)));
      return;
    }
    asyncHandler.handle(createFutureResponse(DeleteEventConfigByIdResponse.respond200WithApplicationJson(eventResponse)));
  }
}
