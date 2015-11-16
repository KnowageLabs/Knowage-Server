/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.query;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractQueryTransformer implements IQueryTransformer {
	private IQueryTransformer previousTransformer;
	
	public AbstractQueryTransformer() {
		this(null);
	}
	
	public AbstractQueryTransformer(IQueryTransformer previousTransformer) {
		setPreviousTransformer( previousTransformer );
	}

	public IQueryTransformer getPreviousTransformer() {
		return previousTransformer;
	}

	public void setPreviousTransformer(IQueryTransformer previousTransformer) {
		this.previousTransformer = previousTransformer;
	}
	
	public boolean hasPreviousTransformer() {
		return  getPreviousTransformer() != null;
	}


	public Object transformQuery(Object query) {
		if( hasPreviousTransformer() ) {
			query = getPreviousTransformer().transformQuery(query);
		}
		return execTransformation(query);
	}
	
	public abstract Object execTransformation(Object query);
}
