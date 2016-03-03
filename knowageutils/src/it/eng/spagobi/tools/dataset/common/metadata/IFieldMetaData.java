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
