package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.jaxrs.model.EventConfigCollection;
import org.folio.rest.jaxrs.model.EventConfigEntity;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.resource.EventConfig;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PgUtil;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static io.vertx.core.Future.succeededFuture;

public class EventConfigAPIs implements EventConfig {

  private final Logger logger = LoggerFactory.getLogger(EventConfigAPIs.class);

  private static final String EVENT_CONFIGS = "event_configurations";

  private final Vertx vertx;
  private final String tenantId;

  public EventConfigAPIs(Vertx vertx, String tenantId) {
    this.vertx = vertx;
    this.tenantId = tenantId;
  }

  @Override
  public void getEventConfig(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders,
                             Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);

      logger.info("CQL Query: " + query);

      String[] fieldList = {"*"};

      CQL2PgJSON cql2pgJson = new CQL2PgJSON(EVENT_CONFIGS + ".jsonb");
      CQLWrapper cql = new CQLWrapper(cql2pgJson, query)
        .setLimit(new Limit(limit))
        .setOffset(new Offset(offset));

      postgresClient.get(EVENT_CONFIGS, EventEntity.class, fieldList, cql,
        true, false, reply -> {
          try {
            if (reply.succeeded()) {
              List<EventEntity> eventEntities = reply.result().getResults();

              EventConfigCollection configCollection =
                new EventConfigCollection()
                  .withEventEntity(eventEntities)
                  .withTotalRecords(reply.result().getResultInfo().getTotalRecords());
              asyncResultHandler.handle(succeededFuture(
                GetEventConfigResponse.respond200WithApplicationJson(configCollection)));
            } else {
              asyncResultHandler.handle(succeededFuture(
                GetEventConfigResponse.respond500WithTextPlain(reply.cause().getMessage())));
            }
          } catch (Exception e) {
            logger.error(e);
            asyncResultHandler.handle(succeededFuture(
              GetEventConfigResponse.respond500WithTextPlain(e.getMessage())));
          }
        });
    } catch (Exception e) {
      logger.error(e);
      asyncResultHandler.handle(succeededFuture(
        GetEventConfigResponse.respond500WithTextPlain(e.getMessage())));
    }
  }

  @Override
  public void postEventConfig(String lang, EventConfigEntity entity, Map<String, String> okapiHeaders,
                              Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.post(EVENT_CONFIGS, entity, okapiHeaders, vertxContext, PostEventConfigResponse.class, asyncResultHandler);
  }

  @Override
  public void getEventConfigById(String id, String lang, Map<String, String> okapiHeaders,
                                 Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.getById(EVENT_CONFIGS, EventConfigEntity.class, id, okapiHeaders, vertxContext, GetEventConfigByIdResponse.class, asyncResultHandler);
  }

  @Override
  public void deleteEventConfigById(String id, String lang, Map<String, String> okapiHeaders,
                                    Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.deleteById(EVENT_CONFIGS, id, okapiHeaders, vertxContext, DeleteEventConfigByIdResponse.class, asyncResultHandler);
  }

  @Override
  public void putEventConfigById(String id, String lang, EventConfigEntity entity, Map<String, String> okapiHeaders,
                                 Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.put(EVENT_CONFIGS, entity, id, okapiHeaders, vertxContext, PutEventConfigByIdResponse.class, asyncResultHandler);
  }
}
