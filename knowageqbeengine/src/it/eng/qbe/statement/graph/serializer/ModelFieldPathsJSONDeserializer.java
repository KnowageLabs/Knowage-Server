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

import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.IQueryField;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ModelFieldPathsJSONDeserializer extends JsonDeserializer<ModelFieldPaths>{
	private Collection<Relationship> relationShips;
//	private Graph<IModelEntity, Relationship> graph;
	private IModelStructure modelStructure;
	private Query query;
	
//	private static final int defaultPathWeight = 1;
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String QUERY_FIELD_NAME = "queryFieldName";
//	private static final String QUERY_FIELD_ALIAS = "queryFieldAlias";
//	private static final String ENTITY = "entity";
	private static final String CHOICES = "choices";
	private static final String NODES = "nodes";
	private static final String START = "start";
	private static final String END = "end";
	private static final String ACTIVE ="active";
	

	public ModelFieldPathsJSONDeserializer(Collection<Relationship> relationShips, IModelStructure modelStructure, Query query){
		this.relationShips = relationShips;
//		this.graph = graph;
		this.query = query;
		this.modelStructure = modelStructure;
	}
	
	
	@Override
	public ModelFieldPaths deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Set<PathChoice> choices = new HashSet<PathChoice>();
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        TextNode id = (TextNode)node.get(ID);
        TextNode name = (TextNode)node.get(NAME);
        TextNode queryFieldName = (TextNode)node.get(QUERY_FIELD_NAME);
       // TextNode entity = (TextNode)node.get(ENTITY);
        ArrayNode choicesJson = (ArrayNode) node.get(CHOICES);
        if(queryFieldName==null){
        	throw new JsonProcessingExceptionImpl("Error parsoing the field choices: The "+QUERY_FIELD_NAME+" must be valorized for each field"+node.toString());
        }
        if(choicesJson!=null && id!=null){
        	for(int i=0; i<choicesJson.size(); i++){
        		PathChoice pc = deserializePath(choicesJson.get(i));
        		if(pc!=null){
            		choices.add(pc);
        		}
        	}
        	IModelField field = modelStructure.getField(id.textValue());
        	IQueryField qf = getQueryField(queryFieldName.textValue());
        	
            if(field==null){
            	throw new JsonProcessingExceptionImpl("Error parsoing the field choices: can not find the field with id"+name.textValue()+" and name "+id.textValue()+" in the model structure");
            }
            
            if(qf==null){
            	return null;
            	//throw new FieldNotAttendInTheQuery("Error parsoing the field choices: can not find the field "+queryFieldName.textValue()+"in the model query");
            }
        	

        	return new ModelFieldPaths(qf,field, choices, true);
        }
        throw new JsonProcessingExceptionImpl("Can not deserialize the ModelFieldPaths. The mandatory fields are name and paths "+node.toString());
	}
	
	private IQueryField getQueryField(String queryFieldName){
		IQueryField qf=null;
		if(queryFieldName!=null){

			
			//check if it is a select field
			int fieldIndex = query.getSelectFieldIndex(queryFieldName);
			if(fieldIndex>=0){
				qf = query.getSelectFieldByIndex(fieldIndex);
			}
			//check if it is a where field
			if(qf==null){
				qf = query.getWhereFieldByName(queryFieldName);
			}
			//check if it is a having field
			if(qf==null){
				qf = query.getHavingFieldByName(queryFieldName);
			}

		}
		return qf;
	}
	
	public PathChoice deserializePath(JsonNode node) throws  JsonProcessingException {
		
		ArrayNode nodes = (ArrayNode) node.get(NODES);
		TextNode start = (TextNode)node.get(START);
		TextNode end = (TextNode)node.get(END);
		BooleanNode active = (BooleanNode)node.get(ACTIVE);
		boolean activebool = active!=null && active.asBoolean();
		if(activebool){
	        if(nodes!=null && start!=null && end!=null){
	        	List<Relationship> relations = new ArrayList<Relationship>();
	        	for(int i=0; i<nodes.size(); i++){
	        		relations.add(deserializeRelationship(nodes.get(i)));
	        	}
	        	return new PathChoice(relations, activebool);
	        }else{
	        	throw new JsonProcessingExceptionImpl("The nodes, start and end values of a path must be valorized. Error processing node "+node.toString());
	        }
		}

        return null;	
	}
	
	public Relationship deserializeRelationship(JsonNode node) throws  JsonProcessingException {
        TextNode sourceId = (TextNode)node.get(RelationJSONSerializer.SOURCE_ID);
        TextNode targetId = (TextNode)node.get(RelationJSONSerializer.TARGET_ID);
        TextNode relationId = (TextNode)node.get(RelationJSONSerializer.RELATIONSHIP_ID);
        if(relationId!=null){
        	return getRelationship(relationId.textValue(), sourceId, targetId);
        }else{
        	throw new JsonProcessingExceptionImpl("The relation name is mandatory in the relation definition "+node.toString());
        }
	}
	
	public Relationship getRelationship(String id, Object source, Object target) throws  JsonProcessingException {
		if(relationShips!=null){
			Iterator<Relationship> iter = relationShips.iterator();
			while (iter.hasNext()) {
				Relationship relationship = (Relationship) iter.next();
				if(relationship.getId().equals(id)){
					return relationship;
				}
			}
		}
		throw new JsonProcessingExceptionImpl("Can not find the relation with name "+id+" in the graph");
	}
}
