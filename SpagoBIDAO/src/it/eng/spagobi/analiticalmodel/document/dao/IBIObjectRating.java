/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface IBIObjectRating extends ISpagoBIDao{
	
	
	/**
	 * Implements the query to insert a rating for a BI Object.
	 * 
	 * @param obj the obj
	 * @param userid the userid
	 * @param rating the rating
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void voteBIObject(BIObject obj,String userid, String rating) throws EMFUserError;
	
	/**
	 * Implements the query to calculate the medium rating for a BI Object.
	 * 
	 * @param obj the obj
	 * 
	 * @return The BI object medium rating
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Double calculateBIObjectRating(BIObject obj) throws EMFUserError;

}
