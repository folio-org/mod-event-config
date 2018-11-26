package org.folio.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.resource.support.ResponseDelegate;

import javax.ws.rs.core.Response;

public class EventConfigUtils {

  public static final String EVENT_CONFIG_PROXY_ADDRESS = "event-config-service.queue";

  public static final String VALUE_IS_NOT_FOUND = "isNotFound";
  public static final JsonObject EMPTY_JSON_OBJECT = new JsonObject().put(VALUE_IS_NOT_FOUND, true);

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
