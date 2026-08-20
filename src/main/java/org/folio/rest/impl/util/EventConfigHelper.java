package org.folio.rest.impl.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.jaxrs.model.EventConfigEntity;
import org.folio.rest.jaxrs.model.Template;
import org.folio.rest.persist.cql.CQLQueryValidationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EventConfigHelper {
  private static final Logger logger = LogManager.getLogger(EventConfigHelper.class);
  private static final String SMS_DELIVERY_CHANNEL = "sms";
  private static final String TEXT_PLAIN = "text/plain";

  private EventConfigHelper() {}

  public static Response validateSmsTemplateOutputFormats(EventConfigEntity entity) {
    if (entity == null || entity.getTemplates() == null) {
      return null;
    }

    List<String> invalidTemplates = new ArrayList<>();
    List<Template> templates = entity.getTemplates();
    for (int index = 0; index < templates.size(); index++) {
      Template template = templates.get(index);
      if (
        template != null &&
        SMS_DELIVERY_CHANNEL.equalsIgnoreCase(template.getDeliveryChannel()) &&
        !TEXT_PLAIN.equals(template.getOutputFormat())
      ) {
        invalidTemplates.add("Template %s".formatted(index));
      }
    }

    if (invalidTemplates.isEmpty()) {
      return null;
    }

    return Response
      .status(Response.Status.BAD_REQUEST.getStatusCode())
      .type(MediaType.TEXT_PLAIN)
      .entity(
        "SMS notification templates must use outputFormat 'text/plain'. " +
        "Invalid template(s): " +
        String.join(", ", invalidTemplates)
      )
      .build();
  }

  public static Response mapException(Throwable throwable) {
    logger.debug("mapException:: Mapping Exception");
    if (throwable instanceof CQL2PgJSONException ||
      throwable instanceof CQLQueryValidationException) {
      logger.warn("mapException:: Returning BAD_REQUEST");
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
