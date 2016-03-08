/**
 * 
 */
package it.eng.spagobi.engines.whatif.version;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * @class SbiVersion
 * 
 * 
 */
public class SbiVersion {
	Integer id;
	String description;
	String name;

	public SbiVersion(Integer id, String name, String description) {
		super();
		this.id = id;
		this.description = description;
		this.name = name;
	}

	public SbiVersion(Integer id) {
		super();
		this.id = id;
		this.description = id.toString();
		this.name = id.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
