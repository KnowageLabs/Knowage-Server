/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.bo;

import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;

public class DocumentMetadataProperty {
	
	private ObjMetadata meta = null;
	private ObjMetacontent metacontent = null;
	
	public ObjMetadata getMeta() {
		return meta;
	}
	public void setMetadataPropertyDefinition(ObjMetadata meta) {
		this.meta = meta;
	}
	public ObjMetacontent getMetacontent() {
		return metacontent;
	}
	public void setMetadataPropertyValue(ObjMetacontent metacontent) {
		this.metacontent = metacontent;
	}

}
