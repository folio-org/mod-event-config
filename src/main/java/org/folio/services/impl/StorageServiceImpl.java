package org.folio.services.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventEntries;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.persist.interfaces.Results;
import org.folio.services.StorageService;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;
import org.z3950.zing.cql.cql2pgjson.FieldException;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of StorageService,
 * calls PostgresClient to perform CRUD operations on EventConfig entity
 */
public class StorageServiceImpl implements StorageService {

  private static final String ERROR_MESSAGE_STORAGE_SERVICE = "Error while %s | message: %s";
  private static final String SUCCESSFUL_MESSAGE_DELETE_EVENT = "Configuration with id: '%s' was successfully deleted";
  private static final String EVENT_CONFIG_TABLE_NAME = "event_configurations";
  private static final String EVENT_CONFIG_CRITERIA_ID = "id==%s";
  private static final String EVENT_CONFIG_ID = "id";

  private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);
  private final Vertx vertx;

  public StorageServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public StorageService createEventConfig(String tenantId, JsonObject eventEntity,
                                          Handler<AsyncResult<JsonObject>> asyncResultHandler) {
    try {
      String id = UUID.randomUUID().toString();
      eventEntity.put(EVENT_CONFIG_ID, id);
      EventEntity eventConfig = eventEntity.mapTo(EventEntity.class);
      PostgresClient.getInstance(vertx, tenantId).save(EVENT_CONFIG_TABLE_NAME, id, eventConfig,
        postReply -> {
          if (postReply.failed()) {
            String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
              "saving the event configuration to the db", postReply.cause().getMessage());
            logger.error(errorMessage);
            asyncResultHandler.handle(Future.failedFuture(postReply.cause()));
            return;
          }
          asyncResultHandler.handle(Future.succeededFuture(eventEntity));
        });
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
        "creating new event configuration", ex.getMessage());
      logger.error(errorMessage);
      asyncResultHandler.handle(Future.failedFuture(errorMessage));
    }
    return this;
  }

  @Override
  public StorageService updateEventConfig(String tenantId, String id, JsonObject eventEntity,
                                          Handler<AsyncResult<JsonObject>> asyncResultHandler) {
    try {
      eventEntity.put(EVENT_CONFIG_ID, id);
      EventEntity eventConfig = eventEntity.mapTo(EventEntity.class);
      CQLWrapper cqlFilter = getCqlWrapper(id);
      PostgresClient.getInstance(vertx, tenantId)
        .update(EVENT_CONFIG_TABLE_NAME, eventConfig, cqlFilter, true,
          updateReply -> {
            if (updateReply.failed()) {
              String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
                "updating the event configuration to the db", updateReply.cause().getMessage());
              logger.error(errorMessage);
              asyncResultHandler.handle(Future.failedFuture(updateReply.cause()));
              return;
            }
            int resultCode = updateReply.result().getUpdated();
            if (resultCode == 0) {
              asyncResultHandler.handle(Future.succeededFuture(null));
              return;
            }
            asyncResultHandler.handle(Future.succeededFuture(JsonObject.mapFrom(eventConfig)));
          });
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
        "updating event configuration", ex.getMessage());
      logger.error(errorMessage);
      asyncResultHandler.handle(Future.failedFuture(errorMessage));
    }
    return this;
  }

  @Override
  public StorageService findEventConfigById(String tenantId, String id,
                                            Handler<AsyncResult<JsonObject>> asyncResultHandler) {
    try {
      CQLWrapper cqlFilter = getCqlWrapper(id);
      PostgresClient.getInstance(vertx, tenantId).get(EVENT_CONFIG_TABLE_NAME, EventEntity.class, cqlFilter,
        true, getReply -> {
          if (getReply.failed()) {
            asyncResultHandler.handle(Future.succeededFuture(null));
            return;
          }
          Optional<EventEntity> eventEntityOpt = getReply.result().getResults().stream().findFirst();
          if (!eventEntityOpt.isPresent()) {
            asyncResultHandler.handle(Future.succeededFuture(null));
            return;
          }
          asyncResultHandler.handle(Future.succeededFuture(JsonObject.mapFrom(eventEntityOpt.get())));
        });
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE, "find the event by id", ex.getMessage());
      logger.error(errorMessage);
      asyncResultHandler.handle(Future.failedFuture(errorMessage));
    }
    return this;
  }

  @Override
  public StorageService findAllEventConfigurations(String tenantId, Handler<AsyncResult<JsonObject>> asyncResultHandler) {
    try {
      String[] fieldList = {"*"};
      PostgresClient.getInstance(vertx, tenantId)
        .get(EVENT_CONFIG_TABLE_NAME, EventEntity.class, fieldList, new CQLWrapper(), true, false,
          getReply -> {
            if (getReply.failed()) {
              String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
                "querying the db to get all event configurations", getReply.cause().getMessage());
              logger.error(errorMessage);
              asyncResultHandler.handle(Future.failedFuture(getReply.cause()));
            } else {
              Results<EventEntity> result = getReply.result();
              Integer totalRecords = result.getResultInfo().getTotalRecords();
              EventEntries eventEntries = new EventEntries()
                .withEventEntity(result.getResults())
                .withTotalRecords(totalRecords);

              JsonObject entries = JsonObject.mapFrom(eventEntries);
              asyncResultHandler.handle(Future.succeededFuture(entries));
            }
          });
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
        "find all event configurations", ex.getMessage());
      logger.error(errorMessage);
      asyncResultHandler.handle(Future.failedFuture(errorMessage));
    }
    return this;
  }

  @Override
  public StorageService deleteEventConfigById(String tenantId, String id,
                                              Handler<AsyncResult<JsonObject>> asyncResultHandler) {
    try {
      CQLWrapper cqlFilter = getCqlWrapper(id);
      PostgresClient.getInstance(vertx, tenantId)
        .delete(EVENT_CONFIG_TABLE_NAME, cqlFilter,
          deleteReply -> {
            if (deleteReply.failed()) {
              String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
                "deleting the event configuration to the db", deleteReply.cause().getMessage());
              logger.error(errorMessage);
              asyncResultHandler.handle(Future.failedFuture(deleteReply.cause()));
              return;
            }
            int resultCode = deleteReply.result().getUpdated();
            if (resultCode == 0) {
              asyncResultHandler.handle(Future.succeededFuture(null));
              return;
            }
            EventResponse eventResponse = new EventResponse()
              .withMessage(String.format(SUCCESSFUL_MESSAGE_DELETE_EVENT, id));
            JsonObject eventResponseJson = JsonObject.mapFrom(eventResponse);
            asyncResultHandler.handle(Future.succeededFuture(eventResponseJson));
          });
    } catch (Exception ex) {
      String errorMessage = String.format(ERROR_MESSAGE_STORAGE_SERVICE,
        "deleting event configuration", ex.getMessage());
      logger.error(errorMessage);
      asyncResultHandler.handle(Future.failedFuture(errorMessage));

    }
    return this;
  }

  /**
   * Builds criteria wrapper
   *
   * @param value - value corresponding to the key
   * @return - CQLWrapper object
   */
  private CQLWrapper getCqlWrapper(String value) throws FieldException {
    CQL2PgJSON cql2PgJSON = new CQL2PgJSON(EVENT_CONFIG_TABLE_NAME + ".jsonb");
    return new CQLWrapper(cql2PgJSON, String.format(EVENT_CONFIG_CRITERIA_ID, value));
  }
}
