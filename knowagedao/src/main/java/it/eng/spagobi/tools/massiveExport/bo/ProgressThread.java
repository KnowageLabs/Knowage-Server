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
package it.eng.spagobi.tools.massiveExport.bo;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProgressThread {

	private Integer progressThreadId;
	private String userId;
	private Integer partial;
	private Integer total;
	private String functionCd;
	private String status;
	private String randomKey;
	private String type;
	private String executionRole;

	public static final String TYPE_MASSIVE_SCHEDULE = "MASSIVE_SCHEDULE";
	public static final String TYPE_MASSIVE_EXPORT = "MASSIVE_EXPORT";
	public static final String TYPE_DOSSIER_EXECUTION = "DOSSIER_EXECUTION";

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getProgressThreadId() {
		return progressThreadId;
	}

	public void setProgressThreadId(Integer progressThreadId) {
		this.progressThreadId = progressThreadId;
	}

	public Integer getPartial() {
		return partial;
	}

	public void setPartial(Integer partial) {
		this.partial = partial;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getFunctionCd() {
		return functionCd;
	}

	public void setFunctionCd(String functionCd) {
		this.functionCd = functionCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ProgressThread(String userId, Integer total, String functionCd, String status, String randomKey, String type,
			String executionRole) {
		this.userId = userId;
		this.total = total;
		this.functionCd = functionCd;
		this.status = status;
		this.randomKey = randomKey;
		this.type = type;
		this.executionRole = executionRole;
	}

	public ProgressThread() {

	}

	public String getRandomKey() {
		return randomKey;
	}

	public void setRandomKey(String randomKey) {
		this.randomKey = randomKey;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExecutionRole() {
		return executionRole;
	}

	public void setExecutionRole(String executionRole) {
		this.executionRole = executionRole;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
