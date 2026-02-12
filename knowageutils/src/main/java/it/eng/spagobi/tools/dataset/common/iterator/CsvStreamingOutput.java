package it.eng.spagobi.tools.dataset.common.iterator;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
		ResultSetMetaData resultSetMetaData;
		List<IFieldMetaData> filteredMetadata = new ArrayList<>();
		try {
			resultSetMetaData = ((it.eng.spagobi.tools.dataset.common.iterator.ResultSetIterator) iterator).getRs().getMetaData();
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				String columnName = resultSetMetaData.getColumnName(i + 1);
				Optional<IFieldMetaData> fieldMetaData = fieldsMetadata.stream().filter(f -> f.getName().equalsIgnoreCase(columnName)).findFirst();
				fieldMetaData.ifPresent(filteredMetadata::add);
			}
		} catch (Exception e) {
            try {
                IMetaData metadati = iterator.getMetaData();
                for (int i = 0; i < metadati.getFieldCount(); i++) {
                    filteredMetadata.add(metadati.getFieldMeta(i));
                }
                while (iterator.hasNext()) {
                    IRecord currRecord = iterator.next();
                    for (int j = 0; j < metadati.getFieldCount(); j++) {
                        IField field = currRecord.getFieldAt(j);
                        Object fieldValue = field.getValue();
                        if (fieldValue != null) {
                            filteredMetadata.add(metadati.getFieldMeta(j));
                        }
                    }
                }

            } catch (Exception ex) {
                logger.error("Error while retrieving metadata from iterator", ex);
                throw new RuntimeException("Error while retrieving metadata from iterator", ex);
            }
		}

        if (filteredMetadata.isEmpty()) {
            logger.error("No metadata found for the iterator");
            throw new RuntimeException("No metadata found for the iterator");
        }

		this.visibleFields = filteredMetadata.stream().filter(e -> {
			Object o = e.getProperties().get("visible");
			return o == null || Boolean.valueOf(o.toString());
		}).collect(toList());
		this.indexesOfVisibleFields = IntStream.range(0, filteredMetadata.size()).filter(e -> {
			Object o = filteredMetadata.get(e).getProperties().get("visible");
			return o == null || Boolean.valueOf(o.toString());
		}).boxed().collect(toList());
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
				if (fieldValue != null) {
                    if (fieldValue instanceof String) {
                        fieldValue = ((String) fieldValue).trim();
                    }
                }
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
	                if (fieldValue != null) {
	                    if (fieldValue instanceof String) {
	                        fieldValue = ((String) fieldValue).trim();
	                    }
	                }
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
