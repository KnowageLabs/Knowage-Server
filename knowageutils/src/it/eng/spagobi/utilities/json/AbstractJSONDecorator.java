/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.json;


import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public abstract class AbstractJSONDecorator {
	
	AbstractJSONDecorator nextDecorator;

	public void decorate(JSONObject json) {
		this.doDecoration(json);
		if ( getNextDecorator() != null ) {
			getNextDecorator().decorate(json);
		}
	}
	 
	protected abstract void doDecoration(JSONObject json);
	
	public AbstractJSONDecorator getNextDecorator() {
		return nextDecorator;
	}

	public void setNextDecorator(AbstractJSONDecorator nextDecorator) {
		this.nextDecorator = nextDecorator;
	}
}
