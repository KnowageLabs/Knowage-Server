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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import it.eng.knowage.encryption.DataEncryptionGlobalCfg;
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

	private final Connection conn;
	private final Statement stmt;
	private final ResultSet rs;
	private final IMetaData metadata;
	private final int columnCount;
	private boolean needDecryption = false;
	private final List<IFieldMetaData> decryptableField = new ArrayList<>();
	private final Map<Integer, IFieldMetaData> decryptableFieldByIndex = new LinkedHashMap<>();
	private PBEStringEncryptor encryptor;

	/**
	 * IMPORTANT!!! An {@code Iterator} has methods {@code hasNext()} and {@code next()} while a {@code ResultSet} object has only {@code next()}, that behaves
	 * like both {@code Iterator} {@code hasNext()} and {@code next()} at the same time, since it returns true if there are other elements while it is moving
	 * forward its internal cursor. But {@code Iterator.hasNext()} is not supposed to move forward, therefore it cannot invoke {@code ResultSet.next()}
	 * method!!! In order to harmonize those API, {@code ResultSetIterator} loads first record into {@code nextRow} variable during initialization (within the
	 * constructor) using the {@code loadNextRow} method; when {@code Iterator.next()} method is invoked, we get values from {@code nextRow} variable and then
	 * we move forward with {@code loadNextRow} method, overriding the values into {@code nextRow} variable or setting it to null in case there no more
	 * elements. {@code Iterator.hasNext()} method simply checks that {@code nextRow} is not null.
	 */
	private Object[] nextRow;

	public ResultSetIterator(Connection conn, Statement stmt, ResultSet rs, IMetaData metadata) throws SQLException {
		this.conn = conn;
		this.stmt = stmt;
		this.rs = rs;
		this.columnCount = rs.getMetaData().getColumnCount();
		this.metadata = metadata;
		setUpDecryption();
		loadNextRow();
	}

	private void loadNextRow() throws SQLException {
		if (rs.next()) {
			int columnsNumber = rs.getMetaData().getColumnCount();
			Object[] row = new Object[columnsNumber];
			for (int columnIndex = 1; columnIndex <= columnsNumber; columnIndex++) {
				row[columnIndex - 1] = rs.getObject(columnIndex);
			}
			this.nextRow = row;
		} else {
			this.nextRow = null;
		}
	}

	@Override
	public boolean hasNext() {
		return nextRow != null;
	}

	@Override
	public IRecord next() {
		if (!hasNext()) {
			throw new SpagoBIRuntimeException("ResultSet is empty or it was already scrolled completely");
		}
		int columnIndex = 0;
		try {
			IRecord currRecord = new Record();
			for (columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				Object columnValue = nextRow[columnIndex - 1];
				IField field = new Field(columnValue);
				if (columnValue != null) {
					metadata.getFieldMeta(columnIndex - 1).setType(columnValue.getClass());
				}
				currRecord.appendField(field);
			}
			decryptIfNeeded(currRecord);
			loadNextRow();
			return currRecord;
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This operation has to be overriden by subclasses in order to be used.");
	}

	@Override
	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
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

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e)).entrySet().stream()
				.filter(e -> e.getValue().isDecrypt()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					decryptableField.add(value);
					decryptableFieldByIndex.put(key, value);
				});

		needDecryption = !decryptableField.isEmpty();

		if (needDecryption) {
			DataEncryptionGlobalCfg decfee = DataEncryptionGlobalCfg.getInstance();
			String algorithm = decfee.getKeyTemplateForAlgorithm(EncryptionPreferencesRegistry.DEFAULT_CFG_KEY);
			String password = decfee.getKeyTemplateForPassword(EncryptionPreferencesRegistry.DEFAULT_CFG_KEY);
			encryptor = EncryptorFactory.getInstance().create(algorithm, password);
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
			LOGGER.warn("Ignoring field value {} from field {} (with \"{}\" alias): see following message", value, fieldName, fieldAlias);
			LOGGER.warn("Cannot decrypt column: see the previous message", e);
		} catch (EncryptionInitializationException e) {
			LOGGER.error("Encryption initialization error: check decryption system properties", e);
		}
	}

}
