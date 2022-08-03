package it.eng.spagobi.rest.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.commons.dao.dto.SbiCategory;

@Provider
public class SbiCategoryBodyWriter implements MessageBodyWriter<SbiCategory> {

	private static class SbiCategoryWrapper {

		private final SbiCategory category;

		public SbiCategoryWrapper(SbiCategory category) {
			this.category = category;
		}

		public String getValueCd() {
			return category.getName();
		}

		public String getValueName() {
			return category.getName();
		}

		public String getDomainCode() {
			return category.getType();
		}

		public String getDomainName() {
			return category.getType();
		}

		public String getValueDescription() {
			return category.getName();
		}

		public Integer getValueId() {
			return category.getId();
		}

	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public void writeTo(SbiCategory t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

		SbiCategoryWrapper wrapper = new SbiCategoryWrapper(t);

		objectMapper.writeValue(entityStream, wrapper);

	}

}
