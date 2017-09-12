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
