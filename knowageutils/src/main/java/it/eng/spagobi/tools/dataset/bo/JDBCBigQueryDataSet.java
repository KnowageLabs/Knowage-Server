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

package it.eng.spagobi.tools.dataset.bo;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCBigQueryDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.AbstractDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JDBCStandardDataReader;

/**
 * @author Marco Libanori
 */
public class JDBCBigQueryDataSet extends AbstractJDBCDataset {

	private static transient Logger logger = Logger.getLogger(JDBCBigQueryDataSet.class);

	/**
	 * Instantiates a new empty JDBC RedShift data set.
	 */
	public JDBCBigQueryDataSet() {
		super();
		setDataProxy(new JDBCBigQueryDataProxy());
		setDataReader(createDataReader());
	}

	public JDBCBigQueryDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
	}

	@Override
	protected AbstractDataReader createDataReader() {
		return new JDBCStandardDataReader();
	}

}
