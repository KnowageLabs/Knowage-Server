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

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleBO;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/2.0/roles")
@ManageAuthorization
public class RolesResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getRoles() {
		IRoleDAO rolesDao = null;
		List<Role> fullList = null;

		try {

			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			fullList = rolesDao.loadAllRoles();
			return Response.ok(fullList).build();
		} catch (Exception e) {
			logger.error("Error with loading resource", e);
			throw new SpagoBIRestServiceException("Error with loading resource", buildLocaleFromSession(), e);
		}
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
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Path("/categories/{id}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getCategoriesById(@PathParam("id") Integer id) {
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

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertRole(@Valid RoleBO body) {
		IRoleDAO rolesDao = null;
		Role role = BOtoRole(body);
		List<RoleMetaModelCategory> list = body.getRoleMetaModelCategories();

		try {
			rolesDao = DAOFactory.getRoleDAO();
			rolesDao.setUserProfile(getUserProfile());
			Integer id = rolesDao.insertRoleComplete(role);
			if (list != null) {
				for (RoleMetaModelCategory roleMetaModelCategory : list) {
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

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateRole(@PathParam("id") Integer id, @Valid RoleBO body) {

		IRoleDAO rolesDao = null;
		IDomainDAO domainsDao = null;
		Role role = BOtoRole(body);
		role.setId(body.getId());
		List<RoleMetaModelCategory> list = body.getRoleMetaModelCategories();
		List<Domain> listAll;

		try {
			rolesDao = DAOFactory.getRoleDAO();
			domainsDao = DAOFactory.getDomainDAO();
			rolesDao.setUserProfile(getUserProfile());
			rolesDao.modifyRole(role);
			listAll = domainsDao.loadListDomainsByType("BM_CATEGORY");
			for (Domain domain : listAll) {
				rolesDao.removeRoleMetaModelCategory(role.getId(), domain.getValueId());
			}
			if (list != null) {
				for (RoleMetaModelCategory roleMetaModelCategory : list) {
					rolesDao.insertRoleMetaModelCategory(role.getId(), roleMetaModelCategory.getCategoryId());
				}
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

		role.setIsAbleToSaveIntoPersonalFolder(bo.isAbleToSaveIntoPersonalFolder());
		role.setIsAbleToEnableDatasetPersistence(bo.isAbleToEnableDatasetPersistence());
		role.setIsAbleToEnableFederatedDataset(bo.isAbleToEnableFederatedDataset());
		role.setAbleToManageGlossaryBusiness(bo.isAbleToManageGlossaryBusiness());
		role.setAbleToManageGlossaryTechnical(bo.isAbleToManageGlossaryTechnical());
		role.setIsAbleToSaveSubobjects(bo.isAbleToSaveSubobjects());
		role.setIsAbleToSeeSubobjects(bo.isAbleToSeeSubobjects());
		role.setIsAbleToSeeViewpoints(bo.isAbleToSeeViewpoints());
		role.setIsAbleToSeeSnapshots(bo.isAbleToSeeSnapshots());
		role.setIsAbleToSeeNotes(bo.isAbleToSeeNotes());
		role.setIsAbleToSendMail(bo.isAbleToSendMail());
		role.setIsAbleToEditWorksheet(bo.isAbleToEditWorksheet());
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
}
