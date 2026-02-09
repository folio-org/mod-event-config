package org.folio.rest.impl;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.cql2pgjson.CQL2PgJSON;
import org.folio.rest.impl.util.EventConfigHelper;
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

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class EventConfigAPIs implements EventConfig {

  private static final Logger logger = LogManager.getLogger(EventConfigAPIs.class);

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
    logger.debug("getEventConfig:: Trying to get Event Configuration");
    buildSqlWrapper(query, offset, limit)
      .compose(cqlWrapper -> getEventConfigCollection(cqlWrapper, vertx, tenantId))
      .map(this::mapResultsToConfigCollections)
      .map(GetEventConfigResponse::respond200WithApplicationJson)
      .map(Response.class::cast)
      .otherwise(EventConfigHelper::mapException)
      .onComplete(asyncResultHandler);
  }

  private Future<CQLWrapper> buildSqlWrapper(String query, int offset, int limit) {
    logger.debug("buildSqlWrapper:: Trying to build SQL Wrapper");
    try {
      logger.info("buildSqlWrapper:: Building SQL Wrapper with query {},offset {} and limit {}",query,offset,limit);
      CQL2PgJSON cql2pgJson = new CQL2PgJSON(EVENT_CONFIGS + ".jsonb");
      CQLWrapper cqlWrapper = new CQLWrapper(cql2pgJson, query)
        .setLimit(new Limit(limit))
        .setOffset(new Offset(offset));
      return Future.succeededFuture(cqlWrapper);
    } catch (Exception e) {
      logger.warn("buildSqlWrapper:: Error building SQL Wrapper, here's the message : {}",e.getMessage());
      return Future.failedFuture(e);
    }
  }

  private Future<Results<EventEntity>> getEventConfigCollection(CQLWrapper cqlWrapper, Vertx vertx, String tenantId) {
    logger.debug("getEventConfigCollection:: Trying to get the Collection of Event Configurations with tenant id :{}",tenantId);
    PostgresClient postgresClient = PostgresClient.getInstance(vertx, tenantId);

    String[] fieldList = {"*"};
    Promise<Results<EventEntity>> promise = Promise.promise();
    postgresClient.get(EVENT_CONFIGS, EventEntity.class, fieldList, cqlWrapper,
      true, false, promise::handle);
    return promise.future();
  }

  private EventConfigCollection mapResultsToConfigCollections(Results<EventEntity> results) {
    logger.debug("mapResultsToConfigCollections:: Mapping results to Configuration Collection");
    return new EventConfigCollection()
      .withEventEntity(results.getResults())
      .withTotalRecords(results.getResultInfo().getTotalRecords());
  }

  @Override
  public void postEventConfig(String lang, EventConfigEntity entity, Map<String, String> okapiHeaders,
                              Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    logger.debug("postEventConfig:: Trying to post the Event Configuration");
    PgUtil.post(EVENT_CONFIGS, entity, okapiHeaders, vertxContext, PostEventConfigResponse.class, asyncResultHandler);
  }

  @Override
  public void getEventConfigById(String id, String lang, Map<String, String> okapiHeaders,
                                 Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    logger.debug("getEventConfigById:: Trying to get the Event Configuration By Id : {}",id);
    PgUtil.getById(EVENT_CONFIGS, EventConfigEntity.class, id, okapiHeaders, vertxContext, GetEventConfigByIdResponse.class, asyncResultHandler);
  }

  @Override
  public void deleteEventConfigById(String id, String lang, Map<String, String> okapiHeaders,
                                    Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    logger.debug("deleteEventConfigById:: Trying to delete the Event Configuration By Id : {}",id);
    PgUtil.deleteById(EVENT_CONFIGS, id, okapiHeaders, vertxContext, DeleteEventConfigByIdResponse.class, asyncResultHandler);
  }

  @Override
  public void putEventConfigById(String id, String lang, EventConfigEntity entity, Map<String, String> okapiHeaders,
                                 Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    logger.debug("putEventConfigById:: Trying to update the Event Configuration By Id : {}",id);
    PgUtil.put(EVENT_CONFIGS, entity, id, okapiHeaders, vertxContext, PutEventConfigByIdResponse.class, asyncResultHandler);
  }
}
