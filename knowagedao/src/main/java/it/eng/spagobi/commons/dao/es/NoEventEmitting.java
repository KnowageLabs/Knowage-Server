package it.eng.spagobi.commons.dao.es;

import org.hibernate.Session;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;

public class NoEventEmitting implements UserEventsEmettingCommand, RoleEventsEmittingCommand {

	@Override
	public void emitUserAttributeDeleted(Session aSession, SbiUserAttributes attribute) {}

	@Override
	public void emitUserAttributeAdded(Session aSession, SbiUserAttributes attribute) {}

	@Override
	public void emitUserDeleted(Session aSession, SbiUser user) {}

	@Override
	public void emitUserCreated(Session aSession, SbiUser user) {}

	@Override
	public void emitUserUpdated(Session aSession, SbiUser user) {}

	@Override
	public void emitUserRoleUpdated(Session aSession, SbiExtUserRoles role) {}

	@Override
	public void emitUserRoleDeleted(Session aSession, SbiExtUserRoles role) {}

	@Override
	public void emitUserRoleAdded(Session aSession, SbiExtUserRoles role) {}

	@Override
	public void emitUserAttributeUpdated(Session aSession, SbiUserAttributes attribute) {}

	@Override
	public void emitRoleDeletedEvent(Session aSession, SbiExtRoles role) {}

	@Override
	public void emitRoleAddedEvent(Session aSession, SbiExtRoles role) {}

	@Override
	public void emitDatasetCategoryRemovedEvent(Session aSession, SbiExtRoles role) {}

	@Override
	public void emitDatasetCategoryAddedEvent(Session aSession, SbiExtRoles role) {}

	@Override
	public void emitRoleUpdatedEvent(Session aSession, SbiExtRoles role) {}

	@Override
	public void emitPublicFlagSetEvent(Session aSession, SbiExtRoles role) {}

}
