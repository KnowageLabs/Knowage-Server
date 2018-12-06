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
package it.eng.spagobi.api.v2.businessmodelcatalogue;

import java.util.List;

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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class BusinessModelDriversResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(BusinessModelDriversResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	public List<BIMetaModelParameter> getBusinessModelParameters(@PathParam("id") Integer id) {
		logger.debug("IN");
		List<BIMetaModelParameter> parameters = null;

		try {
			IBIMetaModelParameterDAO driversDao;
			driversDao = DAOFactory.getBIMetaModelParameterDAO();
			parameters = driversDao.loadBIMetaModelParameterByMetaModelId(id);

		} catch (HibernateException e) {
			logger.error("Driver could not be loaded", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");

		return parameters;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public BIMetaModelParameter addBusinessModelParameter(@PathParam("id") Integer id, BIMetaModelParameter parameter) {
		logger.debug("IN");
		Assert.assertNotNull(parameter, "Driver can not be null");
		IBIMetaModelParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIMetaModelParameterDAO();
			Integer driverId = parameterDAO.insertBIMetaModelParameter(parameter);
			parameter.setId(driverId);
		} catch (HibernateException e) {
			logger.error("Error while inserting new driver", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameter;
	}

	@PUT
	@Path("{driverId}")
	@Consumes("application/json")
	@Produces("application/json")
	public BIMetaModelParameter modifyBusinessModelParameter(@PathParam("id") Integer id, BIMetaModelParameter parameter) {
		logger.debug("IN");
		Assert.assertNotNull(parameter, "Driver can not be null");

		IBIMetaModelParameterDAO parameterDAO = null;
		try {
			parameterDAO = DAOFactory.getBIMetaModelParameterDAO();
			parameterDAO.modifyBIMetaModelParameter(parameter);
		} catch (HibernateException e) {
			logger.error("Error while inserting new driver", e);
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameter;
	}

	@GET
	@Path("{driverId}")
	@Produces("application/json")
	public BIMetaModelParameter getBusinessModelDriverById(@PathParam("id") Integer id, @PathParam("driverId") Integer driverId) {
		logger.debug("IN");
		IBIMetaModelParameterDAO parameterDAO = null;
		BIMetaModelParameter parameter = null;

		try {
			parameterDAO = DAOFactory.getBIMetaModelParameterDAO();
			parameter = parameterDAO.loadBIMetaModelParameterById(driverId);

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
	public void deleteBusinessModelDriverById(@PathParam("id") Integer id, @PathParam("driverId") Integer driverId) {
		logger.debug("IN");
		IBIMetaModelParameterDAO parameterDAO = null;
		BIMetaModelParameter parameter = null;
		try {
			parameterDAO = DAOFactory.getBIMetaModelParameterDAO();
			parameter = parameterDAO.loadBIMetaModelParameterById(driverId);

			Assert.assertNotNull(parameter, "Driver can not be null");
			parameterDAO.eraseBIMetaModelParameter(parameter);
		} catch (HibernateException e) {
			logger.error("Error while trying to delete the specified driver");
			throw new SpagoBIRestServiceException(e.getCause().getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");

	}

}
