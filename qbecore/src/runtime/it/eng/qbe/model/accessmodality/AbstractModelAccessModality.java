/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.accessmodality;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractModelAccessModality implements IModelAccessModality{

	Boolean recursiveFiltering = Boolean.TRUE;
	
	public static final String ATTR_RECURSIVE_FILTERING = "recursiveFiltering";
	
	
	public boolean isEntityAccessible(IModelEntity entity) {
		return true;
	}

	public boolean isFieldAccessible(IModelField field) {
		return true;
	}

	public List getEntityFilterConditions(String entityName) {
		return new ArrayList();
	}

	public List getEntityFilterConditions(String entityName, Properties parameters) {
		return new ArrayList();
	}

	public Boolean getRecursiveFiltering() {
		return recursiveFiltering;
	}

	public void setRecursiveFiltering(Boolean recursiveFiltering) {
		this.recursiveFiltering = recursiveFiltering;
	}

}
