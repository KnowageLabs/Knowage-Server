/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.sbidocument;

import java.util.Locale;

import it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver;


/**
 * @author Giulio Gavardi
 */

public interface SbiDocumentService {

	SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(String token,String user,Integer biObjId, String language, String country);

	String getDocumentAnalyticalDriversJSON(String token,String user,Integer biObjId, String language, String country);


}
