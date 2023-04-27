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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.metamodel.EntityType;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.datasource.jpa.JPAPersistenceManager;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.AuditColumnType;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JPAPersistenceManagerInTableAudit {

	private JPAPersistenceManager jpaPersistenceManager;

	private RegistryConfiguration registryConfiguration;

	private UserProfile userProfile;

	private JPAPersistenceManagerInTableAudit(JPAPersistenceManager jpaPersistenceManager, RegistryConfiguration registryConfiguration,
			UserProfile userProfile) {
		super();

		Assert.assertNotNull(jpaPersistenceManager, "Missing persistence manager object");
		Assert.assertNotNull(registryConfiguration, "Missing registry configuration object");
		Assert.assertNotNull(userProfile, "Missing user profile object");

		this.jpaPersistenceManager = jpaPersistenceManager;
		this.registryConfiguration = registryConfiguration;
		this.userProfile = userProfile;
	}

	public void auditUpdate(JSONObject newRecordValues, EntityType targetEntity, Object obj) {
		Boolean isLogicalDeletion = Boolean.FALSE;
		Boolean isLogicalUnDeletion = Boolean.FALSE;

		if (registryConfiguration.getAuditColumn(AuditColumnType.IS_DELETED).isPresent()) {
			Column column = registryConfiguration.getAuditColumn(AuditColumnType.IS_DELETED).get();
			Object newDeletionStatusObj;
			try {
				newDeletionStatusObj = newRecordValues.get(column.getField());
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException(
						"An error occurred while getting column " + column.getField() + " from input values [" + newRecordValues + "]", e);
			}
			Object currentDeletionStatusObj = jpaPersistenceManager.getOldProperty(targetEntity, obj, column.getField());
			Boolean hasToBeDeleted = newDeletionStatusObj != null && Boolean.parseBoolean(newDeletionStatusObj.toString());
			Boolean isCurrentlyDeleted = currentDeletionStatusObj != null && Boolean.parseBoolean(currentDeletionStatusObj.toString());
			isLogicalDeletion = !isCurrentlyDeleted && hasToBeDeleted;
			isLogicalUnDeletion = isCurrentlyDeleted && !hasToBeDeleted;
		}

		if (isLogicalDeletion) {
			auditLogicalDeletion(targetEntity, obj);
		} else {
			auditModification(targetEntity, obj);
			if (isLogicalUnDeletion) {
				auditLogicalUndeletion(targetEntity, obj);
			}
		}
	}

	protected void auditLogicalUndeletion(EntityType targetEntity, Object obj) {
		registryConfiguration.getAuditColumn(AuditColumnType.USER_DELETE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), null));
		registryConfiguration.getAuditColumn(AuditColumnType.TIME_DELETE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), null));
		registryConfiguration.getAuditColumn(AuditColumnType.IS_DELETED)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), Boolean.FALSE));
	}

	protected void auditModification(EntityType targetEntity, Object obj) {
		String currentTimestamp = new SimpleDateFormat(JPAPersistenceManager.TIMESTAMP_SIMPLE_FORMAT).format(new Date());
		registryConfiguration.getAuditColumn(AuditColumnType.USER_UPDATE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), this.userProfile.getUserId()));
		registryConfiguration.getAuditColumn(AuditColumnType.TIME_UPDATE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), currentTimestamp));
	}

	protected void auditLogicalDeletion(EntityType targetEntity, Object obj) {
		String currentTimestamp = new SimpleDateFormat(JPAPersistenceManager.TIMESTAMP_SIMPLE_FORMAT).format(new Date());
		registryConfiguration.getAuditColumn(AuditColumnType.USER_DELETE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), this.userProfile.getUserId()));
		registryConfiguration.getAuditColumn(AuditColumnType.TIME_DELETE)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), currentTimestamp));
		registryConfiguration.getAuditColumn(AuditColumnType.IS_DELETED)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, obj, column.getField(), Boolean.TRUE));
	}

	public void auditInsertion(RegistryConfiguration registryConf, EntityType targetEntity, Object newObj) {
		String currentTimestamp = new SimpleDateFormat(JPAPersistenceManager.TIMESTAMP_SIMPLE_FORMAT).format(new Date());
		registryConf.getAuditColumn(AuditColumnType.USER_INSERT)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, newObj, column.getField(), this.userProfile.getUserId()));
		registryConf.getAuditColumn(AuditColumnType.TIME_INSERT)
				.ifPresent(column -> jpaPersistenceManager.setProperty(targetEntity, newObj, column.getField(), currentTimestamp));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private JPAPersistenceManager jpaPersistenceManager;

		private RegistryConfiguration registryConfiguration;

		private UserProfile userProfile;

		public Builder withJPAPersistenceManager(JPAPersistenceManager jpaPersistenceManager) {
			this.jpaPersistenceManager = jpaPersistenceManager;
			return this;
		}

		public Builder withRegistryConfiguration(RegistryConfiguration registryConfiguration) {
			this.registryConfiguration = registryConfiguration;
			return this;
		}

		public Builder withUserProfile(UserProfile userProfile) {
			this.userProfile = userProfile;
			return this;
		}

		public JPAPersistenceManagerInTableAudit build() {
			return new JPAPersistenceManagerInTableAudit(jpaPersistenceManager, registryConfiguration, userProfile);
		}
	}
}
