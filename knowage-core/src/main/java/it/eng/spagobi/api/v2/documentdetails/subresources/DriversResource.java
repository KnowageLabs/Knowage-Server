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

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class DriversResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DriversResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<BIObjectParameter> getDocumentParameters(@PathParam("id") Integer id) {
		logger.debug("IN");
		List<BIObjectParameter> biObjectParameters = null;
		BIObjectParameter test1 = new BIObjectParameter();
		try {
			IBIObjectParameterDAO driversDao;
			driversDao = DAOFactory.getBIObjectParameterDAO();
			biObjectParameters = driversDao.loadBIObjectParametersById(id);

		} catch (HibernateException e) {
			logger.error("Driver could not be loaded", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return biObjectParameters;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObjectParameter addDocumentParameter(@PathParam("id") Integer id, @Valid BIObjectParameter biObjectParameter) {
		logger.debug("IN");
		Assert.assertNotNull(biObjectParameter, "Driver can not be null");
		IBIObjectParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
			Integer driverId = parameterDAO.insertBIObjectParameter(biObjectParameter);
			biObjectParameter.setId(driverId);
		} catch (HibernateException e) {
			logger.error("Error while inserting new driver", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return biObjectParameter;
	}

	@PUT
	@Path("{driverId}")
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObjectParameter modifyDocumentParameter(@PathParam("id") Integer id, @Valid BIObjectParameter biObjectParameter) {
		logger.debug("IN");
		Assert.assertNotNull(biObjectParameter, "Driver can not be null");

		IBIObjectParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
			parameterDAO.modifyBIObjectParameter(biObjectParameter);
		} catch (HibernateException e) {
			logger.error("Error while inserting new driver", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return biObjectParameter;
	}

	@GET
	@Path("{driverId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public BIObjectParameter getDocumentDriverbyId(@PathParam("id") Integer id, @PathParam("driverId") Integer driverId) {
		logger.debug("IN");
		IBIObjectParameterDAO parameterDAO = null;
		BIObjectParameter parameter = null;

		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
			parameter = parameterDAO.loadBiObjParameterById(driverId);

			Assert.assertNotNull(parameter, "Driver can not be null");
		} catch (HibernateException e) {
			logger.error("Error while try to retrieve the specified driver", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameter;
	}

	@DELETE
	@Path("{driverId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteDocumentDriverById(@PathParam("id") Integer id, @PathParam("driverId") Integer driverId) {
		logger.debug("IN");
		IBIObjectParameterDAO parameterDAO = null;
		BIObjectParameter parameter = null;
		try {
			parameterDAO = DAOFactory.getBIObjectParameterDAO();
			parameter = parameterDAO.loadBiObjParameterById(driverId);

			Assert.assertNotNull(parameter, "Driver can not be null");
			parameterDAO.eraseBIObjectParameter(parameter, true);
		} catch (HibernateException e) {
			logger.error("Error while trying to delete the specified driver");
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");

	}

}
