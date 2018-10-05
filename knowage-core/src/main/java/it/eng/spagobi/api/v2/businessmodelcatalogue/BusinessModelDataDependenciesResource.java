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
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class BusinessModelDataDependenciesResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(BusinessModelDataDependenciesResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	public List<MetaModelParuse> getDataDependenciesForBusinessModelDriver(@PathParam("id") Integer id, @QueryParam("driverId") Integer driverId) {
		logger.debug("IN");
		List<MetaModelParuse> dataDependencies = null;
		IMetaModelParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getMetaModelParuseDao();
			dataDependencies = parameterUseDAO.loadAllParuses(driverId);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be loaded", e);
			throw new SpagoBIRestServiceException(e.getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return dataDependencies;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public MetaModelParuse addDataDependenciesForBusinessModelDriver(@PathParam("id") Integer id, MetaModelParuse parameterUseObject) {
		logger.debug("IN");
		IMetaModelParuseDAO parameterUseDAO;

		Assert.assertNotNull(parameterUseObject, "Data Dependencies can not be null");
		try {
			parameterUseDAO = DAOFactory.getMetaModelParuseDao();
			parameterUseDAO.insertMetaModelParuse(parameterUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be created", e);
			throw new SpagoBIRestServiceException(e.getCause().getLocalizedMessage() + " in Data Dependency", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterUseObject;
	}

	@POST
	@Path("delete")
	public void deleteDataDependenciesForBusinessModelDriverByPost(@PathParam("id") Integer id, MetaModelParuse parameterUseObject) {
		logger.debug("IN");
		IMetaModelParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getMetaModelParuseDao();
			parameterUseDAO.eraseMetaModelParuse(parameterUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be deleted", e);
			throw new SpagoBIRestServiceException(e.getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}

	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	public MetaModelParuse setDataDependenciesForBusinessModelDriver(@PathParam("id") Integer id, MetaModelParuse parameterUseObject) {
		logger.debug("IN");
		IMetaModelParuseDAO parameterUseDAO;

		Assert.assertNotNull(parameterUseObject, "Data Dependencies can not be null");
		try {
			parameterUseDAO = DAOFactory.getMetaModelParuseDao();
			parameterUseDAO.modifyMetaModelParuse(parameterUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be modified", e);
			throw new SpagoBIRestServiceException(e.getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return parameterUseObject;
	}

	@DELETE
	@Consumes("application/json")
	public void deleteDataDependenciesForBusinessModelDriver(@PathParam("id") Integer id, MetaModelParuse parametarUseObject) {
		logger.debug("IN");
		IMetaModelParuseDAO parameterUseDAO;
		try {
			parameterUseDAO = DAOFactory.getMetaModelParuseDao();
			parameterUseDAO.eraseMetaModelParuse(parametarUseObject);
		} catch (HibernateException e) {
			logger.error("Data Dependencies can not be deleted", e);
			throw new SpagoBIRestServiceException(e.getCause().getLocalizedMessage(), buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
	}
}
