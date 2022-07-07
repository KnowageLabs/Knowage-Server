package it.eng.spagobi.commons.dao.es;

import org.hibernate.Session;

import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;

public interface UserEventsEmettingCommand {

	void emitUserAttributeDeleted(Session aSession, SbiUserAttributes attribute);

	void emitUserAttributeAdded(Session aSession, SbiUserAttributes attribute);

	void emitUserDeleted(Session aSession, SbiUser user);

	void emitUserCreated(Session aSession, SbiUser user);

	void emitUserUpdated(Session aSession, SbiUser user);

	void emitUserRoleUpdated(Session aSession, SbiExtUserRoles role);

	void emitUserRoleDeleted(Session aSession, SbiExtUserRoles role);

	void emitUserRoleAdded(Session aSession, SbiExtUserRoles role);

	void emitUserAttributeUpdated(Session aSession, SbiUserAttributes attribute);

}