package it.eng.spagobi.profiling.bo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ProfileAttributesEnumDeserializer extends StdDeserializer<ProfileAttributesValueTypes> {

	Class<ProfileAttributesValueTypes> profileAttribute;
	private static final long serialVersionUID = 1L;

	protected ProfileAttributesEnumDeserializer(Class<ProfileAttributesValueTypes> profileAttribute) {
		super(profileAttribute);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 */

	@Override
	public ProfileAttributesValueTypes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Object a = p;
		return null;
	}

}
