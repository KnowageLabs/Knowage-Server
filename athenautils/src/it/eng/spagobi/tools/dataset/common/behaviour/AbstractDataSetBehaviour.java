/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.behaviour;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractDataSetBehaviour implements IDataSetBehaviour {
	private String id;
	private IDataSet targetDataSet;
	
	public AbstractDataSetBehaviour(String id, IDataSet targetDataSet) {
		setId(id);
		setTargetDataSet(targetDataSet);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IDataSet getTargetDataSet() {
		return targetDataSet;
	}

	public void setTargetDataSet(IDataSet targetDataSet) {
		this.targetDataSet = targetDataSet;
	}
}
