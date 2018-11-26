package org.folio.services;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventEntries;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.services.impl.StorageServiceImpl;

/**
 * The interface provides basic CRUD operations for storing and retrieving data from the storage
 */
@ProxyGen
public interface StorageService {

  static StorageService create(Vertx vertx) {
    return new StorageServiceImpl(vertx);
  }

  /**
   * Creates proxy instance that helps to push message into the message queue
   *
   * @param vertx   vertx instance
   * @param address host address
   * @return StorageService instance
   */
  static StorageService createProxy(Vertx vertx, String address) {
    return new StorageServiceVertxEBProxy(vertx, address);
  }

  /**
   * Save entity {@link EventEntity}
   *
   * @param eventEntity Json representation of the entity {@link EventEntity}
   * @return EventEntity
   */
  @Fluent
  StorageService createEventConfig(String tenantId, JsonObject eventEntity,
                                   Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Update the entity {@link EventEntity} by id
   *
   * @param id          identifier
   * @param eventEntity Json representation of the entity {@link EventEntity}
   * @return asyncResult with the entity {@link EventEntity}
   */
  @Fluent
  StorageService updateEventConfig(String tenantId, String id, JsonObject eventEntity,
                                   Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Find the entity {@link EventEntity} by id
   *
   * @param id identifier
   * @return asyncResult with the entity {@link EventEntity}
   */
  @Fluent
  StorageService findEventConfigById(String tenantId, String id,
                                     Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Find the entity {@link EventEntity} by name
   *
   * @param name name of event
   * @return asyncResult with the entity {@link EventEntity}
   */
  @Fluent
  StorageService findEventConfigByName(String tenantId, String name,
                                     Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Find all entries
   *
   * @return asyncResult with the entity {@link EventEntries}
   */
  @Fluent
  StorageService findAllEventConfigurations(String tenantId,
                                            Handler<AsyncResult<JsonObject>> asyncResultHandler);

  /**
   * Delete the entity {@link EventEntity} by id
   *
   * @param id identifier
   * @return asyncResult with the entity {@link EventResponse}
   */
  @Fluent
  StorageService deleteEventConfigById(String tenantId, String id,
                                       Handler<AsyncResult<JsonObject>> asyncResultHandler);
}
