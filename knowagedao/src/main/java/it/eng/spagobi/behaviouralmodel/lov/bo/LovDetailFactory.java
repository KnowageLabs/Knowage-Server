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

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

import java.util.Iterator;
import java.util.List;

/**
 * Defines methods to create implementation of the ILovDetail interface
 */
public class LovDetailFactory {
	
	public static final String JAVACLASSLOV = "JAVACLASSLOV";
	public static final String SCRIPTLOV 	= "SCRIPTLOV";
	public static final String QUERYLOV 	= "QUERY";
	public static final String FIXEDLISTLOV = "FIXLISTLOV";
	public static final String DATASETLOV   = "DATASET";

	
	/**
	 * Creates an instace of a lov class (which implements ILovDetail interface)
	 * starting from the xml definition of the lov.
	 * 
	 * @param dataDefinition the xml definition of the lov
	 * 
	 * @return The instance of the class which implements the ILovDetail interface
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public static ILovDetail getLovFromXML(String dataDefinition) throws SourceBeanException {
		dataDefinition = resolveRetroCompatibility(dataDefinition);
		ILovDetail lov = null;
		dataDefinition = dataDefinition.trim();
		if(dataDefinition.startsWith("<" + JAVACLASSLOV )) {
			lov = new JavaClassDetail(dataDefinition);
		} else if(dataDefinition.startsWith("<" + SCRIPTLOV )) {
			lov = new ScriptDetail(dataDefinition);
		} else if(dataDefinition.startsWith("<" + QUERYLOV )) {
			lov = new QueryDetail(dataDefinition);
		} else if (dataDefinition.startsWith("<" + FIXEDLISTLOV)) {
			lov = new FixedListDetail(dataDefinition);
		} else if (dataDefinition.startsWith("<" + DATASETLOV)) {
			lov = new DatasetDetail(dataDefinition);
		}
		
		return lov;
	}
	
	/**
	 * Resolves retrocompatibility problems regarding lov prodiver definition
	 * @param dataDefinition The String
	 * @return
	 * @throws SourceBeanException
	 */
	private static String resolveRetroCompatibility(String dataDefinition) throws SourceBeanException {
		if (dataDefinition.startsWith("<LOV>")) {
			SourceBean oldSB = SourceBean.fromXMLString(dataDefinition);
			String toReturn = "<" + FIXEDLISTLOV + "><ROWS>";
			List elements = oldSB.getAttributeAsList("LOV-ELEMENT");
			Iterator elementsIt = elements.iterator();
			while (elementsIt.hasNext()) {
				SourceBean element = (SourceBean) elementsIt.next();
				String description = (String) element.getAttribute("DESC");
				String value = (String) element.getAttribute("VALUE");
				toReturn += "<ROW DESCRIPTION=\"" + description +  "\" VALUE=\"" + value +  "\" />";
			}
			toReturn += "</ROWS><VALUE-COLUMN>VALUE</VALUE-COLUMN>" +
					"<DESCRIPTION-COLUMN>DESCRIPTION</DESCRIPTION-COLUMN>" +
					"<VISIBLE-COLUMNS>DESCRIPTION</VISIBLE-COLUMNS>" + 
					"<INVISIBLE-COLUMNS>VALUE</INVISIBLE-COLUMNS>" +
					"</" + FIXEDLISTLOV + ">";
			return toReturn;
		}
		if (dataDefinition.startsWith("<ROWS>")) {
			SourceBean oldSB = SourceBean.fromXMLString(dataDefinition);
			String toReturn = "<" + FIXEDLISTLOV + "><ROWS>";
			List elements = oldSB.getAttributeAsList("ROW");
			Iterator elementsIt = elements.iterator();
			while (elementsIt.hasNext()) {
				SourceBean element = (SourceBean) elementsIt.next();
				String description = (String) element.getAttribute("DESCRIPTION");
				String value = (String) element.getAttribute("NAME");
				toReturn += "<ROW DESCRIPTION=\"" + description +  "\" VALUE=\"" + value +  "\" />";
			}
			toReturn += "</ROWS><VALUE-COLUMN>VALUE</VALUE-COLUMN>" +
					"<DESCRIPTION-COLUMN>DESCRIPTION</DESCRIPTION-COLUMN>" +
					"<VISIBLE-COLUMNS>DESCRIPTION</VISIBLE-COLUMNS>" + 
					"<INVISIBLE-COLUMNS>VALUE</INVISIBLE-COLUMNS>" +
					"</" + FIXEDLISTLOV + ">";
			return toReturn;
		}
		return dataDefinition;
	}
	
	/**
	 * Gets the code of the lov type based on the input lovprovider string.
	 * 
	 * @param lovprovider the lovprovider string of the lov
	 * 
	 * @return the string code of the love type
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public static String getLovTypeCode(String lovprovider) throws SourceBeanException {
		String type = "";
		lovprovider = lovprovider.trim();
		if(lovprovider.startsWith("<" + JAVACLASSLOV )) {
			type = "JAVA_CLASS";
		} else if(lovprovider.startsWith("<" + SCRIPTLOV )) {
			type = "SCRIPT";
		} else if(lovprovider.startsWith("<" + QUERYLOV )) {
			type = "QUERY";
		} else if( lovprovider.startsWith("<" + FIXEDLISTLOV ) || lovprovider.startsWith("<" + "ROWS" ) ) {
			type = "FIXED_LIST";
		} else if( lovprovider.startsWith("<" + DATASETLOV )) {
			type = "DATASET";
		}
		return type;
	}
}
