package org.folio.impl.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.folio.HttpStatus;
import org.folio.cql2pgjson.exception.CQL2PgJSONException;
import org.folio.rest.impl.util.EventConfigHelper;
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

}
