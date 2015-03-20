/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.utilities.objects.Couple;


/**
 * @author Andrea Gioia
 */
public class ModelField extends AbstractModelNode implements IModelField {
		
	private boolean key;
	private String type;
	private int length;
	private int precision;
	
	//private String datamartName;	
	
	protected ModelField() {
		// in order to let subclass in this package to relax constraints imposed by the public constructor
		// DataMartField(String name, DataMartEntity parent). Ex. DataMartCalculatedField
		// can be created by themself without a pre-existing parent entity.
		initProperties();
	}
	
	public ModelField(String name, IModelEntity parent) {
		setStructure(parent.getStructure());
		setId( getStructure().getNextId() );		
		setName(name);
		setParent(parent);
		initProperties();		
	}

	public String getUniqueName() {
		
		String parentViewName = getParent().getPropertyAsString("parentView");
		if(parentViewName!= null) {
			return parentViewName+":"+getParent().getType() + ":" + getName();
		}
		if(getParent().getParent() == null) {
			return getParent().getType() + ":" + getName();
		}
		return getParent().getUniqueName() + ":" + getName();
	}
	
	
	public Couple getQueryName() {
		String fieldName = "";
		
		IModelEntity entity = getParent();
		if(entity.getParent() != null) {
			if(entity.getParent() instanceof ModelViewEntity){
				fieldName = getName();
				return new Couple (fieldName, entity);
			}
			fieldName = toLowerCase( entity.getName() );
			entity = entity.getParent();
		}
		while(entity.getParent() != null) {
			if(entity.getParent() instanceof ModelViewEntity){
				if(!fieldName.equalsIgnoreCase("")) fieldName +=  ".";
				fieldName = fieldName+getName();
				return new Couple (fieldName, entity);
			}
			fieldName = toLowerCase( entity.getName() ) + "." + fieldName;
			entity = entity.getParent();
		}		
		if(!fieldName.equalsIgnoreCase("")) fieldName +=  ".";
		fieldName += getName();
		
		return new Couple (fieldName, null);
	}
	
	
	private String toLowerCase(String str) {
		/*
		String head = str.substring(0,1);
		String tail = str.substring(1, str.length());
		
		return head.toLowerCase() + tail;
		*/
		return str;
	}
		
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}
	
	public String toString() {
		return getName() + "(id="+getId()
		+"; parent:" + (getParent()==null?"NULL": getParent().getName())
		+"; type="+type
		+"; length="+length
		+"; precision="+precision
		+")";
	}


	public IModelField clone(IModelEntity newParent){
		IModelField field = new ModelField(name, newParent);
		field.setKey(this.key);
		field.setType(this.type);
		field.setLength(this.length);
		field.setPrecision(this.precision);
		Map<String,Object> properties2 = new HashMap<String, Object>();
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String key= (String)iterator.next();
			String o = (String)properties.get(key);
			if(o==null){
				properties2.put(key.substring(0), null);
			}else{
				properties2.put(key.substring(0), o.substring(0));
			}
			
		}
		field.setProperties(properties2);
		return field;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelNode#getParentViews()
	 */
	public List<ModelViewEntity> getParentViews() {
		return super.getParentViews(this.getParent());
	}
	
}
