package org.folio.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.resource.support.ResponseDelegate;

import javax.ws.rs.core.Response;

public class EventConfigUtils {

  public static final String EVENT_CONFIG_PROXY_ADDRESS = "event-config-service.queue";

  private EventConfigUtils() {
    //not called
  }

  public static AsyncResult<Response> createFutureResponse(ResponseDelegate delegate) {
    return Future.succeededFuture(delegate);
  }

  public static <T> T getResponseEntity(AsyncResult<JsonObject> asyncResult, Class<T> t) {
    return asyncResult.result().mapTo(t);
  }
}
