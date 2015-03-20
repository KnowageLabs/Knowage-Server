/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.graph.serializer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelObject;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.io.IOException;
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
		jgen.writeStringField(SOURCE_FIELDS, value.getSourceFieldsString());
		jgen.writeStringField(TARGET_FIELDS, value.getTargetFieldsString());
		jgen.writeEndObject();
	}
	
	protected String getLabel(IModelObject item){
		String name = datamartLabels.getProperty(item, "label");
		if (name==null){
			name = item.getName();
		}
		return name;
	}
}
