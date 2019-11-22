package org.folio.impl.util;

import org.apache.http.HttpStatus;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.impl.util.EventConfigHelper;
import org.folio.rest.persist.cql.CQLQueryValidationException;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.junit.Assert.*;

public class EventConfigHelperTest {

  @Test
  public void mapToExceptionTest() {
    Response response = EventConfigHelper.mapException(new CQL2PgJSONException("test"));
    assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
    assertEquals(response.getMediaType().toString(), MediaType.TEXT_PLAIN);

    response = EventConfigHelper.mapException(new CQLQueryValidationException(null));
    assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);
    assertEquals(response.getMediaType().toString(), MediaType.TEXT_PLAIN);

    response = EventConfigHelper.mapException(new NullPointerException());
    assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    assertEquals(response.getMediaType().toString(), MediaType.TEXT_PLAIN);
  }

}
