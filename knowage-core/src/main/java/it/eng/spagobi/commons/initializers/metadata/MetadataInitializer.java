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
package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;

/**
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 *         This class initializes SpagoBI metadata repository, if it is empty, with predefined:
 *
 *         - domains, - checks, - lovs, - engines, - user functionalities
 **/
public class MetadataInitializer extends SpagoBIInitializer {

	protected List<SpagoBIInitializer> metadataInitializers;

	private static boolean disposed = false;
	private static Logger logger = Logger.getLogger(MetadataInitializer.class);

	public MetadataInitializer() {
		targetComponentName = "SpagoBI Metadata Database";

		metadataInitializers = new ArrayList<>();
		metadataInitializers.add(new DomainsInitializer());
		metadataInitializers.add(new EnginesInitializer());
		metadataInitializers.add(new ProductTypesInitializer());
		metadataInitializers.add(new TenantsInitializer());
		metadataInitializers.add(new DataSourceInitializer());
		metadataInitializers.add(new ChecksInitializer());
		metadataInitializers.add(new LovsInitializer());
		metadataInitializers.add(new FunctionalitiesInitializer());
		metadataInitializers.add(new ExportersInitializer());
		metadataInitializers.add(new ConfigurationsInitializer());
		metadataInitializers.add(new AlertListenerInitializer());
		metadataInitializers.add(new AlertActionInitializer());
		metadataInitializers.add(new WsEventCleanJobInitializer());

	}

	@Override
	public void init(SourceBean config) {
		if (disposed) {
			logger.warn("[" + targetComponentName + "] hsa been already initialized");
		} else {
			super.init(config);
			disposed = true;
		}
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {

		long startTime;
		long endTime;

		logger.debug("IN");

		try {
			for (SpagoBIInitializer metadataInitializer : metadataInitializers) {
				startTime = System.currentTimeMillis();
				try {
					metadataInitializer.init(config, hibernateSession);
				} catch (Exception e) {
					logger.error("Error running initializer " + metadataInitializer.getClass().getName(), e);
				}
				endTime = System.currentTimeMillis();
				logger.info("[" + metadataInitializer.getTargetComponentName() + "] succesfully initializated in " + (endTime - startTime) + " ms");
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occured while initializing metadata", t);
		} finally {
			logger.debug("OUT");
		}
	}
}