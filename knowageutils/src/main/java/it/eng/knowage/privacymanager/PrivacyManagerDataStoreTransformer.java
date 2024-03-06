package it.eng.knowage.privacymanager;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.pm.dto.DataSetScope;
import it.eng.knowage.pm.dto.PrivacyDTO;
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
	private final Map<Integer, Integer> subjectFieldOrder = new LinkedHashMap<>();

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
			LOGGER.debug("Transforming data store {}", dataStore);

			FullEventBuilder2 eventBuilder = new FullEventBuilder2(true);
			UserProfile up = UserProfileManager.getProfile();

			String sourceIpAddress = "";
			String sessionId = "";
			Long sessionStart = 0L;
			String string = "";
			String os = "";
			Boolean sourceSocketEnabled = false;
			String userAgent = "";

			if (nonNull(up)) {
				sourceIpAddress = up.getSourceIpAddress();
				sessionId = up.getSessionId();
				sessionStart = up.getSessionStart();
				string = nonNull(up.getUserId()) ? up.getUserId().toString() : "";
				os = up.getOs();
				sourceSocketEnabled = up.getSourceSocketEnabled();
				userAgent = up.getUserAgent();
			}

			LOGGER.debug("Session: {}, {}, {}, {}", sourceIpAddress, sessionId, sessionStart, string);
			LOGGER.debug("User agent: {}, {}, {}, {}", os, sourceIpAddress, sourceSocketEnabled, userAgent);

			eventBuilder.appendSession("knowage", sourceIpAddress, sessionId, sessionStart, string);
			eventBuilder.appendUserAgent(os, sourceIpAddress, sourceSocketEnabled, userAgent);
			// metadata --> from dataset (map)
			Map paramsMap = Optional.ofNullable(dataSet.getParamsMap()).orElse(Collections.emptyMap());
			LOGGER.debug("Parameters map is {}", paramsMap);
			Set<String> keys = paramsMap.keySet();
			for (String key : keys) {
				LOGGER.debug("Key is {}", key);
				String paramValue = Optional.ofNullable(paramsMap.get(key)).map(Object::toString).orElse("");
				LOGGER.debug("Value is {}", paramValue);
				eventBuilder.appendMetaData(key, paramValue);
			}

			for (IRecord currRecord : dataStore.getRecords()) {
				String[] subjData = new String[4];
				List<IField> fields = currRecord.getFields();

				for (int i = 0; i < fields.size(); i++) {
					LOGGER.debug("Checking subject field {} - fieldFound {}", i, subjectFieldByIndex.containsKey(i));
					if (subjectFieldByIndex.containsKey(i)) {
						IField fieldAt = currRecord.getFieldAt(i);
						Object value = fieldAt.getValue();

						String val = null;
						if (value != null) {
							val = value.toString();
						}
						subjData[subjectFieldOrder.get(i)] = val;
					}
				}
				for (int i = 0; i < fields.size(); i++) {
					LOGGER.debug("Checking sensiblefield {} - fieldFound {}", i, sensibleFieldByIndex.containsKey(i));
					if (sensibleFieldByIndex.containsKey(i)) {
						IFieldMetaData fieldMetaData = sensibleFieldByIndex.get(i);
						String fieldName = fieldMetaData.getName();
						IField fieldAt = currRecord.getFieldAt(i);
						Object value = fieldAt.getValue();

						String val = null;
						if (value != null) {
							val = value.toString();
						}
						LOGGER.debug("appending fieldName{}, value{}", fieldName, val);
						eventBuilder.appendData(fieldName, val, DataSetScope.OTHER);
					}
				}
				LOGGER.debug("appending subject {}, {}, {}, {}", subjData[0], subjData[1], subjData[2], subjData[3]);
				eventBuilder.appendSubject(subjData[0], subjData[1], subjData[2], subjData[3]);
			}
			PrivacyDTO dto = eventBuilder.getDTO();
			LOGGER.debug("Calculated DTO: {}", dto);
			PrivacyManagerClient.getInstance().sendMessage(dto);
		}
	}

	@Override
	public void transformDataSetMetaData(IDataStore dataStore) {
		// Not needed
	}

	private void setUpPrivacy() {
		IMetaData dataStoreMetadata = getMetaData();

		AtomicInteger index = new AtomicInteger();

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e))
				.entrySet().stream().filter(e -> e.getValue().isSubjectId()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					subjectField.add(value);
					subjectFieldByIndex.put(key, value);
					LOGGER.debug("Scanned subject field {}, {}", key, value.getName());
					String decoded = EventBuilderUtils.decodeSubjectField(value.getName());
					switch (decoded) {
					case EventBuilderUtils.TAXCODE:
						subjectFieldOrder.put(key, 0);
						break;
					case EventBuilderUtils.NAME:
						subjectFieldOrder.put(key, 1);
						break;
					case EventBuilderUtils.LAST_NAME:
						subjectFieldOrder.put(key, 2);
						break;
					case EventBuilderUtils.BIRTHDATE:
						subjectFieldOrder.put(key, 3);
						break;
					default:
						LOGGER.error("Cannot map subject field {}. Check PrivacyManagerClient.properties",
								value.getName());
					}
				});

		dataStoreMetadata.getFieldsMeta().stream().collect(Collectors.toMap(e -> index.getAndIncrement(), e -> e))
				.entrySet().stream().filter(e -> e.getValue().isPersonal()).forEach(e -> {
					Integer key = e.getKey();
					IFieldMetaData value = e.getValue();
					LOGGER.debug("Scanned sensible ect field {}, {}", key, value.getName());
					sensibleField.add(value);
					sensibleFieldByIndex.put(key, value);
				});

		needPM = !sensibleField.isEmpty();
		LOGGER.debug("subjectFiledByIndex {}", subjectFieldByIndex);
		LOGGER.debug("subjectFieldOrder {}", subjectFieldOrder);
		LOGGER.debug("sensibleFiledByIndex {}", sensibleFieldByIndex);

	}

	private IMetaData getMetaData() {
		return dataSet.getDsMetadata() != null ? dataSet.getMetadata() : new MetaData();
	}

}
