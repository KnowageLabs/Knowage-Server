package it.eng.spagobi.commons.dao.es;

import org.hibernate.Session;

import it.eng.knowage.pm.dto.DataSetScope;
import it.eng.knowage.pm.dto.PrivacyDTO;
import it.eng.knowage.privacymanager.FullEventBuilder;
import it.eng.knowage.privacymanager.PrivacyManagerClient;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.user.UserProfileManager;

public class PMEventToDatabaseEmittingCommand implements UserEventsEmettingCommand, RoleEventsEmittingCommand {

	private FullEventBuilder getBasicBuilder() {
		FullEventBuilder eventBuilder = new FullEventBuilder(false);
		UserProfile up = UserProfileManager.getProfile();

		eventBuilder.appendSession("knowage", up.getSourceIpAddress(), up.getSessionId(), up.getSessionStart(), up.getUserId().toString());
		eventBuilder.appendUserAgent(up.getOs(), up.getSourceIpAddress(), up.getSourceSocketEnabled(), up.getUserAgent());

		return eventBuilder;
	}

	private void sendMessage(PrivacyDTO dto) {
		PrivacyManagerClient.getInstance().sendMessage(dto);
	}

	@Override
	public void emitRoleDeletedEvent(Session aSession, SbiExtRoles role) {
	}

	@Override
	public void emitRoleAddedEvent(Session aSession, SbiExtRoles role) {
	}

	@Override
	public void emitDatasetCategoryRemovedEvent(Session aSession, SbiExtRoles role) {
	}

	@Override
	public void emitDatasetCategoryAddedEvent(Session aSession, SbiExtRoles role) {
	}

	@Override
	public void emitRoleUpdatedEvent(Session aSession, SbiExtRoles role) {

	}

	@Override
	public void emitPublicFlagSetEvent(Session aSession, SbiExtRoles role) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		eventBuilder.appendSubject("PUBLIC", "PUBLIC USER", "PUBLIC USER", "" + System.currentTimeMillis());
		eventBuilder.appendData("PublicFlag", role.getName(), DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserAttributeDeleted(Session aSession, SbiUserAttributes attribute) {

		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = attribute.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("UserAttributeDeleted", attribute.getSbiAttribute().getAttributeName() + "=" + attribute.getAttributeValue(),
				DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserAttributeAdded(Session aSession, SbiUserAttributes attribute) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = attribute.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("UserAttributeAdded", attribute.getSbiAttribute().getAttributeName() + "=" + attribute.getAttributeValue(), DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());

	}

	@Override
	public void emitUserDeleted(Session aSession, SbiUser user) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("User Deleted", "User Deleted", DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserCreated(Session aSession, SbiUser user) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("User Created", "User Created", DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserUpdated(Session aSession, SbiUser user) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("User Updated", "User Updated", DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserRoleUpdated(Session aSession, SbiExtUserRoles role) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = role.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("RoleUpdated", "" + role.getId(), DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserRoleDeleted(Session aSession, SbiExtUserRoles role) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = role.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("RoleDeleted", "" + role.getId(), DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserRoleAdded(Session aSession, SbiExtUserRoles role) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = role.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("RoleAdded", "" + role.getId(), DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	@Override
	public void emitUserAttributeUpdated(Session aSession, SbiUserAttributes attribute) {
		FullEventBuilder eventBuilder = getBasicBuilder();
		SbiUser user = attribute.getSbiUser();
		eventBuilder.appendSubject(user.getUserId(), user.getFullName(), "", "" + System.currentTimeMillis());
		eventBuilder.appendData("UserAttributeUpdated", attribute.getSbiAttribute().getAttributeName() + "=" + attribute.getAttributeValue(),
				DataSetScope.OTHER);
		eventBuilder.forceLastSubject();
		sendMessage(eventBuilder.getDTO());
	}

	// Role for ALL (sospeso)
	// UserRole gestione ruolo-utente
	// UserAttribute gestione attribute utente
	//

}
