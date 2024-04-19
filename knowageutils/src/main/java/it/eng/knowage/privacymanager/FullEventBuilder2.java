package it.eng.knowage.privacymanager;

import java.util.ArrayList;
import java.util.Map;

import it.eng.knowage.pm.dto.DataSetScope;
import it.eng.knowage.pm.dto.PrivacyEventType;

public class FullEventBuilder2 extends LoginEventBuilder {

	private StringBuilder subject = new StringBuilder();
	private StringBuilder sData = new StringBuilder();

	public FullEventBuilder2(boolean isDataAccess) {
		if (isDataAccess) {
			dto.setEventType(PrivacyEventType.DATA_ACCESS);
			dto.setDescription("Data Access");
		} else {
			dto.setEventType(PrivacyEventType.CHANGE_ROLE);
			dto.setDescription("Change Role");
		}
	}

	public void appendMetaData(String paramName, String paramValue) {
		Map<String, String> rm = dto.getRequestMetadatas();
		rm.put(paramName, paramValue);
	}

	public void appendSubject(String taxCode, String name, String lastName, String birthDate) {
		if (dto.getResponses() == null) {
			dto.setResponses(new ArrayList<>());
		}

		subject.append("000;").append(taxCode).append(";").append(name).append(";").append(lastName).append(";")
				.append(birthDate).append("\n");
		subject.append(sData);
		dto.getResponses().add(subject.toString());
		subject = new StringBuilder();
		sData = new StringBuilder();
	}

	public void appendData(String field, String val, DataSetScope dataSetScope) {
		sData.append("010;").append(field).append(";").append(val).append(";").append(dataSetScope).append("\n");
	}

}
