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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class VisualDependenciesResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(VisualDependenciesResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<ObjParview> getVisualDependencies(@PathParam("id") Integer id, @QueryParam("driverId") Integer driverId) {
		logger.debug("IN");
		List<ObjParview> parameterViewObjects = null;
		IObjParviewDAO parameterViewDAO;
		try {
			parameterViewDAO = DAOFactory.getObjParviewDAO();
			parameterViewObjects = parameterViewDAO.loadObjParviews(driverId);
			Assert.assertNotNull(parameterViewObjects, "Visual Dependencies can not be null");
		} catch (HibernateException e) {
			logger.error("Visual dependencies could not be loaded", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterViewObjects;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public ObjParview addVisualDependeciesForDocumentDriver(@PathParam("id") Integer id, ObjParview parameterViewObject) {
		logger.debug("IN");
		IObjParviewDAO parameterViewDAO;

		Assert.assertNotNull(parameterViewObject, "Visual Dependencies can not be null");
		try {
			parameterViewDAO = DAOFactory.getObjParviewDAO();
			parameterViewObject.setId(parameterViewDAO.insertObjParview(parameterViewObject));
		} catch (HibernateException e) {
			logger.error("Visual Dependencies can not be created", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage() + "in Visual Dependencsies", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterViewObject;

	}

	@POST
	@Path("delete")
	@Consumes("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteVisualDependeciesForDocumentDriverByPost(@PathParam("id") Integer id, ObjParview parameterViewObject) {
		logger.debug("IN");
		IObjParviewDAO parameterViewDAO;
		try {
			parameterViewDAO = DAOFactory.getObjParviewDAO();
			parameterViewDAO.eraseObjParview(parameterViewObject);
		} catch (HibernateException e) {
			logger.error("Visual Dependencies can not be removed", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}

	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void setVisualDependeciesForDocumentDriver(@PathParam("id") Integer id, ObjParview parameterViewObject) {
		logger.debug("IN");
		IObjParviewDAO parameterViewDAO;

		Assert.assertNotNull(parameterViewObject, "Visual Dependencies can not be null");
		try {
			parameterViewDAO = DAOFactory.getObjParviewDAO();
			parameterViewDAO.modifyObjParview(parameterViewObject);
		} catch (HibernateException e) {
			logger.error("Visual Dependencies can not be modified", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}

	@DELETE
	@Consumes("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteVisualDependeciesForDocumentDriver(ObjParview parameterViewObject) {
		logger.debug("IN");
		IObjParviewDAO parameterViewDAO;
		try {
			parameterViewDAO = DAOFactory.getObjParviewDAO();
			parameterViewDAO.eraseObjParview(parameterViewObject);
		} catch (HibernateException e) {
			logger.error("Visual Dependencies can not be removed", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}
}
