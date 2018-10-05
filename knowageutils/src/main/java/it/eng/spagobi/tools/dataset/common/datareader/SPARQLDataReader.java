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
package it.eng.spagobi.tools.dataset.common.datareader;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SPARQLDataReader extends AbstractDataReader {

	private static final int ROWS_LIMIT_GUESS_TYPE_HEURISTIC = 10000;
	private static transient Logger logger = Logger.getLogger(SPARQLDataReader.class);

	public SPARQLDataReader() {
	}

	@Override
	public IDataStore read(Object data) {
		DataStore dataStore;
		MetaData dataStoreMeta;
		ResultSet resultSet;

		logger.debug("IN");

		try {
			dataStore = new DataStore();
			dataStoreMeta = new MetaData();
			dataStore.setMetaData(dataStoreMeta);

			Assert.assertNotNull(data, "Input object is null.");
			Assert.assertTrue(data instanceof ResultSet,
					"This SPARQLDataReader is able to get the data only from " + ResultSet.class + ". Input object is an instance of " + data.getClass());
			resultSet = (ResultSet) data;

			List<String> columnNames = resultSet.getResultVars();
			logger.debug("Retrieved columns: " + columnNames);

			FieldMetadata[] fieldsMetadata = initializeFieldsMetadata(columnNames);
			dataStoreMeta.setFieldsMeta(fieldsMetadata);

			parseResultSet(dataStore, dataStoreMeta, resultSet);

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Exception reading data", e);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private void parseResultSet(DataStore dataStore, MetaData dataStoreMeta, ResultSet resultSet) {
		List<String> columnNames = resultSet.getResultVars();
		for (; resultSet.hasNext();) {
			QuerySolution row = resultSet.nextSolution();
			IRecord record = new Record(dataStore);
			for (int i = 0; i < columnNames.size(); i++) {
				IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(i);

				String columnName = columnNames.get(i);
				RDFNode rdfNode = row.get(columnName);
				getValue(rdfNode, record);
				getMetaData(rdfNode, fieldMeta);
			}
			dataStore.appendRecord(record);
		}
	}

	private FieldMetadata[] initializeFieldsMetadata(List<String> columnNames) {
		FieldMetadata[] toReturn = new FieldMetadata[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			FieldMetadata fieldMeta = new FieldMetadata();
			fieldMeta.setName(columnNames.get(i));
			toReturn[i] = fieldMeta;
		}
		return toReturn;
	}

	private void getValue(RDFNode rdfNode, IRecord record) {
		if (rdfNode.isLiteral()) {
			Literal literal = (Literal) rdfNode;
			Object value = literal.getValue();
			Field field = new Field(value);
			record.appendField(field);
		} else if (rdfNode.isResource()) {
			Resource resource = (Resource) rdfNode;
			Object value = resource.getURI();
			Field field = new Field(value);
			record.appendField(field);
		}
	}

	private void getMetaData(RDFNode rdfNode, IFieldMetaData fieldMeta) {
		if (rdfNode.isLiteral()) {
			Literal literal = (Literal) rdfNode;
			Class classType = getClassType(literal);
			fieldMeta.setType(classType);
			FieldType type = getDefaultFieldType(literal);
			fieldMeta.setFieldType(type);
		} else if (rdfNode.isResource()) {
			fieldMeta.setType(String.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		}
	}

	private Class getClassType(Literal literal) {
		return literal.getValue().getClass();
	}

	private FieldType getDefaultFieldType(Literal literal) {
		if (literal.getValue().getClass().equals(Character.class) ||
				literal.getValue().getClass().equals(Boolean.class) ||
				literal.getValue().getClass().equals(Date.class) ||
				literal.getValue().getClass().equals(Timestamp.class) ||
				literal.getValue().getClass().equals(Byte.class) ||
				literal.getValue().getClass().equals(String.class)
				) {
			return FieldType.ATTRIBUTE;
		} else {
			return FieldType.MEASURE;
		}
	}

}
