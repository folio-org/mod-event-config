package org.folio.services.impl;

import com.google.gson.Gson;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.folio.rest.impl.EventConfigAPIs;
import org.folio.rest.jaxrs.model.EventEntity;
import org.folio.rest.jaxrs.model.EventResponse;
import org.folio.services.StorageService;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stub implementation
 */
public class StubStorageServiceImpl implements StorageService {

  private static final String FILE_NAME = "stub_storage.json";
  private static final String FILE_ENCODING = "UTF-8";
  private static final String ERROR_FILE_NOT_FOUND = "The file `%s` not found!";

  @Override
  public EventEntity save(EventEntity entity) {
    JsonArray stubConfigurations = readStub();
    JsonObject stubEventEntity = JsonObject.mapFrom(entity);
    stubConfigurations.add(stubEventEntity);
    writeStub(stubConfigurations);
    return entity;
  }

  @Override
  public EventEntity update(String id, EventEntity entity) {
    EventEntity eventEntity = findById(id);
    if (Objects.isNull(eventEntity)) {
      return null;
    }
    entity.setId(id);
    EventResponse eventResponse = delete(id);
    if (Objects.isNull(eventResponse)) {
      return null;
    }
    return save(entity);
  }

  @Override
  public EventEntity findById(String id) {
    String stubConfigurations = readStub().toString();
    return Arrays.stream(new Gson().fromJson(stubConfigurations, EventEntity[].class))
      .filter(stub -> stub.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

  @Override
  public EventResponse delete(String id) {
    String stubConfigurations = readStub().toString();
    List<EventEntity> entities = Arrays.stream(new Gson().fromJson(stubConfigurations, EventEntity[].class)).collect(Collectors.toList());
    EventEntity eventEntity = entities.stream()
      .filter(stub -> stub.getId().equals(id))
      .findFirst()
      .orElse(null);

    if (Objects.isNull(eventEntity)) {
      return null;
    }

    List<EventEntity> result = entities.stream()
      .filter(stub -> !stub.getId().equals(id))
      .collect(Collectors.toList());
    writeStub(new JsonArray(result));

    EventResponse eventResponse = new EventResponse();
    eventResponse.setId(eventEntity.getId());
    eventResponse.setName(eventEntity.getName());

    return eventResponse;
  }

  private JsonArray readStub() {
    ClassLoader classLoader = EventConfigAPIs.class.getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
    try {
      String fileToString = FileUtils.readFileToString(file, FILE_ENCODING);
      return new JsonArray(fileToString);
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format(ERROR_FILE_NOT_FOUND, FILE_NAME));
    }
  }

  private void writeStub(JsonArray stubConfigurations) {
    ClassLoader classLoader = EventConfigAPIs.class.getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
    try {
      FileUtils.write(file, stubConfigurations.toString(), FILE_ENCODING);
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format(ERROR_FILE_NOT_FOUND, FILE_NAME));
    }
  }
}
