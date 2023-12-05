package it.eng.knowage.privacymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.eng.knowage.pm.dto.DataSetScope;
import it.eng.knowage.pm.dto.PrivacyEventType;

public class FullEventBuilder extends LoginEventBuilder {

	private StringBuilder subject = new StringBuilder();

	public FullEventBuilder(boolean isDataAccess) {
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
		if (rm == null) {
			rm = new HashMap<>();
		}
		rm.put(paramName, paramValue);

		dto.setRequestMetadatas(rm);
	}

	public void appendSubject(String taxCode, String name, String lastName, String birthDate) {
		if (dto.getResponses() != null) {
			dto.getResponses().add(subject.toString());
			subject = new StringBuilder();
		} else {
			dto.setResponses(new ArrayList<>());
		}
		subject.append("000;").append(taxCode).append(";").append(name).append(";").append(lastName).append(";").append(birthDate).append("\n");
	}

	public void appendSubject(String fullSubject) {
		if (dto.getResponses() != null) {
			dto.getResponses().add(subject.toString());
			subject = new StringBuilder();
		} else {
			dto.setResponses(new ArrayList<>());
		}
		subject.append("000;").append(fullSubject).append("\n");
	}

	public void appendData(String field, String val, DataSetScope dataSetScope) {
		subject.append("010;").append(field).append(";").append(val).append(";").append(dataSetScope).append("\n");
	}

	public void forceLastSubject() {
		dto.getResponses().add(subject.toString());
	}
}
