/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration.dao;

import it.eng.qbe.model.properties.SimpleModelProperties;

import java.util.Locale;

/**
 * The Interface IModelI18NPropertiesDAO.
 * 
 * @author Andrea Gioia
 */
public interface IModelI18NPropertiesDAO {
	
	/**
	 * Load i18n properties for the default locale. Equals to loadProperties(null)
	 * 
	 * @param locale the target locale
	 * 
	 * @return the loaded i18n properties
	 */
	SimpleModelProperties loadProperties();
	
	/**
	 * Load i18n properties for the given locale.
	 *
	 * @param locale the target locale
	 * 
	 * @return the loaded i18n properties
	 */
	SimpleModelProperties loadProperties(Locale locale);
}
