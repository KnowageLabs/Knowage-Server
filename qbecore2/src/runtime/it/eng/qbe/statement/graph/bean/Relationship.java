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
package it.eng.qbe.statement.graph.bean;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.util.List;

import org.jgrapht.graph.DefaultEdge;

public class Relationship extends DefaultEdge implements Comparable<Relationship>{
	
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String name;
	
	List<IModelField> sourceFields;
	List<IModelField> targetFields;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public IModelEntity getSourceEntity() {
		IModelEntity toreturn = (IModelEntity)this.getSource();
		if(toreturn==null && sourceFields!=null && sourceFields.size()>0){
			toreturn = sourceFields.get(0).getParent();
		}
		return toreturn;
	} 
	
	public List<IModelField> getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(List<IModelField> sourceFields) {
		this.sourceFields = sourceFields;
	}
	
	public IModelEntity getTargetEntity() {
		IModelEntity toreturn = (IModelEntity)this.getTarget();
		if(toreturn==null && targetFields!=null && targetFields.size()>0){
			toreturn = targetFields.get(0).getParent();
		}
		return toreturn;
	}

	public List<IModelField> getTargetFields() {
		return targetFields;
	}

	public void setTargetFields(List<IModelField> targetFields) {
		this.targetFields = targetFields;
	}

	public String getName() {
		return name;
	}

	public String getId(){
		String id = name;
		if(getSourceEntity().getUniqueName().hashCode()>getTargetEntity().getUniqueName().hashCode()){
			 id = id+"-"+getSourceEntity().getUniqueName()+"-"+getTargetEntity().getUniqueName();
		}else{
			 id = id+"-"+getTargetEntity().getUniqueName()+"-"+getSourceEntity().getUniqueName();
		}
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getTargetFieldsString(){
		String fields = "";
		if(this.targetFields!=null){
			for(int i=0; i<this.targetFields.size(); i++){
				fields = fields+ getFieldName(this.targetFields.get(i));		
				fields=fields+",";
			}
		}
		if(fields.length()>1){
			fields = fields.substring(0,fields.length()-1);
		}
		return fields;
	}
	
	public String getSourceFieldsString(){
		String fields = "";
		if(this.sourceFields!=null){
			for(int i=0; i<this.sourceFields.size(); i++){
				fields = fields+ getFieldName(this.sourceFields.get(i));		
				fields=fields+",";
			}
		}
		if(fields.length()>1){
			fields = fields.substring(0,fields.length()-1);
		}
		return fields;
	}
	
	public String getFieldName(IModelField field){
		String fieldName = field.getName();
		if(field!=null){
			
			//removes the relation from the name of the field
			if((fieldName.indexOf("rel_")==0) && (fieldName.contains("."))){
				String joinColumnName = field.getPropertyAsString("joinColumnName");
				if(joinColumnName==null){
					joinColumnName = field.getName();
				}
				fieldName = SqlUtils.unQuote(joinColumnName);
			}			
			
			//removes the compId prefix
			if((fieldName.indexOf("compId.")==0) ){
				fieldName = fieldName.substring(7);
			}
			
		}
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relationship other = (Relationship) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return getId();
	}

	public int compareTo(Relationship arg0) {
		if(arg0.getId()==null){
			return 1;
		}
		if(this.getId()==null){
			return -1;
		}
		return this.getId().compareTo(arg0.getId());
	}
	
	
}