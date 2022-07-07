package it.eng.spagobi.commons.dao.es;

import org.hibernate.Session;

import it.eng.spagobi.commons.metadata.SbiExtRoles;

public interface RoleEventsEmittingCommand {

	void emitRoleDeletedEvent(Session aSession, SbiExtRoles role);

	void emitRoleAddedEvent(Session aSession, SbiExtRoles role);

	void emitDatasetCategoryRemovedEvent(Session aSession, SbiExtRoles role);

	void emitDatasetCategoryAddedEvent(Session aSession, SbiExtRoles role);

	void emitRoleUpdatedEvent(Session aSession, SbiExtRoles role);

	void emitPublicFlagSetEvent(Session aSession, SbiExtRoles role);

}
