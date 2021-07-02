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

package it.eng.spagobi.engines.drivers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

/**
 * Defines the methods implements by the SpagoBI drivers that, starting from a SpagoBI BIOBject, produce the parameters for a specific engine to which they are
 * associated. The names anv values of the map parameters will be used by the system to produce a POST request to the engine application. Each driver can
 * extract and trasform the BIParameter of the BIObject in order to create a a right request based on the engine specificaion. The methods can be used also to
 * do some setting operation like for example handshake security requests.
 */
public interface IEngineDriver {

	public static final String COUNTRY = "knowage_sys_country";
	public static final String LANGUAGE = "knowage_sys_language";

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
	 *
	 * @param profile  Profile of the user
	 * @param roleName the name of the execution role
	 * @param biobject the biobject
	 *
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName);

	/**
	 * Returns a map of parameters which will be send in the request to the engine application.
	 *
	 * @param subObject SubObject to execute
	 * @param profile   Profile of the user
	 * @param roleName  the name of the execution role
	 * @param object    the object
	 *
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName);

	/**
	 * Returns the EngineURL for the creation of a new template for the document.
	 *
	 * @param biobject the biobject
	 * @param profile  the profile
	 *
	 * @return the EngineURL for the creation of a new template for the document
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest;

	/**
	 * Returns the EngineURL for the modification of the document template.
	 *
	 * @param biobject the biobject
	 * @param profile  the profile
	 *
	 * @return the EngineURL for the modification of the document template
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest;

	/**
	 * Returns the template elaborated.
	 *
	 * @param byte[]  the template
	 * @param profile the profile
	 *
	 * @return the byte[] with the modification of the document template
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public byte[] ElaborateTemplate(byte[] template) throws InvalidOperationRequest;

	/**
	 * Returns the template elaborated.
	 *
	 * @param byte[]  the template
	 * @param profile the profile
	 *
	 * @return the byte[] with the modification of the document template
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public void applyLocale(Locale locale);

	public ArrayList<String> getDatasetAssociated(byte[] contentTemplate) throws JSONException;

	public ArrayList<String> getFunctionsAssociated(byte[] contentTemplate) throws JSONException;

	public List<DefaultOutputParameter> getDefaultOutputParameters();

	/**
	 * Specially provided method for custom-made output category parameters for the SUNBURST chart.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 *
	 *         Handles OLAP cross-navigation parameters in WhatIfDriver implementation.
	 *
	 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
	 *
	 */
	public List<DefaultOutputParameter> getSpecificOutputParameters(List categories);

	/**
	 * Method used for special chart types, that need exclusion of some of default output parameters).
	 *
	 * Example: WORDCLOUD, PARALLEL and CHORD chart types.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public List<DefaultOutputParameter> getSpecificOutputParameters(String specificChartType);

}
