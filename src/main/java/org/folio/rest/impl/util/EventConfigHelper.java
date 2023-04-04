package org.folio.rest.impl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.persist.cql.CQLQueryValidationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EventConfigHelper {
  private static final Logger logger = LogManager.getLogger(EventConfigHelper.class);

  private EventConfigHelper() {}

  public static Response mapException(Throwable throwable) {
    logger.debug("mapException:: Mapping Exception");
    if (throwable instanceof CQL2PgJSONException ||
      throwable instanceof CQLQueryValidationException) {
      logger.warn("mapException:: It is a BAD_REQUEST and here's the message : {}",throwable.getMessage());
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
        .type(MediaType.TEXT_PLAIN)
        .entity(throwable.getMessage())
        .build();
    }
    logger.warn("mapException :: There is a {}",Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .type(MediaType.TEXT_PLAIN)
      .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .build();
  }

}
