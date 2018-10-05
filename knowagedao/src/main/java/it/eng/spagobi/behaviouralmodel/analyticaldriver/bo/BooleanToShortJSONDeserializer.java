package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class BooleanToShortJSONDeserializer extends JsonDeserializer<Short> {

	@Override
	public Short deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Short truthy = 1;
		Short falsy = 0;
		return parser.getBooleanValue() ? truthy : falsy;
	}

}
