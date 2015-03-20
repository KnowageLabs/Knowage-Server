/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.scheduler.utils;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public abstract class JavaClassDestination implements IJavaClassDestination {

	BIObject biObj=null;
	byte[] documentByte=null;
	
	public abstract void execute();
	
	public byte[] getDocumentByte() {
		return documentByte;
	}

	public void setDocumentByte(byte[] documentByte) {
		this.documentByte = documentByte;
	}

	public BIObject getBiObj() {
		return biObj;
	}

	public void setBiObj(BIObject biObj) {
		this.biObj = biObj;
	}
}
