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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleBO;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.domains.DomainCRUD;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/roles")
@ManageAuthorization
public class RolesResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT, SpagoBIConstants.READ_ROLES })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getRoles() {
		IRoleDAO rolesDao = null;
		List<Role> filteredList = null;

		try {
			List<Role> fullList = null;

			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			fullList = rolesDao.loadAllRoles();

			IEngUserProfile profile = this.getUserProfile();
			if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT) || profile.isAbleToExecuteAction(SpagoBIConstants.READ_ROLES)) {
				filteredList = fullList;
			} else {
				// user with FINAL_USERS_MANAGEMENT (users with neither
				// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by
				// the
				// business_map.xml therefore they cannot execute this action)
				filteredList = this.filterRolesListForFinalUser(fullList);
			}

			return Response.ok(filteredList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}
	}

	private List<Role> filterRolesListForFinalUser(List<Role> allRoles) {
		List<Role> toReturn = new ArrayList<Role>();
		for (Role role : allRoles) {
			if (role.getRoleTypeCD().equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_USER)) {
				toReturn.add(role);
			}
		}
		return toReturn;
	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getRoleById(@PathParam("id") Integer id) {
		IRoleDAO rolesDao = null;

		try {
			Role role = new Role();
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			role = rolesDao.loadByID(id);
			return Response.ok(role).build();
		} catch (Exception e) {
			logger.error("Role with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + charset)
	@Path("/idsByNames")
	public Response getRolesIdsByName(@QueryParam("name") List<String> roleNames) {
		IRoleDAO rolesDao = null;
		List roles = new ArrayList<>();
		try {
			for (int i = 0; i < roleNames.size(); i++) {
				Role role = new Role();
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(getUserProfile());
				role = rolesDao.loadByName(roleNames.get(i));
				roles.add(role.getId());
			}

			return Response.ok(roles).build();
		} catch (Exception e) {
			logger.error("Role with selected id: " + roles + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + roles + " doesn't exists", buildLocaleFromSession(), e);
		}
	}

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/categories/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getMetaModelCategoriesById(@PathParam("id") Integer id) {
		IRoleDAO rolesDao = null;

		try {
			Role role = new Role();
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			role = rolesDao.loadByID(id);
			List<RoleMetaModelCategory> meta = rolesDao.getMetaModelCategoriesForRole(role.getId());
			return Response.ok(meta).build();
		} catch (Exception e) {
			logger.error("Role with selected id: " + id + " doesn't exists", e);
			throw new SpagoBIRestServiceException("Item with selected id: " + id + " doesn't exists", buildLocaleFromSession(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "unchecked" })
	@GET
	@Path("/ds_categories")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public String getDataSetCategories() {
		IRoleDAO rolesDao = null;
		List<RoleMetaModelCategory> ds = new ArrayList<RoleMetaModelCategory>();
		List<Domain> resp = new ArrayList<Domain>();

		try {
			UserProfile up = getUserProfile();
			Collection<String> roles = up.getRoles();

			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
			List<Domain> array = categoryDao.getCategoriesForDataset()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());

			if (UserUtilities.isAdministrator(up)) {
				resp = array;

			} else {
				for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
					String role = (String) iterator.next();
					rolesDao = DAOFactory.getRoleDAO();
					rolesDao.setUserProfile(getUserProfile());
					ds.addAll(rolesDao.getDataSetCategoriesForRole(role));
				}
				for (RoleMetaModelCategory r : ds) {
					for (Domain dom : array) {
						if (r.getCategoryId().equals(dom.getValueId())) {
							resp.add(dom);
						}
					}
				}
			}

			return DomainCRUD.translate(resp, null).toString();
		} catch (Exception e) {
			logger.error("Error loading the list of dataset categories associated to user", e);
			throw new SpagoBIRestServiceException("Error loading the list of dataset categories associated to user", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertRole(@Valid RoleBO body) {
		IRoleDAO rolesDao = null;
		Role role = BOtoRole(body);
		List<RoleMetaModelCategory> listMetaModelCategories = body.getRoleMetaModelCategories();
		try {
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			Integer id = rolesDao.insertRoleComplete(role);
			if (listMetaModelCategories != null) {
				for (RoleMetaModelCategory roleMetaModelCategory : listMetaModelCategories) {
					rolesDao.insertRoleMetaModelCategory(id, roleMetaModelCategory.getCategoryId());
				}
			}

			String encodedRole = URLEncoder.encode("" + role.getId(), "UTF-8");
			return Response.created(new URI("2.0/roles/" + encodedRole)).entity(encodedRole).build();
		} catch (Exception e) {
			logger.error("Error while inserting resource", e);
			throw new SpagoBIRestServiceException("Error while inserting resource", buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRole(@PathParam("id") Integer id, @Valid RoleBO body) {

		Role role = BOtoRole(body);
		role.setId(body.getId());
		List<RoleMetaModelCategory> listMetaModelCategories = body.getRoleMetaModelCategories();
		List<Domain> listAll;

		try {
			IRoleDAO rolesDao = DAOFactory.getRoleDAO();
			IDomainDAO domainsDao = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
			rolesDao.setUserProfile(getUserProfile());
			rolesDao.modifyRole(role);

			// update Business Model categories
			listAll = categoryDao.getCategoriesForBusinessModel()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
			for (Domain domain : listAll) {
				rolesDao.removeRoleMetaModelCategory(role.getId(), domain.getValueId());
			}
			listAll = categoryDao.getCategoriesForKpi()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
			for (Domain domain : listAll) {
				rolesDao.removeRoleMetaModelCategory(role.getId(), domain.getValueId());
			}
			listAll = categoryDao.getCategoriesForDataset()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
			for (Domain domain : listAll) {
				rolesDao.removeRoleMetaModelCategory(role.getId(), domain.getValueId());
			}
			if (listMetaModelCategories != null) {
				for (RoleMetaModelCategory roleMetaModelCategory : listMetaModelCategories) {
					rolesDao.insertRoleMetaModelCategory(role.getId(), roleMetaModelCategory.getCategoryId());
				}
			}
			// update Data Set categories
			listAll = categoryDao.getCategoriesForDataset()
					.stream()
					.map(Domain::fromCategory)
					.collect(toList());
			for (Domain domain : listAll) {
				rolesDao.removeRoleDataSetCategory(role.getId(), domain.getValueId());
			}

			String encodedRole = URLEncoder.encode("" + role.getId(), "UTF-8");
			return Response.created(new URI("2.0/roles/" + encodedRole)).entity(encodedRole).build();
		} catch (Exception e) {
			logger.error("Error while modifying resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error while modifying resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	public Response deleteRole(@PathParam("id") Integer id) {

		IRoleDAO rolesDao = null;

		try {
			Role role = new Role();
			role.setId(id);
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			// Remove Role - Business Model Categories Associations
			List<RoleMetaModelCategory> RoleMetaModelCategories = rolesDao.getMetaModelCategoriesForRole(id);
			for (RoleMetaModelCategory roleMetaModelCategory : RoleMetaModelCategories) {
				rolesDao.removeRoleMetaModelCategory(roleMetaModelCategory.getRoleId(), roleMetaModelCategory.getCategoryId());
			}

			rolesDao.eraseRole(role);
			String encodedRole = URLEncoder.encode("" + id, "UTF-8");
			return Response.ok().entity(encodedRole).build();
		} catch (Exception e) {
			logger.error("Error with deleting resource with id: " + id, e);
			throw new SpagoBIRestServiceException("Error with deleting resource with id: " + id, buildLocaleFromSession(), e);
		}
	}

	public Role BOtoRole(RoleBO bo) {
		Role role = new Role();
		role.setName(bo.getName());
		role.setCode(bo.getCode());
		role.setDescription(bo.getDescription());
		role.setRoleTypeCD(bo.getRoleTypeCD());
		role.setRoleTypeID(bo.getRoleTypeID());
		role.setIsPublic(bo.getIsPublic());
		role.setIsAbleToSaveIntoPersonalFolder(bo.isAbleToSaveIntoPersonalFolder());
		role.setIsAbleToEnableDatasetPersistence(bo.isAbleToEnableDatasetPersistence());
		role.setIsAbleToEnableFederatedDataset(bo.isAbleToEnableFederatedDataset());
		role.setIsAbleToEnableRate(bo.isAbleToEnableRate());
		role.setIsAbleToEnablePrint(bo.isAbleToEnablePrint());
		role.setIsAbleToEnableCopyAndEmbed(bo.isAbleToEnableCopyAndEmbed());
		role.setAbleToManageGlossaryBusiness(bo.isAbleToManageGlossaryBusiness());
		role.setAbleToManageGlossaryTechnical(bo.isAbleToManageGlossaryTechnical());
		role.setAbleToManageKpiValue(bo.isAbleToManageKpiValue());
		role.setAbleToManageCalendar(bo.isAbleToManageCalendar());
		role.setAbleToManageInternationalization(bo.isAbleToManageInternationalization());
		role.setAbleToCreateSelfServiceCockpit(bo.isAbleToCreateSelfServiceCockpit());
		role.setAbleToCreateSelfServiceGeoreport(bo.isAbleToCreateSelfServiceGeoreport());
		role.setAbleToCreateSelfServiceKpi(bo.isAbleToCreateSelfServiceKpi());
		role.setAbleToUseFunctionsCatalog(bo.isAbleToUseFunctionsCatalog());
		role.setIsAbleToEditPythonScripts(bo.isAbleToEditPythonScripts());
		role.setIsAbleToCreateCustomChart(bo.isAbleToCreateCustomChart());
		role.setIsAbleToSaveSubobjects(bo.isAbleToSaveSubobjects());
		role.setIsAbleToSeeSubobjects(bo.isAbleToSeeSubobjects());
		role.setIsAbleToSeeViewpoints(bo.isAbleToSeeViewpoints());
		role.setIsAbleToSeeSnapshots(bo.isAbleToSeeSnapshots());
		role.setIsAbleToRunSnapshots(bo.isAbleToRunSnapshots());
		role.setIsAbleToSeeNotes(bo.isAbleToSeeNotes());
		role.setIsAbleToSendMail(bo.isAbleToSendMail());
		role.setIsAbleToSaveRememberMe(bo.isAbleToSaveRememberMe());
		role.setIsAbleToSeeMetadata(bo.isAbleToSeeMetadata());
		role.setIsAbleToSaveMetadata(bo.isAbleToSaveMetadata());
		role.setIsAbleToBuildQbeQuery(bo.isAbleToBuildQbeQuery());
		role.setIsAbleToDoMassiveExport(bo.isAbleToDoMassiveExport());
		role.setIsAbleToManageUsers(bo.isAbleToManageUsers());
		role.setIsAbleToSeeDocumentBrowser(bo.isAbleToSeeDocumentBrowser());
		role.setIsAbleToSeeFavourites(bo.isAbleToSeeFavourites());
		role.setIsAbleToSeeSubscriptions(bo.isAbleToSeeSubscriptions());
		role.setIsAbleToSeeMyData(bo.isAbleToSeeMyData());
		role.setIsAbleToSeeMyWorkspace(bo.isAbleToSeeMyWorkspace());
		role.setIsAbleToSeeToDoList(bo.isAbleToSeeToDoList());
		role.setIsAbleToCreateDocuments(bo.isAbleToCreateDocuments());
		role.setIsAbleToCreateSocialAnalysis(bo.isAbleToCreateSocialAnalysis());
		role.setIsAbleToViewSocialAnalysis(bo.isAbleToViewSocialAnalysis());
		role.setIsAbleToHierarchiesManagement(bo.isAbleToHierarchiesManagement());
		role.setAbleToEditAllKpiComm(bo.isAbleToEditAllKpiComm());
		role.setAbleToEditMyKpiComm(bo.isAbleToEditMyKpiComm());
		role.setAbleToDeleteKpiComm(bo.isAbleToDeleteKpiComm());

		return role;
	}

	/**
	 * Service for getting list of Roles only with id and name of role
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT, SpagoBIConstants.FINAL_USERS_MANAGEMENT })
	@Path("/short")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getRolesSimeple() {
		IRoleDAO rolesDao = null;
		List<Role> filteredList = null;

		try {
			List<Role> fullList = null;

			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			fullList = rolesDao.loadAllRoles();

			IEngUserProfile profile = this.getUserProfile();
			if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
				filteredList = fullList;
			} else {
				filteredList = this.filterRolesListForFinalUser(fullList);
			}

			JSONArray filteredListArray = new JSONArray();
			for (int i = 0; i < filteredList.size(); i++) {
				Role aRole = filteredList.get(i);
				JSONObject aRoleJson = new JSONObject();
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
				filteredListArray.put(aRoleJson);
			}

			return Response.ok(filteredListArray.toString()).build();
		} catch (Exception e) {
			String errorString = "sbi.folder.roles.load.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}
}
