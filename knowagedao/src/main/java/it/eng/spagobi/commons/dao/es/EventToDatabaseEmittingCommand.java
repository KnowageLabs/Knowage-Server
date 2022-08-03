package it.eng.spagobi.commons.dao.es;

import static java.util.Objects.isNull;

import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiEs;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class EventToDatabaseEmittingCommand implements UserEventsEmettingCommand, RoleEventsEmittingCommand {

	@Override
	public void emitUserAttributeDeleted(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getSbiAttribute().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", Optional.ofNullable(attribute.getAttributeValue()).orElse(""))
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeDeleted")
				.withData(data)
				.build();

			aSession.save(event);

	}

	@Override
	public void emitUserAttributeAdded(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getId().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", Optional.ofNullable(attribute.getAttributeValue()).orElse(""))
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeAdded")
				.withData(data)
				.build();

			aSession.save(event);

	}

	@Override
	public void emitUserDeleted(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> Json.createObjectBuilder()
					.add("extRoleId", e.getExtRoleId())
					.add("code", e.getCode())
					.add("name", e.getName())
					.build())
			.forEach(arrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", arrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserDeleted")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserCreated(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> Json.createObjectBuilder()
					.add("extRoleId", e.getExtRoleId())
					.add("code", e.getCode())
					.add("name", e.getName())
					.build())
			.forEach(arrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", arrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserCreated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserUpdated(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder rolesArrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> {
				JsonObjectBuilder builder = Json.createObjectBuilder();
				builder.add("extRoleId", e.getExtRoleId());

				if (isNull(e.getCode())) {
					builder.addNull("code");
				} else {
					builder.add("code", e.getCode());
				}
				builder.add("name", e.getName());

				JsonObject built = builder.build();

				return built;
			})
			.forEach(rolesArrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", rolesArrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserUpdated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserRoleUpdated(Session aSession, SbiExtUserRoles role) {

		SbiExtUserRolesId id = role.getId();
		Integer extRoleId = id.getExtRoleId();

		IRoleDAO roleDAO = DAOFactory.getRoleDAO();

		SbiExtRoles extRole = null;

		try {
			extRole = roleDAO.loadSbiExtRoleById(extRoleId);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot load ext role with id " + extRoleId);
		}

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", extRoleId)
				.add("code", Optional.ofNullable(extRole.getCode()).orElse(""))
				.add("name", extRole.getName())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleUpdated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserRoleDeleted(Session aSession, SbiExtUserRoles role) {

		SbiExtUserRolesId id = role.getId();
		Integer extRoleId = id.getExtRoleId();

		IRoleDAO roleDAO = DAOFactory.getRoleDAO();

		SbiExtRoles extRole = null;

		try {
			extRole = roleDAO.loadSbiExtRoleById(extRoleId);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot load ext role with id " + extRoleId);
		}

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", extRoleId)
				.add("code", Optional.ofNullable(extRole.getCode()).orElse(""))
				.add("name", extRole.getName())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleDeleted")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserRoleAdded(Session aSession, SbiExtUserRoles role) {

		SbiExtUserRolesId id = role.getId();
		Integer extRoleId = id.getExtRoleId();

		IRoleDAO roleDAO = DAOFactory.getRoleDAO();

		SbiExtRoles extRole = null;

		try {
			extRole = roleDAO.loadSbiExtRoleById(extRoleId);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Cannot load ext role with id " + extRoleId);
		}

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", extRoleId)
				.add("code", Optional.ofNullable(extRole.getCode()).orElse(""))
				.add("name", extRole.getName())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleAdded")
			.withData(data)
			.build();

		aSession.save(event);
	}

	@Override
	public void emitUserAttributeUpdated(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getSbiAttribute().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", Optional.ofNullable(attribute.getAttributeValue()).orElse(""))
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeUpdated")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitRoleDeletedEvent(Session aSession, SbiExtRoles role) {

		JsonArray authorizations = createAuthorizationsAsJsonArray(role);
		JsonArray datasetCategories = createDatasetCategoriesAsJsonArray(role);
		JsonArray metaModelCategories = createMetaModelCategoriesAsJsonArray(role);

		JsonObject data = createCommonDataForEvent()
				.add("id", role.getExtRoleId())
				.add("code", Optional.ofNullable(role.getCode()).orElse(""))
				.add("name", role.getName())
				.add("description", Optional.ofNullable(role.getDescr()).orElse(""))
				.add("isPublic", role.getIsPublic())
				.add("roleTypeCode", role.getRoleTypeCode())
				.add("authorizations", authorizations)
				.add("datasetCategories", datasetCategories)
				.add("metaModelCategories", metaModelCategories)
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("RoleAdded")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitRoleAddedEvent(Session aSession, SbiExtRoles role) {

		JsonArray authorizations = createAuthorizationsAsJsonArray(role);
		JsonArray datasetCategories = createDatasetCategoriesAsJsonArray(role);
		JsonArray metaModelCategories = createMetaModelCategoriesAsJsonArray(role);

		JsonObject data = createCommonDataForEvent()
				.add("id", role.getExtRoleId())
				.add("code", Optional.ofNullable(role.getCode()).orElse(""))
				.add("name", role.getName())
				.add("description", Optional.ofNullable(role.getDescr()).orElse(""))
				.add("isPublic", role.getIsPublic())
				.add("roleTypeCode", role.getRoleTypeCode())
				.add("authorizations", authorizations)
				.add("datasetCategories", datasetCategories)
				.add("metaModelCategories", metaModelCategories)
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("RoleAdded")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitDatasetCategoryRemovedEvent(Session aSession, SbiExtRoles role) {

		JsonArray datasetCategories = createDatasetCategoriesAsJsonArray(role);

		JsonObjectBuilder builder = createCommonDataForEvent()
				.add("id", role.getExtRoleId());
		if (role.getCode() == null) {
			builder.addNull("code");
		} else {
			builder.add("code", role.getCode());
		}
		JsonObject data = builder.add("name", role.getName())
				.add("datasetCategories", datasetCategories)
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("DatasetCategoryRemoved")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitDatasetCategoryAddedEvent(Session aSession, SbiExtRoles role) {

		JsonArray datasetCategories = createDatasetCategoriesAsJsonArray(role);

		JsonObjectBuilder builder = createCommonDataForEvent()
				.add("id", role.getExtRoleId());
		if (role.getCode() == null) {
			builder.addNull("code");
		} else {
			builder.add("code", role.getCode());
		}
		JsonObject data = builder.add("name", role.getName())
				.add("datasetCategories", datasetCategories)
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("DatasetCategoryAdded")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitRoleUpdatedEvent(Session aSession, SbiExtRoles role) {

		JsonArray authorizations = createAuthorizationsAsJsonArray(role);
		JsonArray datasetCategories = createDatasetCategoriesAsJsonArray(role);
		JsonArray metaModelCategories = createMetaModelCategoriesAsJsonArray(role);

		JsonObject data = createCommonDataForEvent()
				.add("id", role.getExtRoleId())
				.add("code", Optional.ofNullable(role.getCode()).orElse(""))
				.add("name", role.getName())
				.add("description", Optional.ofNullable(role.getDescr()).orElse(""))
				.add("isPublic", role.getIsPublic())
				.add("roleTypeCode", role.getRoleTypeCode())
				.add("authorizations", authorizations)
				.add("datasetCategories", datasetCategories)
				.add("metaModelCategories", metaModelCategories)
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("RoleUpdated")
				.withData(data)
				.build();

		aSession.save(event);

	}

	@Override
	public void emitPublicFlagSetEvent(Session aSession, SbiExtRoles role) {

		JsonObjectBuilder builder = createCommonDataForEvent()
				.add("id", role.getExtRoleId());
		if (role.getCode() == null) {
			builder.addNull("code");
		} else {
			builder.add("code", role.getCode());
		}
		JsonObject data = builder.add("name", role.getName())
				.add("isPublic", role.getIsPublic())
				.build();

		SbiEs event = createRoleEvent(role)
				.withEvent("PublicFlagSet")
				.withData(data)
				.build();

		aSession.save(event);

	}

	public SbiEs.Builder createUserEvent(SbiUser user) {
		return createUserEvent(user.getId());
	}

	public SbiEs.Builder createUserEvent(int userId) {
		return SbiEs.Builder.newBuilder()
			.withType("User")
			.withId(userId);
	}

	public JsonArray createAuthorizationsAsJsonArray(SbiExtRoles role) {
		JsonArrayBuilder authorizationsArrayBuilder = Json.createArrayBuilder();

		((Set<SbiAuthorizationsRoles>) role.getSbiAuthorizationsRoleses())
			.stream()
			.map(e -> {

				JsonObjectBuilder builder = Json.createObjectBuilder()
						.add("authorizationId", e.getId().getAuthorizationId())
						.add("roleId", e.getId().getRoleId());

				if (isNull(e.getSbiAuthorizations())) {
					builder.addNull("name");
				} else {
					builder.add("name", e.getSbiAuthorizations().getName());
				}

				JsonObject ret = builder.build();

				return ret;
			})
			.forEach(authorizationsArrayBuilder::add);

		return authorizationsArrayBuilder.build();
	}

	public JsonArray createDatasetCategoriesAsJsonArray(SbiExtRoles role) {
		JsonArrayBuilder authorizationsArrayBuilder = Json.createArrayBuilder();

		if (!isNull(role.getSbiDataSetCategories())) {
			((Set<SbiDomains>) role.getSbiDataSetCategories())
				.stream()
				.map(this::fromSbiDomainsToJsonObject)
				.forEach(authorizationsArrayBuilder::add);
		}

		return authorizationsArrayBuilder.build();
	}

	public JsonArray createMetaModelCategoriesAsJsonArray(SbiExtRoles role) {
		JsonArrayBuilder authorizationsArrayBuilder = Json.createArrayBuilder();

		if (!isNull(role.getSbiMetaModelCategories())) {
			role.getSbiMetaModelCategories()
				.stream()
				.map(this::fromSbiCategoryToJsonObject)
				.forEach(authorizationsArrayBuilder::add);
		}

		return authorizationsArrayBuilder.build();
	}

	public SbiEs.Builder createRoleEvent(SbiExtRoles role) {
		return createRoleEvent(role.getExtRoleId());
	}

	public SbiEs.Builder createRoleEvent(int roleId) {
		return SbiEs.Builder.newBuilder()
			.withType("Role")
			.withId(roleId);
	}

	protected final JsonObjectBuilder createCommonDataForEvent() {

		UserProfile userProfile = UserProfileManager.getProfile();

		JsonObject userProfileAsJson = Json.createObjectBuilder()
				.add("id", userProfile.getUserId().toString())
				.add("username", userProfile.getUserName().toString())
				.build();

		return Json.createObjectBuilder()
			.add("userProfile", userProfileAsJson);
	}

	protected final JsonObject fromSbiDomainsToJsonObject(SbiDomains e) {
		JsonObject ret = Json.createObjectBuilder()
				.add("domainCd", e.getDomainCd())
				.add("domainNm", e.getDomainNm())
				.add("valueCd", e.getValueCd())
				.add("valueDs", e.getValueDs())
				.add("valueNm", e.getValueNm())
				.add("valueId", e.getValueId())
				.build();

		return ret;
	}

	protected final JsonObject fromSbiCategoryToJsonObject(SbiCategory e) {
		JsonObject ret = Json.createObjectBuilder()
				.add("code", e.getCode())
				.add("name", e.getName())
				.add("type", e.getType())
				.add("id", e.getId())
				.build();

		return ret;
	}

}
