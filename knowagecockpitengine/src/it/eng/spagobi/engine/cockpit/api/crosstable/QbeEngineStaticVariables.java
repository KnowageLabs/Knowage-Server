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

package it.eng.spagobi.engine.cockpit.api.crosstable;

/**
 * @authors Alberto Alagna
 */
public class QbeEngineStaticVariables {

	// qbeEngineAnalysisState
	public static final String CATALOGUE = "CATALOGUE";
	public static final String WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public static final String WORKSHEET_DEFINITION_LOWER = "worksheetdefinition";
	public static final String DATASOURCE = "DATAMART_MODEL";
	public static final String CURRENT_QUERY_VERSION = "7";

	// SaveAnalysisStateAction
	public static final String CATALOGUE_NAME = "name";
	public static final String CATALOGUE_DESCRIPTION = "description";
	public static final String CATALOGUE_SCOPE = "scope";

	// LoadCrosstabAction
	public static final String CROSSTAB_DEFINITION = "crosstabDefinition";
	// public static final String OPTIONAL_FILTERS = "optionalfilters";
	public static final String FILTERS = "FILTERS";
	public static final String OPTIONAL_VISIBLE_COLUMNS = "visibleselectfields";
	public static final String DATASET_LABEL = "datasetLabel";
}
