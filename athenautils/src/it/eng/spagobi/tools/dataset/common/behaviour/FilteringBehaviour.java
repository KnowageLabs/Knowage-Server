/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.behaviour;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.List;
import java.util.Map;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FilteringBehaviour extends AbstractDataSetBehaviour {
	
	public static final String ID = FilteringBehaviour.class.getName();
	
	private Map<String, List<String>> filters = null;
	
	public FilteringBehaviour(IDataSet targetDataSet) {
		super(FilteringBehaviour.class.getName(), targetDataSet);
	}

	public void setFilters(Map<String, List<String>> filters) {
		this.filters = filters;
	}
	
	public Map<String, List<String>> getFilters() {
		return this.filters;
	}
	
}
