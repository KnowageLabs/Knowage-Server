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
package it.eng.qbe.model.structure;

import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractModelObject implements IModelObject {
	
	protected long id;	
	protected String name;
	protected Map<String,Object> properties;
	
	public long getId() {
		return id;
	}
	
	protected void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	protected void initProperties() {
		if(properties == null) {
			properties = new HashMap<String,Object>();
		}
	}
	
	public Map<String,Object> getProperties() {
		return properties;
	}
	
	public Object getProperty(String name) {
		return properties.get(name);
	}
	
	public String getPropertyAsString(String name) {
		return (String)getProperty(name);
	}
	
	public boolean getPropertyAsBoolean(String name) {
		boolean booleanValue = true;
		String stringValue = getPropertyAsString(name);
		//Assert.assertNotNull(stringValue, "Property [" + name + "] is not defined for item [" + this.name + "]");
		if(stringValue == null) {
			Assert.assertUnreachable("Property [" + name + "] is not defined for item [" + this.name + "]");
		}
		
		if( "TRUE".equalsIgnoreCase( stringValue ) ) {
			booleanValue = true;
		} else if( "FALSE".equalsIgnoreCase( stringValue ) ) {
			booleanValue = false;
		} else {
			Assert.assertUnreachable("Value [" + stringValue + "] is not vaid for the boolean property [" + this.name + "] of item [" + name + "]");
		}	
		
		return booleanValue;
	}
	
	public int getPropertyAsInt(String name) {
		int intValue = 0;
		String stringValue = getPropertyAsString(name);
		//Assert.assertNotNull(stringValue, "Property [" + name + "] is not defined for item [" + this.name + "]");
		if(stringValue == null) {
			Assert.assertUnreachable("Property [" + name + "] is not defined for item [" + this.name + "]");
		}
		
		intValue = Integer.parseInt(stringValue);
		
		return intValue;
	}
	
	public void setProperties(Map<String,Object> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.toUpperCase().hashCode());
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
		AbstractModelObject other = (AbstractModelObject) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.toUpperCase().equals(other.name.toUpperCase()))
			return false;
		return true;
	}
	

	
	
}
