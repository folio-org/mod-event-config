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
import javax.ws.rs.core.Response.Status;
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

  @Override
  public void postEventConfig(EventEntity entity, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    logger.debug(" POST | `/eventConfig` | entityId: " + entity.getId());
    EventEntity eventEntity = service.save(entity);
    asyncHandler.handle(createFutureResponse(Status.OK, eventEntity, PostEventConfigResponse.class));
  }

  @Override
  public void getEventConfigById(String id, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    logger.debug(" GET | `/eventConfig/{id}` | entityId: " + id);
    EventEntity eventEntity = service.findById(id);
    if (Objects.isNull(eventEntity)) {
      asyncHandler.handle(createFutureResponse(Status.BAD_REQUEST, EVENT_ENTITY_NOT_FOUND, GetEventConfigByIdResponse.class));
      return;
    }
    asyncHandler.handle(createFutureResponse(Status.OK, eventEntity, GetEventConfigByIdResponse.class));
  }

  @Override
  public void putEventConfigById(String id, EventEntity entity, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    logger.debug(" PUT | `/eventConfig/{id}` | entityId: " + id);
    EventEntity eventEntity = service.update(id, entity);
    if (Objects.isNull(eventEntity)) {
      asyncHandler.handle(createFutureResponse(Status.BAD_REQUEST, EVENT_ENTITY_NOT_FOUND, PutEventConfigByIdResponse.class));
      return;
    }
    asyncHandler.handle(createFutureResponse(Status.OK, eventEntity, PutEventConfigByIdResponse.class));
  }

  @Override
  public void deleteEventConfigById(String id, Map<String, String> headers, Handler<AsyncResult<Response>> asyncHandler, Context context) {
    logger.debug(" DELETE | `/eventConfig/{id}` | entityId: " + id);
    EventResponse eventResponse = service.delete(id);
    if (Objects.isNull(eventResponse)) {
      asyncHandler.handle(createFutureResponse(Status.BAD_REQUEST, EVENT_ENTITY_NOT_FOUND, DeleteEventConfigByIdResponse.class));
      return;
    }
    asyncHandler.handle(createFutureResponse(Status.OK, eventResponse, DeleteEventConfigByIdResponse.class));
  }
}
