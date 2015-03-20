/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.bean;

import java.io.Serializable;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class JSONNetworkMappingMetadata implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5227922255614802058L;
	private String name;
	private String type;

	public JSONNetworkMappingMetadata(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	public JSONNetworkMappingMetadata(String name) {
		this(name,"string");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		JSONNetworkMappingMetadata other = (JSONNetworkMappingMetadata) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
