package org.folio.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import org.folio.rest.jaxrs.resource.support.ResponseDelegate;

import javax.ws.rs.core.Response;

public class EventConfigUtils {

  private EventConfigUtils() {
    //not called
  }

  public static AsyncResult<Response> createFutureResponse(ResponseDelegate delegate) {
    return Future.succeededFuture(delegate);
  }
}
