package org.folio.services;

import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventResponse;

/**
 * The interface provides basic CRUD operations for storing and retrieving data from the storage
 */
public interface StorageService {

  /**
   * Save entity
   *
   * @param entity EventEntity
   * @return EventEntity
   */
  EventEntity save(EventEntity entity);

  /**
   * Update the entity by id
   *
   * @param id     identifier
   * @param entity EventEntity
   * @return EventEntity
   */
  EventEntity update(String id, EventEntity entity);

  /**
   * Find the entity by id
   *
   * @param id identifier
   * @return EventEntity
   */
  EventEntity findById(String id);

  /**
   * Delete the entity by id
   *
   * @param id identifier
   * @return EventResponse
   */
  EventResponse delete(String id);
}
