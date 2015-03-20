/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.metadata;

import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *         Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IFieldMetaData {

	public static final String DECIMALPRECISION = "decimalPrecision";
	public static final String ORDERTYPE = "oprdertype";
	
	public static final String PROPERTY_ATTRIBUTE_PRESENTATION = "attributePresentation";
	public static final String PROPERTY_ATTRIBUTE_PRESENTATION_CODE = "code";
	public static final String PROPERTY_ATTRIBUTE_PRESENTATION_DESCRIPTION = "description";
	public static final String PROPERTY_ATTRIBUTE_PRESENTATION_CODE_AND_DESCRIPTION = "both";
	
	public enum FieldType {ATTRIBUTE, MEASURE}

	String getName();
	String getAlias();
	Class getType();
	FieldType getFieldType();
	Object getProperty(String propertyName);

	void setName(String name);
	void setAlias(String alias);
	void setType(Class type);
	void setProperty(String propertyName, Object propertyValue);
	void setFieldType(FieldType fieldType);
	void deleteProperty(String propertyName);
	Map getProperties();
	void setProperties(Map properties);
}
