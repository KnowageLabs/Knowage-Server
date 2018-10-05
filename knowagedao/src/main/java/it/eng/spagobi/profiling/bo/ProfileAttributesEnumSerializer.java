package it.eng.spagobi.profiling.bo;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ProfileAttributesEnumSerializer extends StdSerializer<ProfileAttributesValueTypes> {
	private static final long serialVersionUID = 1376504304439963619L;

	public ProfileAttributesEnumSerializer() {
		super(ProfileAttributesValueTypes.class);
	}

	public ProfileAttributesEnumSerializer(Class<ProfileAttributesValueTypes> profileAttribute) {
		super(profileAttribute);
	}

	@Override
	public void serialize(ProfileAttributesValueTypes profileAttribute, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		generator.writeStartObject();
		generator.writeFieldName("name");
		generator.writeString(profileAttribute.name());
		generator.writeFieldName("type");
		generator.writeString(profileAttribute.getType());
		generator.writeEndObject();
	}

}
