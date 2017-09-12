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
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelObject;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RelationJSONSerializer extends JsonSerializer<Relationship> {

	private IModelProperties datamartLabels;
	
	public static final String SOURCE_NAME = "sourceName";
	public static final String SOURCE_ID = "sourceId";
	public static final String TARGET_NAME = "targetName";
	public static final String TARGET_ID = "targetId";
	public static final String RELATIONSHIP_NAME = "relationshipName";
	public static final String RELATIONSHIP_ID = "relationshipId";
	public static final String SOURCE_FIELDS = "sourceFields";
	public static final String TARGET_FIELDS = "targetFields";
	
	protected RelationJSONSerializer() {
		super();
	}
	
	public RelationJSONSerializer(IDataSource dataSource, Locale locale) {
		super();
		datamartLabels = dataSource.getModelI18NProperties(locale );
	}

	@Override
	public void serialize(Relationship value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException{
		jgen.writeStartObject();
		jgen.writeStringField(SOURCE_NAME, getLabel(value.getSourceEntity()));
		jgen.writeStringField(SOURCE_ID, value.getSourceEntity().getUniqueName());
		jgen.writeStringField(TARGET_NAME, getLabel(value.getTargetEntity()));
		jgen.writeStringField(TARGET_ID, value.getTargetEntity().getUniqueName());
		jgen.writeStringField(RELATIONSHIP_NAME, value.getName());
		jgen.writeStringField(RELATIONSHIP_ID, value.getId());
		jgen.writeStringField(SOURCE_FIELDS, getFieldsString(value.getSourceFields()));
		jgen.writeStringField(TARGET_FIELDS, getFieldsString(value.getTargetFields()));
		jgen.writeEndObject();
	}
	
	protected String getFieldsString(List<IModelField> fieldsList) {
		String name = "";
		if (fieldsList != null) {
			for (int i = 0; i < fieldsList.size(); i++) {
				String tempName = (String) (fieldsList.get(i)).getProperty("label");
				if (tempName == null) {
					tempName = (fieldsList.get(i)).getName();
				}
				name = name + tempName;
				name = name + ",";
			}
		}
		if (name.length() > 1) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}
	
	protected String getLabel(IModelObject item){
		String name = datamartLabels.getProperty(item, "label");
		if (name==null){
			name = item.getName();
		}
		return name;
	}
}
