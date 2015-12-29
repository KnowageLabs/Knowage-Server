/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j.mondrian;

import it.eng.spagobi.writeback4j.IXmlaDriver;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class MondrianDriver implements IXmlaDriver {

	private String olapSchema;

	public MondrianDriver(String olapSchema) {
		super();
		this.olapSchema = olapSchema;
	}

	public String getOlapSchema() {
		return olapSchema;
	}

	public void setOlapSchema(String olapSchema) {
		this.olapSchema = olapSchema;
	}

}
