/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import java.util.List;
import java.util.Locale;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;

public abstract class AbstractJavaClassLov implements IJavaClassLov {

	List<BIObjectParameter> BIObjectParameter;
	Locale locale;
	/**
	 * @return the bIObjectParameter
	 */
	public List<BIObjectParameter> getBIObjectParameter() {
		return BIObjectParameter;
	}
	/**
	 * @param bIObjectParameter the bIObjectParameter to set
	 */
	public void setBIObjectParameter(List<BIObjectParameter> bIObjectParameter) {
		BIObjectParameter = bIObjectParameter;
	}
	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}
	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	
}
