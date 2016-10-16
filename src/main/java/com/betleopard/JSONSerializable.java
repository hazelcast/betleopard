/*
 * 
 * 
 * 
 */
package com.betleopard;

import com.betleopard.domain.Bet;
import static com.betleopard.domain.Bet.parseBlob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author ben
 */
public interface JSONSerializable {

    public default String toJSONString() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(this);
    }

    public static <E> E parse(final String parseText, final Function<Map<String, ?>, E> f) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return f.apply(mapper.readValue(parseText, new TypeReference<Map<String, ?>>() {
        }));
    }

}
