/*
 * 
 * 
 * 
 */
package com.betleopard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author ben
 */
public interface JSONSerializable {

    public default String toJSONString() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    public static <E> E parse(final String parseText, final Function<Map<String, ?>, E> f) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        return f.apply(mapper.readValue(parseText, new TypeReference<Map<String, ?>>() {
        }));
    }

}
