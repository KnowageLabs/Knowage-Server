/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.exception;

import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.Locale;

/**
 * 
 * @class exception throw when an error happens persisting the modification on
 *        the db
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class WhatIfPersistingTransformationException extends SpagoBIEngineException {

	private static final long serialVersionUID = 8811825156790676261L;
	private final String localizationMessage = "sbi.olap.weiteback.persist.error";
	private final CellTransformationsStack transformations;

	public WhatIfPersistingTransformationException(Locale locale, CellTransformationsStack transformations, Exception ex) {
		super("Error peristing the transformation", ex);
		this.transformations = transformations;

	}

	public CellTransformationsStack getTransformations() {
		return transformations;
	}

	public String getLocalizationmessage() {
		return localizationMessage;
	}

}
