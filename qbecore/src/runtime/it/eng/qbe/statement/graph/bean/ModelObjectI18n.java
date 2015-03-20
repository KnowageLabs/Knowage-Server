/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.bean;

import it.eng.qbe.model.structure.IModelObject;

/**
 * 
 * A simple bean that contains a IModelObject.
 * We use it instead of IModelObject to make easier the localization ion the serializer of the qbeengine 
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class ModelObjectI18n {

	private IModelObject object;

	public ModelObjectI18n(IModelObject object) {
		super();
		this.object = object;
	}

	public IModelObject getObject() {
		return object;
	}
	
	
	
}
