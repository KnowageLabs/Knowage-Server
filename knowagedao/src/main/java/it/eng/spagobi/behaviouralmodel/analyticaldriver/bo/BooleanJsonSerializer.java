package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BooleanJsonSerializer extends JsonSerializer<Object> {

	@Override
	public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException {

		int boolean1 = Integer.valueOf((String.valueOf(value)));
		boolean newValue = false;
		if (boolean1 == 1)
			newValue = true;
		generator.writeBoolean(newValue);

	}

}
