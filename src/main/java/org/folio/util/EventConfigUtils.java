package org.folio.util;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.rest.jaxrs.resource.EventConfig.DeleteEventConfigByIdResponse;
import org.folio.rest.jaxrs.resource.EventConfig.GetEventConfigByIdResponse;
import org.folio.rest.jaxrs.resource.EventConfig.PostEventConfigResponse;
import org.folio.rest.jaxrs.resource.EventConfig.PutEventConfigByIdResponse;
import org.folio.rest.jaxrs.resource.support.ResponseDelegate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class EventConfigUtils {

  private static final String INCORRECT_EVENT_ENTITY = "Incorrect the event object";
  private static final String INCORRECT_RESPONSE = "Incorrect response class";
  private static final String SERVER_ERROR = "Internal Server Error";

  private EventConfigUtils() {
    //not called
  }

  public static Future<Response> createFutureResponse(Status status, Object obj, Class<? extends ResponseDelegate> delegate) {
    return Future.succeededFuture(createResponse(status, obj, delegate));
  }

  private static Response createResponse(Status status, Object obj, Class<? extends ResponseDelegate> delegate) {
    if (delegate.isAssignableFrom(GetEventConfigByIdResponse.class)) {
      return getGetEventConfigByIdResponse(status, obj);
    } else if (delegate.isAssignableFrom(PostEventConfigResponse.class)) {
      return getPostEventConfigResponse(status, obj);
    } else if (delegate.isAssignableFrom(PutEventConfigByIdResponse.class)) {
      return getPutEventConfigByIdResponse(status, obj);
    } else if (delegate.isAssignableFrom(DeleteEventConfigByIdResponse.class)) {
      return getDeleteEventConfigByIdResponse(status, obj);
    } else {
      throw new IllegalArgumentException(INCORRECT_RESPONSE);
    }
  }

  private static Response getDeleteEventConfigByIdResponse(Status status, Object obj) {
    switch (status) {
      case OK:
        if (obj instanceof EventResponse) {
          return DeleteEventConfigByIdResponse.respond200WithApplicationJson((EventResponse) obj);
        }
        return DeleteEventConfigByIdResponse.respond400WithTextPlain(INCORRECT_EVENT_ENTITY);
      case BAD_REQUEST:
        return DeleteEventConfigByIdResponse.respond400WithTextPlain(obj);
      default:
        return DeleteEventConfigByIdResponse.respond500WithTextPlain(SERVER_ERROR);
    }
  }

  private static Response getPutEventConfigByIdResponse(Status status, Object obj) {
    switch (status) {
      case OK:
        if (obj instanceof EventEntity) {
          return PutEventConfigByIdResponse.respond200WithApplicationJson((EventEntity) obj);
        }
        return PutEventConfigByIdResponse.respond400WithTextPlain(INCORRECT_EVENT_ENTITY);
      case BAD_REQUEST:
        return PutEventConfigByIdResponse.respond400WithTextPlain(obj);
      default:
        return PutEventConfigByIdResponse.respond500WithTextPlain(SERVER_ERROR);
    }
  }

  private static Response getPostEventConfigResponse(Status status, Object obj) {
    switch (status) {
      case OK:
        if (obj instanceof EventEntity) {
          return PostEventConfigResponse.respond200WithApplicationJson((EventEntity) obj);
        }
        return PostEventConfigResponse.respond400WithTextPlain(INCORRECT_EVENT_ENTITY);
      case BAD_REQUEST:
        return PostEventConfigResponse.respond400WithTextPlain(obj);
      default:
        return PostEventConfigResponse.respond500WithTextPlain(SERVER_ERROR);
    }
  }

  private static Response getGetEventConfigByIdResponse(Status status, Object obj) {
    switch (status) {
      case OK:
        if (obj instanceof EventEntity) {
          return GetEventConfigByIdResponse.respond200WithApplicationJson((EventEntity) obj);
        }
        return GetEventConfigByIdResponse.respond400WithTextPlain(INCORRECT_EVENT_ENTITY);
      case BAD_REQUEST:
        return GetEventConfigByIdResponse.respond400WithTextPlain(obj);
      default:
        return GetEventConfigByIdResponse.respond500WithTextPlain(SERVER_ERROR);
    }
  }
}
