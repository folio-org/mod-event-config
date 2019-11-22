package org.folio.rest.impl.util;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.persist.cql.CQLQueryValidationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EventConfigHelper {
  private static final Logger logger = LoggerFactory.getLogger(EventConfigHelper.class);

  public static Response mapException(Throwable throwable) {
    if (throwable instanceof CQL2PgJSONException ||
      throwable instanceof CQLQueryValidationException) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
        .type(MediaType.TEXT_PLAIN)
        .entity(throwable.getMessage())
        .build();
    }
    logger.error(throwable);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .type(MediaType.TEXT_PLAIN)
      .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .build();
  }

}
