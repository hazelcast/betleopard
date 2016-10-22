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
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author ben
 */
public interface JSONSerializable extends Serializable, LongIndexed {

    public default String toJSONString() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException jsonx) {
            throw new RuntimeException(jsonx);
        }
    }

    public static <E> E parse(final String parseText, final Function<Map<String, ?>, E> f) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        try {
            return f.apply(mapper.readValue(parseText, new TypeReference<Map<String, ?>>() {
            }));
        } catch (IOException iox) {
            throw new RuntimeException(iox);
        }
    }

    public static LocalDateTime parseDateTime(final Map<String, ?> dateBits) {
        final int year = Integer.parseInt("" + dateBits.get("year"));
        final int month = Integer.parseInt("" + dateBits.get("monthValue"));
        final int day = Integer.parseInt("" + dateBits.get("dayOfMonth"));
        final int hour = Integer.parseInt("" + dateBits.get("hour"));
        final int minute = Integer.parseInt("" + dateBits.get("minute"));

        return LocalDateTime.of(year, month, day, hour, minute);
    }
}
