package it.eng.spagobi.tools.dataset.common.datareader;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SPARQLDataReader extends AbstractDataReader {

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

			IField field = null;
			List<String> columnNames = resultSet.getResultVars();
			logger.debug("Retrieved columns: " + columnNames);

			for (int i = 0; i < columnNames.size(); i++) {
				FieldMetadata fieldMeta = new FieldMetadata();
				fieldMeta.setName(columnNames.get(i));
				dataStoreMeta.addFiedMeta(fieldMeta);

			}

			for (; resultSet.hasNext();) {
				QuerySolution row = resultSet.nextSolution();
				IRecord record = new Record(dataStore);
				for (int i = 0; i < columnNames.size(); i++) {

					IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(i);
					String columnName = columnNames.get(i);
					RDFNode rdfNode = row.get(columnName);
					isLiteralOrResource(rdfNode, fieldMeta, field, record);
				}
				dataStore.appendRecord(record);
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Exception reading data", e);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private void isLiteralOrResource(RDFNode rdfNode, IFieldMetaData fieldMeta, IField field, IRecord record) {
		if (rdfNode.isLiteral()) {
			Literal literal = (Literal) rdfNode;
			Object value = literal.getValue();
			setClassAndFieldType(literal, fieldMeta);
			field = new Field(value);
			record.appendField(field);
		} else if (rdfNode.isResource()) {
			Resource resource = (Resource) rdfNode;
			Object value = resource.getURI();
			fieldMeta.setType(String.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
			field = new Field(value);
			record.appendField(field);
		}
	}

	// metadata - field and class type
	private void setClassAndFieldType(Literal literal, IFieldMetaData fieldMeta) {

		if (literal.getValue().getClass().equals(String.class)) {
			fieldMeta.setType(String.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		} else if (literal.getValue().getClass().equals(Character.class)) {
			fieldMeta.setType(Character.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		} else if (literal.getValue().getClass().equals(Integer.class)) {
			fieldMeta.setType(Integer.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(Double.class)) {
			fieldMeta.setType(Double.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(Float.class)) {
			fieldMeta.setType(Float.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(Boolean.class)) {
			fieldMeta.setType(Boolean.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		} else if (literal.getValue().getClass().equals(Byte.class)) {
			fieldMeta.setType(Byte.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		} else if (literal.getValue().getClass().equals(Short.class)) {
			fieldMeta.setType(Short.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(BigInteger.class)) {
			fieldMeta.setType(BigInteger.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(BigDecimal.class)) {
			fieldMeta.setType(BigDecimal.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(Long.class)) {
			fieldMeta.setType(Long.class);
			fieldMeta.setFieldType(FieldType.MEASURE);
		} else if (literal.getValue().getClass().equals(Date.class)) {
			fieldMeta.setType(Date.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		} else if (literal.getValue().getClass().equals(Timestamp.class)) {
			fieldMeta.setType(Timestamp.class);
			fieldMeta.setFieldType(FieldType.ATTRIBUTE);
		}

	}

}
