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
package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class DataDependenciesResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataDependenciesResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<ObjParuse> getDataDependeciesForDocumentDriver(@PathParam("id") Integer id, @QueryParam("driverId") Integer driverId) {
		logger.debug("IN");
		List<ObjParuse> dataDependencies = null;
		IObjParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getObjParuseDAO();
			dataDependencies = parameterUseDAO.loadObjParuses(driverId);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be loaded", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return dataDependencies;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public ObjParuse addDataDependeciesForDocumentDriver(@PathParam("id") Integer id, ObjParuse parameterUseObject) {
		logger.debug("IN");
		IObjParuseDAO parameterUseDAO;

		Assert.assertNotNull(parameterUseObject, "Data Dependencies can not be null");
		try {
			parameterUseDAO = DAOFactory.getObjParuseDAO();
			parameterUseObject.setId(parameterUseDAO.insertObjParuse(parameterUseObject));
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be created", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage() + " in Data Dependency", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterUseObject;
	}

	@POST
	@Path("delete")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteDataDependeciesForDocumentDriverByPost(@PathParam("id") Integer id, ObjParuse parameterUseObject) {
		logger.debug("IN");
		IObjParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getObjParuseDAO();
			parameterUseDAO.eraseObjParuse(parameterUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be deleted", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}

	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public ObjParuse setDataDependeciesForDocumentDriver(@PathParam("id") Integer id, ObjParuse parameterUseObject) {
		logger.debug("IN");
		IObjParuseDAO parameterUseDAO;

		Assert.assertNotNull(parameterUseObject, "Data Dependencies can not be null");
		try {
			parameterUseDAO = DAOFactory.getObjParuseDAO();
			parameterUseDAO.modifyObjParuse(parameterUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be modified", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterUseObject;
	}

	@DELETE
	@Consumes("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteDataDependeciesForDocumentDriver(@PathParam("id") Integer id, ObjParuse parametarUseObject) {
		logger.debug("IN");
		IObjParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getObjParuseDAO();
			parameterUseDAO.eraseObjParuse(parametarUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be deleted", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}
}
