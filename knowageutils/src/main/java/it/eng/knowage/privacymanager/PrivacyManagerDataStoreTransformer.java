package it.eng.knowage.privacymanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.pm.dto.DataSetScope;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.transformer.AbstractDataStoreTransformer;
import it.eng.spagobi.user.UserProfileManager;

public class PrivacyManagerDataStoreTransformer extends AbstractDataStoreTransformer {

	private static final Logger LOGGER = LogManager.getLogger(PrivacyManagerDataStoreTransformer.class);

	private final IDataSet dataSet;
	private boolean needPM = false;
	private final List<IFieldMetaData> sensibleField = new ArrayList<>();
	private final Map<Integer, IFieldMetaData> sensibleFieldByIndex = new LinkedHashMap<>();

	private final List<IFieldMetaData> subjectField = new ArrayList<>();
	private final Map<Integer, IFieldMetaData> subjectFieldByIndex = new LinkedHashMap<>();

	public PrivacyManagerDataStoreTransformer(IDataSet dataSet) {
		this.dataSet = dataSet;
		try {
			setUpPrivacy();
		} catch (Exception e) {
			LOGGER.error("Privacy initialization error: check setUpPrivacy method", e);
		}
	}

	@Override
	public void transformDataSetRecords(IDataStore dataStore) {
		if (needPM) {
			FullEventBuilder eventBuilder = new FullEventBuilder(true);
			UserProfile up = UserProfileManager.getProfile();

			eventBuilder.appendSession("knowage", up.getSourceIpAddress(), up.getSessionId(), up.getSessionStart(), up.getUserId().toString());
			eventBuilder.appendUserAgent(up.getOs(), up.getSourceIpAddress(), up.getSourceSocketEnabled(), up.getUserAgent());
			// metadata --> from dataset (map)
			Set<String> keys = this.dataSet.getParamsMap().keySet();
			for (String key : keys) {
				eventBuilder.appendMetaData(key, this.dataSet.getParamsMap().get(key).toString());
			}

			for (IRecord record : dataStore.getRecords()) {
				List<IField> fields = record.getFields();

				for (int i = 0; i < fields.size(); i++) {
					if (subjectFieldByIndex.containsKey(i)) {
						IFieldMetaData fieldMetaData = subjectFieldByIndex.get(i);
						// String fieldName = fieldMetaData.getName();
						IField fieldAt = record.getFieldAt(i);
						Object value = fieldAt.getValue();
						// TODO in teoria campo unico da decidere con il FE
						eventBuilder.appendSubject(value.toString());
					}
				}
				for (int i = 0; i < fields.size(); i++) {
					if (sensibleFieldByIndex.containsKey(i)) {
						IFieldMetaData fieldMetaData = sensibleFieldByIndex.get(i);
						String fieldName = fieldMetaData.getName();
						IField fieldAt = record.getFieldAt(i);
						Object value = fieldAt.getValue();
						// TODO definizione del tipo dato
						eventBuilder.appendData(fieldName, value.toString(), DataSetScope.OTHER);
					}
				}
			}
			// l'ultimo record non e' ancora stato caricato sul dto
			eventBuilder.forceLastSubject();

			PrivacyManagerClient.getInstance().sendMessage(eventBuilder.getDTO());
		}
	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {
		// TODO Auto-generated method stub DAOFactory

	}

	private void setUpPrivacy() {
		IMetaData dataStoreMetadata = getMetaData();

		AtomicInteger index = new AtomicInteger();

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e)).entrySet().stream()
				.filter(e -> e.getValue().isPersonal()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					sensibleField.add(value);
					sensibleFieldByIndex.put(key, value);
				});

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e)).entrySet().stream()
				.filter(e -> e.getValue().isSubjectId()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					subjectField.add(value);
					subjectFieldByIndex.put(key, value);
				});

		needPM = !sensibleField.isEmpty();

	}

	private String mapFieldKey(IFieldMetaData field) {
		return field.getName();
	}

	private Map<String, IFieldMetaData> mapFieldByColumnName(IMetaData metaData) {
		return metaData.getFieldsMeta().stream().collect(Collectors.toMap(e -> mapFieldKey(e), e -> e));
	}

	private IMetaData getMetaData() {
		return dataSet.getDsMetadata() != null ? dataSet.getMetadata() : new MetaData();
	}

}
