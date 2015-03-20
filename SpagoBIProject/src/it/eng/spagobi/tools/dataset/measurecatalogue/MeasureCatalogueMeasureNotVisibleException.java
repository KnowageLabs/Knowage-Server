/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class MeasureCatalogueMeasureNotVisibleException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 1256155514086200093L;
	private MeasureCatalogueMeasure measure;
	
	/**
	 * Builds a <code>MeasureCatalogueMeasureNotVisibleException</code>.
	 * 
	 * @param message Text of the exception
	 */
    public MeasureCatalogueMeasureNotVisibleException(String message) {
    	super(message);
    }
	
    /**
     * Builds a <code>MeasureCatalogueMeasureNotVisibleException</code>.
     * 
     * @param message Text of the exception
     * @param ex previous Throwable object
     */
    public MeasureCatalogueMeasureNotVisibleException( MeasureCatalogueMeasure measure) {
    	super("The measure "+measure.getAlias()+" of the dataset "+measure.getDsName()+" is not visible for the logged user");
    	this.measure = measure;
    }

	public MeasureCatalogueMeasure getMeasure() {
		return this.measure;
	}

}