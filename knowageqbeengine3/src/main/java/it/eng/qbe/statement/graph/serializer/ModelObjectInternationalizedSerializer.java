/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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