package it.eng.knowage.encryption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.transformer.AbstractDataStoreTransformer;

public class DecryptionDataStoreTransformer extends AbstractDataStoreTransformer {

	private static final Logger LOGGER = LogManager.getLogger(DecryptionDataStoreTransformer.class);

	private boolean needDecryption = false;
	private List<IFieldMetaData> decryptableField = new ArrayList<>();
	private Map<Integer, IFieldMetaData> decryptableFieldByIndex = new LinkedHashMap<>();
	private StandardPBEStringEncryptor encryptor;
	private final IDataSet dataSet;

	public DecryptionDataStoreTransformer(IDataSet dataSet) {
		super();
		this.dataSet = dataSet;
		setUpDecryption();
	}

	@Override
	public void transformDataSetRecords(IDataStore dataStore) {
		for (IRecord record : dataStore.getRecords()) {
			decryptIfNeeded(record);
		}

	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {

	}

	private void setUpDecryption() {
		IMetaData dataStoreMetadata = getMetaData();

		AtomicInteger index = new AtomicInteger();

		dataStoreMetadata.getFieldsMeta()
			.stream()
			.collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e))
			.entrySet()
			.stream()
			.filter(e -> e.getValue().isDecrypt())
			.forEach(e -> {
				Integer key = e.getKey();
				IFieldMetaData value = e.getValue();
				decryptableField.add(value);
				decryptableFieldByIndex.put(key, value);
			});

		needDecryption = !decryptableField.isEmpty();

		if (needDecryption) {
			EncryptionConfiguration cfg = EncryptionPreferencesRegistry.getInstance()
					.getConfiguration(EncryptionPreferencesRegistry.DEFAULT_CFG_KEY);

			String algorithm = cfg.getAlgorithm();
			String password = cfg.getEncryptionPwd();

			encryptor = new StandardPBEStringEncryptor();
			encryptor.setAlgorithm(algorithm);
			encryptor.setPassword(password);
		}

	}

	private void decryptIfNeeded(IRecord record) {
		if (needDecryption) {
			List<IField> fields = record.getFields();

			for (int i = 0; i<fields.size(); i++) {
				if (decryptableFieldByIndex.containsKey(i)) {
					IFieldMetaData fieldMetaData = decryptableFieldByIndex.get(i);
					String fieldName = fieldMetaData.getName();
					String fieldAlias = fieldMetaData.getAlias();
					IField fieldAt = record.getFieldAt(i);
					Object value = fieldAt.getValue();
					String newValue = null;

					try {
						newValue = encryptor.decrypt(value.toString());
						fieldAt.setValue(newValue);
					} catch (EncryptionOperationNotPossibleException e) {
						LOGGER.warn("Ignoring field value {} from field {} (with \"{}\" alias): see following message", value, fieldName, fieldAlias);
						LOGGER.warn("Cannot decrypt column: see the previous message", e);
					} catch (EncryptionInitializationException e) {
						LOGGER.error("Encryption initialization error: check decryption system properties", e);
					}
				}
			}
		}
	}

	private String mapFieldKey(IFieldMetaData field) {
		return field.getName();
	}

	private Map<String, IFieldMetaData> mapFieldByColumnName(IMetaData metaData) {
		return metaData.getFieldsMeta()
			.stream()
			.collect(Collectors.toMap(e -> mapFieldKey(e), e -> e));
	}

	private IMetaData getMetaData() {
		return dataSet.getDsMetadata() != null ? dataSet.getMetadata() : new MetaData();
	}

}