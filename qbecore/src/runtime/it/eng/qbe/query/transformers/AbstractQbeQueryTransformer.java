/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.transformers;

import it.eng.qbe.query.Query;
import it.eng.spagobi.tools.dataset.common.query.AbstractQueryTransformer;
import it.eng.spagobi.utilities.assertion.Assert;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractQbeQueryTransformer extends AbstractQueryTransformer{

	public abstract Query execTransformation(Query query);
	
	public Object execTransformation(Object query) {		
		Assert.assertTrue(query instanceof Query, "Unable to transform object of type [" + query.getClass().getName() + "]");
		return execTransformation((Query)query);
	}

}
