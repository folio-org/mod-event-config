package org.folio.impl.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.folio.HttpStatus;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.impl.util.EventConfigHelper;
import org.folio.rest.jaxrs.model.EventConfigEntity;
import org.folio.rest.jaxrs.model.Template;
import org.folio.rest.persist.cql.CQLQueryValidationException;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EventConfigHelperTest {

  @Test
  public void mapToExceptionTest() {
    Response response = EventConfigHelper.mapException(new CQL2PgJSONException("test"));
    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType().toString(), is(MediaType.TEXT_PLAIN));

    response = EventConfigHelper.mapException(new CQLQueryValidationException(null));
    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType().toString(), is(MediaType.TEXT_PLAIN));

    response = EventConfigHelper.mapException(new NullPointerException());
    assertThat(response.getStatus(), is(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    assertThat(response.getMediaType().toString(), is(MediaType.TEXT_PLAIN));
  }

  @Test
  public void validateSmsTemplateOutputFormatsAllowsPlainTextForSmsDeliveryChannel() {
    EventConfigEntity entity = new EventConfigEntity()
      .withTemplates(List.of(template("sms-template", "sms", "text/plain")));

    assertNull(EventConfigHelper.validateSmsTemplateOutputFormats(entity));
  }

  @Test
  public void validateSmsTemplateOutputFormatsAllowsHtmlForEmailDeliveryChannel() {
    EventConfigEntity entity = new EventConfigEntity()
      .withTemplates(List.of(template("email-template", "email", "text/html")));

    assertNull(EventConfigHelper.validateSmsTemplateOutputFormats(entity));
  }

  @Test
  public void validateSmsTemplateOutputFormatsAllowsNonPlainTextForTextDeliveryChannel() {
    EventConfigEntity entity = new EventConfigEntity()
      .withTemplates(List.of(template("text-template", "text", "application/json")));

    assertNull(EventConfigHelper.validateSmsTemplateOutputFormats(entity));
  }

  @Test
  public void validateSmsTemplateOutputFormatsRejectsNonPlainTextForSmsDeliveryChannel() {
    EventConfigEntity entity = new EventConfigEntity()
      .withTemplates(List.of(template("sms-template", "sms", "text/html")));

    Response response = EventConfigHelper.validateSmsTemplateOutputFormats(entity);

    assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    assertThat(response.getMediaType().toString(), is(MediaType.TEXT_PLAIN));
    assertThat(response.getEntity().toString().contains("SMS notification templates must use outputFormat 'text/plain'"), is(true));
    assertThat(response.getEntity().toString().contains("Template 0"), is(true));
  }

  private Template template(String templateId, String deliveryChannel, String outputFormat) {
    return new Template()
      .withTemplateId(templateId)
      .withDeliveryChannel(deliveryChannel)
      .withOutputFormat(outputFormat);
  }
}
