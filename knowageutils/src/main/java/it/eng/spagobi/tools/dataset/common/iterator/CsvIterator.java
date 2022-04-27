/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.dataset.common.iterator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CsvIterator extends FileIterator implements DataIterator {

	private static transient Logger logger = Logger.getLogger(CsvIterator.class);

	private final String[] header;
	private final ICsvMapReader reader;
	private final long totLines;

	public CsvIterator(IMetaData metadata, String csvDelimiter, String csvQuote, String csvEncoding, Path filePath) throws IOException {
		super(metadata, filePath);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, csvEncoding);
		CsvPreference csvPreference = new CsvPreference.Builder(csvQuote.charAt(0), csvDelimiter.charAt(0), "\n").build();
		totLines = getTotalNumberOfLines(filePath);
		reader = new CsvMapReader(inputStreamReader, csvPreference);
		header = reader.getHeader(true);
	}

	private long getTotalNumberOfLines(Path filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
		long lines = 0;
		while (reader.readLine() != null)
			lines++;
		reader.close();
		return lines;
	}

	@Override
	public boolean hasNext() {
		return (reader.getLineNumber() < totLines);
	}

	@Override
	public IRecord next() {
		Map<String, String> contentsMap;
		IRecord record = new Record();
		try {
			contentsMap = reader.read(header);
			for (int i = 0; i < header.length; i++) {
				Object value = getValue(contentsMap.get(header[i]), metadata.getFieldMeta(i));
				IField field = new Field(value);
				logger.debug("Appending " + field);
				record.appendField(field);
			}
			return record;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private Object getValue(String stringValue, IFieldMetaData fieldMeta) {
		Class<?> clazz = fieldMeta.getType();
		if (stringValue != null && NumberUtils.isNumber(stringValue.replace(",", "."))) {
			return new BigDecimal(stringValue.replace(",", "."));
		}
		return clazz.cast(stringValue);
	}

}
