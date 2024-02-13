/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.pm.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrivacyDTO implements Serializable {

	private static final long serialVersionUID = 2313489224466722385L;

	private PrivacyEventType eventType;

	private UserAgentDTO userAgent;

	private String description;

	private Long timestamp;

	private String module;

	private String request;

	private final Map<String, String> requestMetadatas = new LinkedHashMap<>();

	private List<String> responses;

	private Outcome outcome;

	private SessionDTO session;

	public PrivacyEventType getEventType() {
		return eventType;
	}

	public void setEventType(PrivacyEventType eventType) {
		this.eventType = eventType;
	}

	public UserAgentDTO getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(UserAgentDTO userAgent) {
		this.userAgent = userAgent;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public Map<String, String> getRequestMetadatas() {
		return requestMetadatas;
	}

	public List<String> getResponses() {
		return responses;
	}

	public void setResponses(List<String> responses) {
		this.responses = responses;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SessionDTO getSession() {
		return session;
	}

	public void setSession(SessionDTO session) {
		this.session = session;
	}

}
