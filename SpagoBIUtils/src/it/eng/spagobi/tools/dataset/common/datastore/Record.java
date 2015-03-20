/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class Record implements IRecord,Serializable {

	IDataStore dataStore;
	List<IField> fields = new ArrayList<IField>();

	public Record() {
		super();
		this.fields = new ArrayList<IField>();
	}
	  
    public Record(IDataStore dataStore) {
		super();
		this.fields = new ArrayList<IField>();
		this.setDataStore(dataStore);
	}


	public IField getFieldAt(int position) {
		return (IField)fields.get(position);  	
    }
	
	public void appendField(IField field) {    	
		fields.add(field);	
    }
	
	public void insertField(int fieldIndex, IField field) {    	
		fields.add(fieldIndex, field);	
    }
	
	public List<IField> getFields() {
		return this.fields;
	}

	public void setFields(List<IField> fields) {
		this.fields = fields;
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	public String toString() {
		return "" + getFields().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
		Record other = (Record) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}
	
	

}
