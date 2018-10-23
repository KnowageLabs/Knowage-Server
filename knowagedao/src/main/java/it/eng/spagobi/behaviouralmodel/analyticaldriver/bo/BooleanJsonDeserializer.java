package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class BooleanJsonDeserializer extends JsonDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return parser.getBooleanValue() ? 1 : 0;
	}

}
