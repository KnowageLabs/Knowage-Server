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

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/exporters")
@Produces(MediaType.APPLICATION_JSON)
public class ExportersResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(ExportersResource.class);

	@GET
	@Path("/{label}")
	public ExportersResourceEngineListResponse get(@PathParam("label") String engineLabel) {
		return getExportersByConfigOldVersion(engineLabel);
	}

	/**
	 * @deprecated Prefer {@link #get(String)}
	 */
	@GET
	@Path("/config/{label}")
	@Deprecated
	public ExportersResourceEngineListResponse getExportersByConfigOldVersion(@PathParam("label") String engineLabel) {

		LOGGER.debug("IN: Engine label = {}", engineLabel);
		ExportersResourceEngineListResponse ret = new ExportersResourceEngineListResponse();
		List<ExportersResourceEngineListItem> exporters = ret.getExporters();

		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
			configsDao.setUserProfile(getUserProfile());

			IEngineDAO engineDao = DAOFactory.getEngineDAO();
			engineDao.setUserProfile(getUserProfile());

			Engine engine = engineDao.loadEngineByLabel(engineLabel);
			if (nonNull(engine)) {
				List<String> associatedExporters = engineDao.getAssociatedExporters(engine);

				for (String associatedExporter : associatedExporters) {
					ExportersResourceEngineListItem e = new ExportersResourceEngineListItem();
					e.setName(associatedExporter);
					exporters.add(e);
				}

				LOGGER.debug("Getting exporters for engine label=[{}] - done successfully", engineLabel);
			}

		} catch (Exception exception) {

			String messageToSend = String.format("Error while getting exporters for engine: [%s]", engineLabel);
			LOGGER.error(messageToSend, exception);
			throw new SpagoBIServiceException(messageToSend, exception);

		}

		return ret;
	}

}

class ExportersResourceEngineListResponse {

	private final List<ExportersResourceEngineListItem> exporters = new ArrayList<>();

	/**
	 * @return the exporters
	 */
	public List<ExportersResourceEngineListItem> getExporters() {
		return exporters;
	}

}

class ExportersResourceEngineListItem {

	private String name;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}