/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
