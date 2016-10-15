package com.betleopard;

import com.betleopard.domain.Leg;
import com.betleopard.domain.OddsType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 *
 * @author ben
 */
public class CustomLegSerializer extends JsonSerializer<Leg> {

    @Override
    public void serialize(final Leg leg, final JsonGenerator jgen, final SerializerProvider sp) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeNumberField("race", leg.getRace().getID());
        jgen.writeNumberField("backing", leg.getBacking().getID());
        jgen.writeNumberField("oddsVersion", leg.getOddsVersion());
        final OddsType ot = leg.getoType();
        jgen.writeStringField("oddsType", "" + ot);
        if (ot == OddsType.FIXED_ODDS) {
            jgen.writeNumberField("odds", leg.odds());
        }
        if (leg.hasStake()) {
            jgen.writeNumberField("stake", leg.stake());
        }
        jgen.writeEndObject();
    }

    @Override
    public Class<Leg> handledType() {
        return Leg.class;
    }
}
