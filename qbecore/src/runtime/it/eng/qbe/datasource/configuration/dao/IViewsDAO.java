/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration.dao;

import it.eng.qbe.model.structure.IModelViewEntityDescriptor;

import java.util.List;

import org.json.JSONObject;

/**
 * The Interface IViewsDAO.
 * 
 * @author Andrea Gioia
 */
public interface IViewsDAO {
	
	/**
	 * Load views.
	 * 
	 * @return the model views
	 */
	List<IModelViewEntityDescriptor> loadModelViews();
	
	/**
	 * Save model views.
	 *
	 * @param properties the model views
	 */
	void saveModelViews(List<JSONObject> views);
}
