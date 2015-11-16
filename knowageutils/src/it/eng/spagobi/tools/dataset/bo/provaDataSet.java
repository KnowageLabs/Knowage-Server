/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class provaDataSet implements IJavaClassDataSet {


	public String getValues(Map userProfileAttributes, Map parameters) {
	
		String result = "<ROWS>";
		result += "<ROW VALUE=\"";
		int i = 2*100;
		result += new Integer (i).toString() +"\"/>";
		result += "</ROWS>";
		return result;
		
	}

	public List getNamesOfProfileAttributeRequired(){
		List a=new ArrayList();
		a.add("month");
		return a;
	}
	
	
}
