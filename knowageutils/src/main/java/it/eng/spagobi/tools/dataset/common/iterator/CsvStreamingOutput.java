package it.eng.spagobi.tools.dataset.common.iterator;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.IntStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

public class CsvStreamingOutput implements StreamingOutput {

	private static Logger logger = Logger.getLogger(CsvStreamingOutput.class);

	private IDataStore dataStore = null;
	private DataIterator iterator;
	private IMetaData metaData;
	private int visibleFieldCount;
	private List<IFieldMetaData> visibleFields;
	private List<Integer> indexesOfVisibleFields;

	public CsvStreamingOutput(DataIterator iterator) {
		super();
		this.iterator = iterator;
		this.metaData = iterator.getMetaData();

		List<IFieldMetaData> fieldsMetadata = this.metaData.getFieldsMeta();

		this.visibleFields = fieldsMetadata.stream()
			.filter(e -> (Boolean) e.getProperties().getOrDefault("visible", true))
			.collect(toList());
		this.indexesOfVisibleFields = IntStream.range(0, fieldsMetadata.size())
			.filter(e -> (Boolean) fieldsMetadata.get(e).getProperties().getOrDefault("visible", true))
			.boxed()
			.collect(toList());
		this.visibleFieldCount = visibleFields.size();
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
			IRecord currRecord = dataStore.getRecordAt(i);
			for (int j = 0; j < fieldCount; j++) {
				if (j != 0) {
					writer.write(",");
				}
				IField field = currRecord.getFieldAt(j);
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

			int fieldCount = visibleFieldCount;
			Assert.assertTrue(fieldCount > 0, "Impossible to get fields metadata");

			for (int i = 0; i < fieldCount; i++) {
				if (i != 0) {
					writer.write(",");
				}
				IFieldMetaData fieldMeta = visibleFields.get(i);
				String columnName = fieldMeta.getAlias() != null ? fieldMeta.getAlias() : fieldMeta.getName();
				writer.write("\"" + columnName + "\"");
			}

			logger.debug("ResultSet iteration");
			while (iterator.hasNext()) {
				writer.write("\n");
				IRecord currRecord = iterator.next();
				for (int j = 0; j < fieldCount; j++) {
					if (j != 0) {
						writer.write(",");
					}
					Integer realdFieldIndex = indexesOfVisibleFields.get(j);
					IField field = currRecord.getFieldAt(realdFieldIndex);
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
