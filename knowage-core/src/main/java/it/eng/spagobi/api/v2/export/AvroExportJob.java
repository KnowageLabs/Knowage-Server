/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.knowage.tools.utils.DatabaseUtils;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Export dataset as Avro file.
 *
 * @author Marco Balestri
 */
public class AvroExportJob extends AbstractExportJob {

	private static final Logger logger = Logger.getLogger(AvroExportJob.class);

	private static final String ready = "ready";
	private static final String failed = "failed";
	private static final String data = "data";

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
	private static final int BIG_DECIMAL_PRECISION = 38;
	private static final int BIG_DECIMAL_SCALE = 6;

	private IDataSet dataSet;
	private IMetaData dsMeta;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	private SimpleDateFormat timestampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);

	private Path avroExportFolder;

	@Override
	protected void export(JobExecutionContext context) throws JobExecutionException {
		logger.debug("Start Avro export for dataSetId " + getDataSetId() + " with id " + getId() + " by user " + getUserProfile().getUserId());
		try {

			GenericData model = new GenericData();
			model.addLogicalTypeConversion(new Conversions.DecimalConversion());
			dataSet = getDataSet();
			dsMeta = dataSet.getMetadata();
			Schema schema = getSchema(dataSet);

			try (OutputStream exportFileOS = getDataOutputStream()) {
				clearStatusFiles();
				DatumWriter<GenericRecord> dout = new GenericDatumWriter<GenericRecord>(schema, model);

				try (DataFileWriter<GenericRecord> writer = new DataFileWriter<GenericRecord>(dout)) {
					writer.create(schema, exportFileOS);

					try (DataIterator iterator = dataSet.iterator()) {
						GenericData.Record avroRecord = new GenericData.Record(schema);

						while (iterator.hasNext()) {
							IRecord dataSetRecord = iterator.next();

							for (int i = 0; i <= dataSetRecord.getFields().size() - 1; i++) {
								Object value = getAvroValue(dataSetRecord, i);

								avroRecord.put(i, value);
							}
							writer.append(avroRecord);
						}
					}
					writer.flush();
				}
			}
		} catch (Exception e) {
			logger.error("Error during Avro file creation", e);
			setStatusFailed(e);
			throw new JobExecutionException(e);
		}

		setStatusReady();
		LogMF.info(logger, "Avro export completed for user {0}. DataSet is {1}. Final file: dimension (in bytes): {2,number}, path: [{3}], ",
				this.getUserProfile().getUserId(), this.getDataSet().getLabel(), getDataFile().toFile().length(), getDataFile().toString());

	}

	/**
	 * Avro does not accept BigDecimal, Date or Timestamp objects, therefore we need to convert them to primitive objects
	 */
	private Object getAvroValue(IRecord dataSetRecord, int i) {
		Object value = dataSetRecord.getFieldAt(i).getValue();
		if (value == null)
			return value;
		// convert Date and Timestamp to String and numbers to Double
		try {
			Class<?> type = dsMeta.getFieldType(i);
			if (isDate(type)) {
				value = dateFormatter.parse(value.toString()).getTime();
			} else if (isTimestamp(type)) {

				value = DatabaseUtils.timestampFormatter(value);

			} else if (BigDecimal.class.isAssignableFrom(type)) {
				BigDecimal bigDecimalValue = (BigDecimal) value;
				value = bigDecimalValue.setScale(BIG_DECIMAL_SCALE, RoundingMode.CEILING);
			} else if (Double.class.isAssignableFrom(type)) {
				value = Double.valueOf(value.toString());
			} else if (Integer.class.isAssignableFrom(type)) {
				value = Integer.valueOf(value.toString());
			} else if (Long.class.isAssignableFrom(type)) {
				value = Long.valueOf(value.toString());
			} else if (Float.class.isAssignableFrom(type)) {
				value = Float.valueOf(value.toString());
			} else if (String.class.isAssignableFrom(type)) {
				value = String.valueOf(value);
			} else {
				value = value.toString();
			}
		} catch (Exception e) {
			logger.error("Error getting Avro value {" + value + "}", e);
			value = value.toString();
		}
		return value;
	}

	private void clearStatusFiles() {
		try {
			Files.deleteIfExists(avroExportFolder.resolve(ready));
			Files.deleteIfExists(avroExportFolder.resolve(failed));
		} catch (IOException e) {
			logger.error("Error while clearing status files", e);
		}
	}

	private void setStatusReady() {
		try {
			Files.createFile(avroExportFolder.resolve(ready));
		} catch (IOException e) {
			logger.error("Cannot create ready status file", e);
		}
	}

	private void setStatusFailed(Exception cause) {
		Path failedStatusFilePath = avroExportFolder.resolve(failed);
		try {
			Files.createFile(failedStatusFilePath);
		} catch (IOException e) {
			logger.error("Cannot create failed status file");
		}
		try {
			PrintWriter pw = new PrintWriter(failedStatusFilePath.toFile());
			cause.printStackTrace(pw);
			pw.close();
		} catch (IOException e) {
			logger.error("Error while logging exception inside failed status file");
		}
	}

	private boolean isTimestamp(Class<?> fieldType) {
		return (Timestamp.class.isAssignableFrom(fieldType) || fieldType.getName().equalsIgnoreCase("oracle.sql.timestamp"));
	}

	private boolean isDate(Class<?> fieldType) {
		return (Date.class.isAssignableFrom(fieldType) || fieldType.getName().equalsIgnoreCase("oracle.sql.date"));
	}

	private Schema getSchema(IDataSet dataSet) throws JSONException {
		FieldAssembler<Schema> fieldAssembler = SchemaBuilder.record("DataSet").namespace("it.eng.spagobi.api.v2.export.AvroExportJob").fields();

		for (int i = 0; i <= dsMeta.getFieldCount() - 1; i++) {
			JSONObject metadata = new JSONObject();
			metadata.put("knColumnAlias", dsMeta.getFieldAlias(i));
			metadata.put("knJavaType", dsMeta.getFieldType(i).getName());

			fieldAssembler.name(dsMeta.getFieldName(i)).prop("knColumnAlias", dsMeta.getFieldAlias(i)).prop("knJavaType", dsMeta.getFieldType(i).getName())
					.prop("knFieldType", dsMeta.getFieldMeta(i).getFieldType().toString()).type().unionOf().nullType().and()
					.type(getType(dsMeta.getFieldType(i))).endUnion().noDefault();

		}

		Schema schema = fieldAssembler.endRecord();

		return schema;
	}

	private Schema getType(Class fieldType) {
		Schema ret = null;
		if (Integer.class.isAssignableFrom(fieldType)) {
			ret = Schema.create(Schema.Type.INT);
		} else if (BigDecimal.class.isAssignableFrom(fieldType)) {
			ret = LogicalTypes.decimal(BIG_DECIMAL_PRECISION, BIG_DECIMAL_SCALE).addToSchema(Schema.create(Schema.Type.BYTES));
		} else if (Float.class.isAssignableFrom(fieldType)) {
			ret = Schema.create(Schema.Type.FLOAT);
		} else if (Long.class.isAssignableFrom(fieldType)) {
			ret = Schema.create(Schema.Type.LONG);
		} else if (Double.class.isAssignableFrom(fieldType)) {
			ret = Schema.create(Schema.Type.DOUBLE);
		} else if (isTimestamp(fieldType) || isDate(fieldType)) {
			ret = Schema.create(Schema.Type.LONG);
		} else {
			ret = Schema.create(Schema.Type.STRING);
		}
		return ret;
	}

	@Override
	protected OutputStream getDataOutputStream() {
		try {
			String dsIdasString = Integer.toString(dataSet.getId());
			avroExportFolder = Paths.get(resourcePathAsStr, "dataPreparation", (String) userProfile.getUserId(), dsIdasString);
			Files.createDirectories(avroExportFolder);
			return Files.newOutputStream(avroExportFolder.resolve(data));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot create Avro file", e);
		}
	}

	@Override
	protected String extension() {
		return "avro";
	}

	@Override
	protected String mime() {
		return "application/avro";
	}

}
