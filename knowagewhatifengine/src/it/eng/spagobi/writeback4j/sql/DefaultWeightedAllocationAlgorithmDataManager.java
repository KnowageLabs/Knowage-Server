/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
