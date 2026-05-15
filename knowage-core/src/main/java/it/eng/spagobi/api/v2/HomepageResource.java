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
package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.wapp.bo.*;
import it.eng.spagobi.wapp.dao.IHomepageDAO;
import it.eng.spagobi.wapp.util.MenuUtilities;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/2.0/homepage")
@ManageAuthorization
	public class HomepageResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(HomepageResource.class);
	private static final String CHARSET = "; charset=UTF-8";
	private static final String DEFAULT_ROLE_TOKEN = "default";

	@GET
	@Path("/default")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getDefaultHomepage() {
		try {
			IHomepageDAO homepageDAO = getHomepageDAO();
			Homepage homepage = homepageDAO.loadDefaultHomepage();
			if (homepage == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			return Response.ok(homepage).build();
		} catch (Exception e) {
			String errorString = "sbi.homepage.load.default.error";
			LOGGER.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, getLocale(), e);
		}
	}

	@GET
	@Path("/preview/{label}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response previewHomepageByRole(@PathParam("label") String label) {
		try {
			RoleRequest roleRequest = parseRoleRequest(label);
			Homepage homepage = roleRequest.isDefaultRequest()
					? getHomepageDAO().loadDefaultHomepage()
					: getHomepageDAO().loadHomepageByRoleName(roleRequest.getRoleName());
			if (homepage == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			if (roleRequest.isDefaultRequest()) {
				return Response.ok(homepage).build();
			}
			return Response.ok(filterHomepageForRole(homepage, roleRequest.getRole())).build();
		} catch (NotFoundException | SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			String errorString = "sbi.homepage.preview.error";
			LOGGER.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, getLocale(), e);
		}
	}

	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getHomepageByRole(@PathParam("label") String label) {
		try {
			RoleRequest roleRequest = parseRoleRequest(label);
			if (!roleRequest.isDefaultRequest() && !canReadRole(roleRequest.getRole())) {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			Homepage homepage = roleRequest.isDefaultRequest()
					? getHomepageDAO().loadDefaultHomepage()
					: getHomepageDAO().loadHomepageByRoleName(roleRequest.getRoleName());
			if (homepage == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			return Response.ok(homepage).build();
		} catch (NotFoundException | SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			String errorString = "sbi.homepage.load.error";
			LOGGER.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, getLocale(), e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MENU_MANAGEMENT })
	public Response createHomepage(@Context HttpServletRequest req) {
		try {
			Homepage homepage = readHomepage(req);
			validateHomepage(homepage);
			Homepage savedHomepage = getHomepageDAO().saveHomepage(homepage);
			return Response.ok(savedHomepage).build();
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			String errorString = "sbi.homepage.save.error";
			LOGGER.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, getLocale(), e);
		}
	}

	@PUT
	@Path("/{homepageId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MENU_MANAGEMENT })
	public Response updateHomepage(@PathParam("homepageId") Integer homepageId, @Context HttpServletRequest req) {
		try {
			Homepage homepage = readHomepage(req);
			homepage.setId(homepageId);
			validateHomepage(homepage);
			Homepage savedHomepage = getHomepageDAO().saveHomepage(homepage);
			return Response.ok(savedHomepage).build();
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			String errorString = "sbi.homepage.update.error";
			LOGGER.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, getLocale(), e);
		}
	}

	private IHomepageDAO getHomepageDAO() {
		IHomepageDAO homepageDAO = DAOFactory.getHomepageDAO();
		homepageDAO.setUserProfile(getUserProfile());
		return homepageDAO;
	}

	private Homepage readHomepage(HttpServletRequest request) throws Exception {
		JSONObject body = RestUtilities.readBodyAsJSONObject(request);
		if (body.has("roleIds") || body.has("roleId")) {
			throw validationException("Use roleNames or roleName instead of roleIds or roleId");
		}
		Homepage homepage = new Homepage();
		boolean hasExplicitDefaultFlag = body.has("default") || body.has("isDefault");
		boolean defaultHomepage = body.optBoolean("default", body.optBoolean("isDefault", false));
		homepage.setType(body.optString("type", null));

		if (body.has("documentId") && !body.isNull("documentId")) {
			homepage.setDocumentId(body.getInt("documentId"));
		} else if (body.has("document") && !body.isNull("document")) {
			homepage.setDocumentId(body.getInt("document"));
		}

		if (body.has("imageUrl") && !body.isNull("imageUrl")) {
			homepage.setImageUrl(body.getString("imageUrl"));
		}
		if (body.has("staticPage") && !body.isNull("staticPage")) {
			homepage.setStaticPage(body.getString("staticPage"));
		}

		if (body.has("template") && !body.isNull("template")) {
			JSONObject templateObject = body.getJSONObject("template");
			HomepageTemplate template = new HomepageTemplate();
			if (templateObject.has("html") && !templateObject.isNull("html")) {
				template.setHtml(templateObject.getString("html"));
			}
			if (templateObject.has("css") && !templateObject.isNull("css")) {
				template.setCss(templateObject.getString("css"));
			}
			if (templateObject.has("menuPlaceholders") && !templateObject.isNull("menuPlaceholders")) {
				template.setMenuPlaceholders(readMenuPlaceholders(templateObject.getJSONArray("menuPlaceholders")));
			}
			homepage.setTemplate(template);
		}

		List<String> roleNames = new ArrayList<>();
		boolean defaultRoleRequested = false;
		if (body.has("roleNames") && !body.isNull("roleNames")) {
			defaultRoleRequested = readRoleArray(body.getJSONArray("roleNames"), roleNames);
		}
		if (body.has("roleName") && !body.isNull("roleName")) {
			defaultRoleRequested = addRoleValue(body.get("roleName"), "roleName", roleNames) || defaultRoleRequested;
		}
		boolean inferDefaultHomepage = !hasExplicitDefaultFlag && !defaultRoleRequested && roleNames.isEmpty();
		homepage.setDefaultHomepage(defaultHomepage || defaultRoleRequested || inferDefaultHomepage);
		homepage.setRoleNames(roleNames.stream().distinct().collect(Collectors.toList()));
		return homepage;
	}

	private RoleRequest parseRoleRequest(String label) throws EMFUserError {
		if (label == null) {
			throw new NotFoundException();
		}
		String normalizedLabel = label.trim();
		if (normalizedLabel.isEmpty()) {
			throw new NotFoundException();
		}
		if (DEFAULT_ROLE_TOKEN.equalsIgnoreCase(normalizedLabel)) {
			return RoleRequest.defaultRequest();
		}

		Role role = loadRoleByLabel(normalizedLabel);
		if (role == null) {
			throw new NotFoundException();
		}
		return RoleRequest.forRole(role);
	}

	private Role loadRoleByLabel(String label) throws EMFUserError {
		IRoleDAO roleDAO = DAOFactory.getRoleDAO();
		roleDAO.setUserProfile(getUserProfile());
		return roleDAO.loadByName(label);
	}

	private List<MenuPlaceholder> readMenuPlaceholders(JSONArray jsonArray) throws JSONException {
		List<MenuPlaceholder> placeholders = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject placeholderObject = jsonArray.getJSONObject(i);
			MenuPlaceholder placeholder = new MenuPlaceholder();
			placeholder.setIndex(placeholderObject.getInt("index"));
			JSONArray menuIds = placeholderObject.optJSONArray("menuIds");
			List<Integer> ids = new ArrayList<>();
			if (menuIds != null) {
				ids.addAll(readIntegerArray(menuIds));
			}
			placeholder.setMenuIds(ids);
			placeholders.add(placeholder);
		}
		return placeholders;
	}

	private List<Integer> readIntegerArray(JSONArray jsonArray) throws JSONException {
		List<Integer> integers = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			integers.add(jsonArray.getInt(i));
		}
		return integers;
	}

	private boolean readRoleArray(JSONArray jsonArray, List<String> roleNames) throws JSONException {
		boolean defaultRoleRequested = false;
		for (int i = 0; i < jsonArray.length(); i++) {
			defaultRoleRequested = addRoleValue(jsonArray.get(i), "roleNames[" + i + "]", roleNames) || defaultRoleRequested;
		}
		return defaultRoleRequested;
	}

	private boolean addRoleValue(Object rawRoleValue, String fieldName, List<String> roleNames) {
		if (rawRoleValue == null || JSONObject.NULL.equals(rawRoleValue)) {
			throw validationException(fieldName + " cannot be null");
		}
		if (!(rawRoleValue instanceof String)) {
			throw validationException("Invalid " + fieldName + " value [" + rawRoleValue + "]");
		}

		String normalizedRoleValue = ((String) rawRoleValue).trim();
		if (normalizedRoleValue.isEmpty()) {
			throw validationException(fieldName + " cannot be blank");
		}
		if (DEFAULT_ROLE_TOKEN.equalsIgnoreCase(normalizedRoleValue)) {
			return true;
		}
		roleNames.add(normalizedRoleValue);
		return false;
	}

	private void validateHomepage(Homepage homepage) {
		HomepageType homepageType;
		try {
			homepageType = HomepageType.fromValue(homepage.getType());
		} catch (IllegalArgumentException e) {
			throw validationException(e.getMessage(), e);
		}
		homepage.setType(homepageType.getValue());

		if (homepage.isDefaultHomepage() && !homepage.getRoleNames().isEmpty()) {
			throw validationException("Default homepage cannot be explicitly associated to roles");
		}
		if (!homepage.isDefaultHomepage() && homepage.getRoleNames().isEmpty()) {
			throw validationException("At least one role must be provided for a non-default homepage");
		}

		switch (homepageType) {
		case DOCUMENT:
			require(homepage.getDocumentId() != null, "Document homepage requires documentId");
			require(isBlank(homepage.getImageUrl()), "Document homepage cannot define imageUrl");
			require(isBlank(homepage.getStaticPage()), "Document homepage cannot define staticPage");
			require(hasTemplateContent(homepage.getTemplate()), "Document homepage cannot define template");
			break;
		case IMAGE:
			require(!isBlank(homepage.getImageUrl()), "Image homepage requires imageUrl");
			require(homepage.getDocumentId() == null, "Image homepage cannot define documentId");
			require(isBlank(homepage.getStaticPage()), "Image homepage cannot define staticPage");
			require(hasTemplateContent(homepage.getTemplate()), "Image homepage cannot define template");
			break;
		case STATIC:
			require(!isBlank(homepage.getStaticPage()), "Static homepage requires staticPage");
			require(homepage.getDocumentId() == null, "Static homepage cannot define documentId");
			require(isBlank(homepage.getImageUrl()), "Static homepage cannot define imageUrl");
			require(hasTemplateContent(homepage.getTemplate()), "Static homepage cannot define template");
			break;
		case DYNAMIC:
			require(homepage.getTemplate() != null, "Dynamic homepage requires template");
			require(!isBlank(homepage.getTemplate().getHtml()), "Dynamic homepage requires template html");
			require(homepage.getDocumentId() == null, "Dynamic homepage cannot define documentId");
			require(isBlank(homepage.getImageUrl()), "Dynamic homepage cannot define imageUrl");
			require(isBlank(homepage.getStaticPage()), "Dynamic homepage cannot define staticPage");
			break;
		default:
			break;
		}
	}

	private Homepage filterHomepageForRole(Homepage homepage, Role role) throws EMFUserError {
		if (!HomepageType.DYNAMIC.getValue().equalsIgnoreCase(homepage.getType()) || homepage.getTemplate() == null) {
			return homepage;
		}

		Set<Integer> visibleMenuIds = loadVisibleMenuIds(role);
		Homepage filteredHomepage = copyHomepage(homepage);
		HomepageTemplate template = filteredHomepage.getTemplate();
		for (MenuPlaceholder placeholder : template.getMenuPlaceholders()) {
			placeholder.setMenuIds(placeholder.getMenuIds().stream()
					.filter(visibleMenuIds::contains)
					.collect(Collectors.toList()));
		}
		return filteredHomepage;
	}

	private Set<Integer> loadVisibleMenuIds(Role role) throws EMFUserError {
		if (role == null) {
			return new HashSet<>();
		}

		List menuItems = MenuUtilities.getMenuItemsForRole(getUserProfile(), role.getName());

		Set<Integer> menuIds = new HashSet<>();
		collectVisibleMenuIds(menuItems, menuIds);
		return menuIds;
	}

	private void collectVisibleMenuIds(List menuItems, Set<Integer> menuIds) {
		for (Object menuObject : menuItems) {
			Menu menu = (Menu) menuObject;
			if (menu.isClickable()) {
				menuIds.add(menu.getMenuId());
			}
			if (menu.getLstChildren() != null && !menu.getLstChildren().isEmpty()) {
				collectVisibleMenuIds(menu.getLstChildren(), menuIds);
			}
		}
	}

	private Homepage copyHomepage(Homepage homepage) {
		Homepage copy = new Homepage();
		copy.setId(homepage.getId());
		copy.setDefaultHomepage(homepage.isDefaultHomepage());
		copy.setType(homepage.getType());
		copy.setDocumentId(homepage.getDocumentId());
		copy.setImageUrl(homepage.getImageUrl());
		copy.setStaticPage(homepage.getStaticPage());
		copy.setRoleNames(new ArrayList<>(homepage.getRoleNames()));

		if (homepage.getTemplate() != null) {
			HomepageTemplate template = new HomepageTemplate();
			template.setHtml(homepage.getTemplate().getHtml());
			template.setCss(homepage.getTemplate().getCss());
			List<MenuPlaceholder> placeholders = new ArrayList<>();
			for (MenuPlaceholder placeholder : homepage.getTemplate().getMenuPlaceholders()) {
				MenuPlaceholder copyPlaceholder = new MenuPlaceholder();
				copyPlaceholder.setIndex(placeholder.getIndex());
				copyPlaceholder.setMenuIds(new ArrayList<>(placeholder.getMenuIds()));
				placeholders.add(copyPlaceholder);
			}
			template.setMenuPlaceholders(placeholders);
			copy.setTemplate(template);
		}
		return copy;
	}

	private boolean canReadRole(Role role) throws EMFInternalError {
		UserProfile userProfile = getUserProfile();
		if (userProfile.isAbleToExecuteAction(CommunityFunctionalityConstants.MENU_MANAGEMENT)) {
			return true;
		}
		if (role == null) {
			return false;
		}

		Collection<String> availableRoles = userProfile.getRolesForUse();
		return availableRoles.contains(role.getName());
	}

	private boolean hasTemplateContent(HomepageTemplate template) {
		return template == null || (isBlank(template.getHtml())
				&& isBlank(template.getCss())
				&& (template.getMenuPlaceholders() == null || template.getMenuPlaceholders().isEmpty()));
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private void require(boolean condition, String message) {
		if (!condition) {
			throw validationException(message);
		}
	}

	private SpagoBIRuntimeException validationException(String message) {
		return new SpagoBIRuntimeException(message);
	}

	private SpagoBIRuntimeException validationException(String message, Throwable throwable) {
		return new SpagoBIRuntimeException(message, throwable);
	}

	private static final class RoleRequest {

		private final Role role;
		private final boolean defaultRequest;

		private RoleRequest(Role role, boolean defaultRequest) {
			this.role = role;
			this.defaultRequest = defaultRequest;
		}

		private static RoleRequest defaultRequest() {
			return new RoleRequest(null, true);
		}

		private static RoleRequest forRole(Role role) {
			return new RoleRequest(role, false);
		}

		private Role getRole() {
			return role;
		}

		private String getRoleName() {
			return role != null ? role.getName() : null;
		}

		private boolean isDefaultRequest() {
			return defaultRequest;
		}
	}

}
