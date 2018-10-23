/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */

@Path("/2.0/databases")
public class DataBaseResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataBaseResource.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@PublicService
	public List<IDataBase> getDataBases() {
		logger.debug("IN");
		try {
			List<IDataBase> databases = new ArrayList<>();
			for (DatabaseDialect sqlDialect : DatabaseDialect.values()) {
				if (!sqlDialect.equals(DatabaseDialect.METAMODEL)) {
					IDataSource datasource = DataSourceFactory.getDataSource();
					datasource.setHibDialectClass(sqlDialect.getValue());
					IDataBase database = DataBaseFactory.getDataBase(datasource);
					databases.add(database);
				}
			}
			Collections.sort(databases);
			return databases;
		} catch (Exception e) {
			logger.error(e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public IDataBase getDataBase(@PathParam("label") String label) {
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(label);
			return DataBaseFactory.getDataBase(dataSource);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}
}