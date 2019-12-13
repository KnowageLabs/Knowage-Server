package it.eng.spagobi.tools.dataset.common.iterator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

public class CsvStreamingOutput implements StreamingOutput {

	static protected Logger logger = Logger.getLogger(CsvStreamingOutput.class);

	private DataIterator iterator = null;
	private IDataStore dataStore = null;

	public CsvStreamingOutput(DataIterator iterator) {
		super();
		this.iterator = iterator;
	}

	public CsvStreamingOutput(IDataStore dataStore) {
		super();
		this.dataStore = dataStore;
	}

	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		if (iterator != null) {
			iteratorWrite(os);
		} else {
			dataStoreWrite(os);
		}
	}

	private void dataStoreWrite(OutputStream os) throws IOException, WebApplicationException {
		Helper.checkNotNull(dataStore, "datastore");

		Writer writer = new BufferedWriter(new OutputStreamWriter(os));

		int fieldCount = dataStore.getMetaData().getFieldCount();
		Assert.assertTrue(fieldCount > 0, "Impossible to get fields metadata");

		for (int i = 0; i < fieldCount; i++) {
			if (i != 0) {
				writer.write(",");
			}
			IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
			String columnName = fieldMeta.getAlias() != null ? fieldMeta.getAlias() : fieldMeta.getName();
			writer.write("\"" + columnName + "\"");
		}

		logger.debug("ResultSet iteration");
		for (int i = 0; i < Integer.MAX_VALUE && i < dataStore.getRecordsCount(); i++) {
			writer.write("\n");
			IRecord record = dataStore.getRecordAt(i);
			for (int j = 0; j < fieldCount; j++) {
				if (j != 0) {
					writer.write(",");
				}
				IField field = record.getFieldAt(j);
				Object fieldValue = field.getValue();
				writer.write("\"" + fieldValue + "\"");
			}
		}
		writer.flush();
	}

	private void iteratorWrite(OutputStream os) throws IOException, WebApplicationException {
		Helper.checkNotNull(iterator, "dataset iterator");

		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));

			int fieldCount = iterator.getMetaData().getFieldCount();
			Assert.assertTrue(fieldCount > 0, "Impossible to get fields metadata");

			for (int i = 0; i < fieldCount; i++) {
				if (i != 0) {
					writer.write(",");
				}
				IFieldMetaData fieldMeta = iterator.getMetaData().getFieldMeta(i);
				String columnName = fieldMeta.getAlias() != null ? fieldMeta.getAlias() : fieldMeta.getName();
				writer.write("\"" + columnName + "\"");
			}

			logger.debug("ResultSet iteration");
			while (iterator.hasNext()) {
				writer.write("\n");
				IRecord record = iterator.next();
				for (int j = 0; j < fieldCount; j++) {
					if (j != 0) {
						writer.write(",");
					}
					IField field = record.getFieldAt(j);
					Object fieldValue = field.getValue();
					writer.write("\"" + fieldValue + "\"");
				}
			}
			writer.flush();
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}
}
