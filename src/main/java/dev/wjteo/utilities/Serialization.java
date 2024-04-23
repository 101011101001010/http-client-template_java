package dev.wjteo.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class Serialization {
    public static <T> Optional<String> serialize(T object) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return Optional.of(json);
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(Serialization.class).error("Failed to serialize: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static <T> Optional<T> deserialize(String json, Class<T> objectClass) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final T object = mapper.readValue(json, objectClass);
            return Optional.of(object);
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(Serialization.class).error("Failed to serialize: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static <T> Optional<List<T>> deserializeList(String json, Class<T> listElementClass) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final List<T> list = mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, listElementClass));
            return Optional.of(list);
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(Serialization.class).error("Failed to serialize: " + e.getMessage());
            return Optional.empty();
        }
    }
}
