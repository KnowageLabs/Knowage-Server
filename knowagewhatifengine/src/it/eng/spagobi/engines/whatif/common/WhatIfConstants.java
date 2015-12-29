/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
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
	public static final String MDX_QUERY = "mdxQuery";

	public static final String ENGINE_NAME = "SpagoBIWhatIfEngine";

}
