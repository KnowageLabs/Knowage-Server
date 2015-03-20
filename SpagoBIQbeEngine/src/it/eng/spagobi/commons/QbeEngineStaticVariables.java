/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class QbeEngineStaticVariables {
	
	
	// qbeEngineAnalysisState
	public static final String CATALOGUE = "CATALOGUE";
	public static final String WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public static final String WORKSHEET_DEFINITION_LOWER = "worksheetdefinition";
	public static final String DATASOURCE = "DATAMART_MODEL";
	public static final String CURRENT_QUERY_VERSION = "7";
	
	
	//SaveAnalysisStateAction
	public static final String CATALOGUE_NAME = "name";	
	public static final String CATALOGUE_DESCRIPTION = "description";
	public static final String CATALOGUE_SCOPE = "scope";
	
	//LoadCrosstabAction
	public static final String CROSSTAB_DEFINITION = "crosstabDefinition";
	//public static final String OPTIONAL_FILTERS = "optionalfilters";
	public static final String FILTERS = "FILTERS";
	public static final String OPTIONAL_VISIBLE_COLUMNS = "visibleselectfields";
	
}
