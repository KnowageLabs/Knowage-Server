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
package it.eng.spagobi.tools.dataset.common.iterator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import it.eng.knowage.encryption.EncryptionConfiguration;
import it.eng.knowage.encryption.EncryptionPreferencesRegistry;
import it.eng.knowage.encryption.EncryptorFactory;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ResultSetIterator implements DataIterator {

	private static final Logger LOGGER = LogManager.getLogger(ResultSetIterator.class);

	private final CachedRowSet cache;
	private final IMetaData metadata;
	private final int columnCount;
	private boolean needDecryption = false;
	private final List<IFieldMetaData> decryptableField = new ArrayList<>();
	private final Map<Integer, IFieldMetaData> decryptableFieldByIndex = new LinkedHashMap<>();
	private PBEStringEncryptor encryptor;

	public ResultSetIterator(ResultSet rs, IMetaData metadata) throws SQLException {
		RowSetFactory rowSetFactory = RowSetProvider.newFactory();
		cache = rowSetFactory.createCachedRowSet();
		cache.populate(rs);
		this.columnCount = rs.getMetaData().getColumnCount();
		this.metadata = metadata;
		setUpDecryption();
	}

	@Override
	public boolean hasNext() {
		try {
			return cache.next();
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public IRecord next() {
		IRecord currRecord = new Record();
		for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
			try {
				Object columnValue;
				columnValue = cache.getObject(columnIndex);
				IField field = new Field(columnValue);
				if (columnValue != null) {
					metadata.getFieldMeta(columnIndex - 1).setType(columnValue.getClass());
				}
				currRecord.appendField(field);
			} catch (SQLException e) {
				new SpagoBIRuntimeException("Error getting value at column " + columnIndex, e);
			}
		}
		decryptIfNeeded(currRecord);
		return currRecord;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"This operation has to be overriden by subclasses in order to be used.");
	}

	@Override
	public void close() {
		try {
			if (cache != null) {
				cache.close();
			}
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public IMetaData getMetaData() {
		return metadata;
	}

	private void setUpDecryption() {
		IMetaData dataStoreMetadata = getMetaData();

		AtomicInteger index = new AtomicInteger();

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e))
				.entrySet().stream().filter(e -> e.getValue().isDecrypt()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					decryptableField.add(value);
					decryptableFieldByIndex.put(key, value);
				});

		needDecryption = !decryptableField.isEmpty();

		if (needDecryption) {
			EncryptionConfiguration cfg = EncryptionPreferencesRegistry.getInstance()
					.getConfiguration(EncryptionPreferencesRegistry.DEFAULT_CFG_KEY);

			encryptor = EncryptorFactory.getInstance().create(cfg);
		}

	}

	private void decryptIfNeeded(IRecord currRecord) {
		if (needDecryption) {
			List<IField> fields = currRecord.getFields();

			for (int i = 0; i < fields.size(); i++) {
				if (decryptableFieldByIndex.containsKey(i)) {
					decrypt(currRecord, i);
				}
			}
		}
	}

	private void decrypt(IRecord currRecord, int i) {
		IFieldMetaData fieldMetaData = decryptableFieldByIndex.get(i);
		String fieldName = fieldMetaData.getName();
		String fieldAlias = fieldMetaData.getAlias();
		IField fieldAt = currRecord.getFieldAt(i);
		Object value = fieldAt.getValue();
		String newValue = null;

		try {
			if (Objects.nonNull(value)) {
				newValue = encryptor.decrypt(value.toString());
				fieldAt.setValue(newValue);
			}
		} catch (EncryptionOperationNotPossibleException e) {
			LOGGER.warn("Ignoring field value {} from field {} (with \"{}\" alias): see following message", value,
					fieldName, fieldAlias);
			LOGGER.warn("Cannot decrypt column: see the previous message", e);
		} catch (EncryptionInitializationException e) {
			LOGGER.error("Encryption initialization error: check decryption system properties", e);
		}
	}

}
