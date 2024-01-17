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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.JDBCPostgreSQLDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.AbstractDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JDBCStandardDataReader;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JDBCPostgreSQLDataSet extends JDBCDataSet {

	private static final Logger LOGGER = LogManager.getLogger(JDBCPostgreSQLDataSet.class);

	/**
	 * Instantiates a new empty JDBC PostgreSQL data set.
	 */
	public JDBCPostgreSQLDataSet() {
		setDataProxy(new JDBCPostgreSQLDataProxy());
		setDataReader(createDataReader());
	}

	public JDBCPostgreSQLDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
	}

	@Override
	protected AbstractDataReader createDataReader() {
		return new JDBCStandardDataReader();
	}

	@Override
	public DataIterator iterator() {
		LOGGER.debug("IN");
		try {
			IMetaData currMetadata = getMetadata();
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) getBehaviour(QuerableBehaviour.class.getName());
			String statement = querableBehaviour.getStatement();
			LOGGER.debug("Obtained statement [{}]", statement);
			dataProxy.setStatement(statement);
			JDBCDataProxy jdbcDataProxy = (JDBCDataProxy) dataProxy;
			IDataSource dataSource = jdbcDataProxy.getDataSource();
			Assert.assertNotNull(dataSource, "Invalid datasource");
			Connection connection = dataSource.getConnection();
			Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			connection.setAutoCommit(false); // PostgreSQL requires disabling auto-commit for setFetchSize to work
			stmt.setFetchSize(5000);

			ResultSet rs = (ResultSet) dataProxy.getData(dataReader, stmt);
			return new ResultSetIterator(connection, stmt, rs, currMetadata);
		} catch (ClassNotFoundException | SQLException | NamingException e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

}
