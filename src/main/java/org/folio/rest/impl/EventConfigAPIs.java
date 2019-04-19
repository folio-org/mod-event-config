package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
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
import org.folio.rest.persist.interfaces.Results;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;
import org.z3950.zing.cql.cql2pgjson.FieldException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

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
    buildSqlWrapper(query, offset, limit)
      .compose(cqlWrapper -> getEventConfigCollection(cqlWrapper, vertx, tenantId))
      .map(this::mapResultsToConfigCollections)
      .map(GetEventConfigResponse::respond200WithApplicationJson)
      .map(Response.class::cast)
      .otherwise(this::mapException)
      .setHandler(asyncResultHandler);
  }

  private Future<CQLWrapper> buildSqlWrapper(String query, int offset, int limit) {
    try {
      CQL2PgJSON cql2pgJson = new CQL2PgJSON(EVENT_CONFIGS + ".jsonb");
      CQLWrapper cqlWrapper = new CQLWrapper(cql2pgJson, query)
        .setLimit(new Limit(limit))
        .setOffset(new Offset(offset));
      return Future.succeededFuture(cqlWrapper);
    } catch (FieldException e) {
      return Future.failedFuture(e);
    }
  }

  private Future<Results<EventEntity>> getEventConfigCollection(CQLWrapper cqlWrapper, Vertx vertx, String tenantId) {
    PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);

    String[] fieldList = {"*"};
    Future<Results<EventEntity>> getFuture = Future.future();
    postgresClient.get(EVENT_CONFIGS, EventEntity.class, fieldList, cqlWrapper,
      true, false, getFuture.completer());
    return getFuture;
  }

  private EventConfigCollection mapResultsToConfigCollections(Results<EventEntity> results) {
    return new EventConfigCollection()
      .withEventEntity(results.getResults())
      .withTotalRecords(results.getResultInfo().getTotalRecords());
  }

  private Response mapException(Throwable throwable) {
    logger.error(throwable);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .type(MediaType.TEXT_PLAIN)
      .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .build();
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
