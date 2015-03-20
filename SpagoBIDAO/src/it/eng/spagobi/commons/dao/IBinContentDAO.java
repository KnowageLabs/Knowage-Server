/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 13-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFInternalError;

public interface IBinContentDAO extends ISpagoBIDao{
	
	/**
	 * Gets the bin content.
	 * 
	 * @param binId the bin id
	 * 
	 * @return the bin content
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public byte[] getBinContent(Integer binId) throws EMFInternalError;
	
}