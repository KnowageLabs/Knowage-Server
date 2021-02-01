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
package it.eng.qbe.datasource.jpa.audit;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.user.UserProfileManager;

public class JPAPersistenceManagerAuditLogger {

	public static transient Logger auditLogger = Logger.getLogger(JPAPersistenceManagerAuditLogger.class);

	public static void log(Operation operation, String modelName, String entityName, JSONObject oldRecord, JSONObject newRecord, Integer changesNumber) {
		String oldRecordAsString = oldRecord != null ? oldRecord.toString() : "";
		String newRecordAsString = newRecord != null ? newRecord.toString() : "";
		UserProfile userProfile = UserProfileManager.getProfile();
		String userId = (String) userProfile.getUserId();
		logInternal(userId, operation, modelName, entityName, oldRecordAsString, newRecordAsString, changesNumber);
	}

	private static void logInternal(String userId, Operation operation, String modelName, String entityName, String oldRecordAsString, String newRecordAsString,
			Integer changesNumber) {
		try {
			MDC.put("userId", StringEscapeUtils.escapeSql(userId));
			MDC.put("operation", StringEscapeUtils.escapeSql(operation.name()));
			MDC.put("variations", changesNumber != null && changesNumber > 0 ? changesNumber : "NULL");
			MDC.put("modelName", StringEscapeUtils.escapeSql(modelName));
			MDC.put("entityName", StringEscapeUtils.escapeSql(entityName));
			MDC.put("oldRecord", StringEscapeUtils.escapeSql(oldRecordAsString));
			MDC.put("newRecord", StringEscapeUtils.escapeSql(newRecordAsString));
			auditLogger.info("User [" + userId + "] is performing operation [" + operation.name() + "] on entity [" + entityName + "] from model [" + modelName
					+ "] for record: old one is [" + oldRecordAsString + "], new one is [" + newRecordAsString + "], number of changes is " + changesNumber);
		} finally {
			MDC.clear();
		}
	}

}
