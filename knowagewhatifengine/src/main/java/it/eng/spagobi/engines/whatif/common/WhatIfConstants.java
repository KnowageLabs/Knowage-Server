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
package it.eng.spagobi.engines.whatif.common;

import it.eng.spagobi.engines.whatif.cube.LeafProperty;

public class WhatIfConstants {

	public static final String VERSION_DIMENSION_NAME = "Version";
	public static final String VERSION_DIMENSION_UNIQUENAME = "[Version]";

	public static final String STANDALONE_HOST = "http://localhost:8080/";
	public static final String SATNDALONE_CONTEXT = "SpagoBIWhatIfEngine";

	public static final String WBVERSION_COLUMN_NAME = "version_name";
	public static final String WBVERSION_COLUMN_DESCRIPTION = "version_descr";
	public static final String WBVERSION_COLUMN_ID = "wbversion";

	public static final LeafProperty MEMBER_PROPERTY_LEAF = new LeafProperty();

	public static final String WHAT_IF_ANALYSIS_STATE = "WHAT_IF_ANALYSIS_STATE";
	public static final String CURRENT_WHAT_IF_ANALYSIS_STATE_VERSION = "0";

	public static final String MODEL_CONFIG = "modelConfig";
	public static final String CALCULATED_FIELDS = "calculatedFields";
	public static final String MDX_QUERY = "mdxQuery";

	public static final String ENGINE_NAME = "SpagoBIWhatIfEngine";

}
