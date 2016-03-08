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
