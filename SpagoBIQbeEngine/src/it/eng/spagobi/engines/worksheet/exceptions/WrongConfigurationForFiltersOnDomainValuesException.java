/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.exceptions;

public class WrongConfigurationForFiltersOnDomainValuesException extends Exception {

	private static final long serialVersionUID = 52324913751033593L;

    public WrongConfigurationForFiltersOnDomainValuesException() {
    	super();
    }

    public WrongConfigurationForFiltersOnDomainValuesException(String message) {
    	super(message);
    }

    public WrongConfigurationForFiltersOnDomainValuesException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongConfigurationForFiltersOnDomainValuesException(Throwable cause) {
        super(cause);
    }
	
}
