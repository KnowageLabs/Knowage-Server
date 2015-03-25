/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation;

import it.eng.spago.base.SourceBean;
import it.eng.spago.paginator.basic.ListIFace;

/**
 * Interface for list objects transformation.
 * 
 * @author sulis
 */
public interface IListObjectsTransformer {
	
	/**
	 * Transform.
	 * 
	 * @param data the data
	 * 
	 * @return the list i face
	 */
	public ListIFace transform(SourceBean data);
	
}
