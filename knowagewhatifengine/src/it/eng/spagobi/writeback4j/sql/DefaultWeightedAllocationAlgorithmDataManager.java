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

package it.eng.spagobi.writeback4j.sql;

import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.ISchemaRetriver;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Member;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class DefaultWeightedAllocationAlgorithmDataManager extends AbstractUpdatingAlgotithmsDataManager {

	IDataSource dataSource;

	public static transient Logger logger = Logger.getLogger(DefaultWeightedAllocationAlgorithmDataManager.class);

	public DefaultWeightedAllocationAlgorithmDataManager(ISchemaRetriver retriver, IDataSource dataSource) {
		this.retriver = retriver;
		this.dataSource = dataSource;
	}

	/**
	 * Build the update statement for the measure
	 * 
	 * @param buffer
	 *            the buffer of the query
	 * @param measure
	 *            the measure to update
	 * @param prop
	 *            the ratio
	 */
	@Override
	protected void buildUpdate(StringBuffer buffer, Member measure, Object... values) throws SpagoBIEngineException {
		String measureColumn = null;
		try {
			measureColumn = retriver.getMeasureColumn(measure);
		} catch (SpagoBIEngineException e) {
			logger.error("Error loading the column for the table measure " + measure.getName(), e);
			throw new SpagoBIEngineException("Error loading the column for the table measure " + measure.getName(), e);
		}

		buffer.append("update ");
		buffer.append(retriver.getEditCubeTableName());
		buffer.append(" " + getCubeAlias());
		buffer.append(" set " + measureColumn + " = " + measureColumn + "*" + values[0]);

	}

	public boolean isUseInClause() {
		return useInClause;
	}

	public void setUseInClause(boolean useInClause) {
		this.useInClause = useInClause;
	}

}
