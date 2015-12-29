/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.List;

import org.olap4j.metadata.Member;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public interface ISchemaRetriver {

	public IMemberCoordinates getMemberCordinates(Member member);

	public String getEditCubeTableName();

	public String getMeasureColumn(Member member) throws SpagoBIEngineException;

	public List<String> getColumnNamesList();

	public String getVersionColumnName();

	public String getVersionTableName();

	public List<String> getMeasuresColumn();

}
