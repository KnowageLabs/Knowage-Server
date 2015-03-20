package it.eng.qbe.statement.graph.serializer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelObject;
import it.eng.qbe.statement.graph.bean.ModelObjectI18n;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ModelObjectInternationalizedSerializer extends JsonSerializer<ModelObjectI18n> {

	private IModelProperties datamartLabels;

	
	public ModelObjectInternationalizedSerializer(IDataSource dataSource, Locale locale) {
		super();
		datamartLabels = dataSource.getModelI18NProperties(locale );
	}

	@Override
	public void serialize(ModelObjectI18n value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException{
		jgen.writeString(getLabel(value.getObject()));

	}
	
	private String getLabel(IModelObject item){
		String name = datamartLabels.getProperty(item, "label");
		if (name==null){
			name = item.getName();
		}
		return name;
	}
}