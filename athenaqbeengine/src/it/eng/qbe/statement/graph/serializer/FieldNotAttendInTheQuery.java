/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.serializer;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * use this exception when search for field in the query but the query does not contain it  
 */
public class FieldNotAttendInTheQuery extends SpagoBIEngineRuntimeException{

 static final long serialVersionUID = 819198637035544596L;

	public FieldNotAttendInTheQuery(String msg){
		super(msg);
	}
}
