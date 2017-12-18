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

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 * 
 */
public class OAuth2MetadataInitializer extends MetadataInitializer {

	// It differs from MetadataInitializer() because it adds also tenants retrieved from fi-ware application
	public OAuth2MetadataInitializer() {
		targetComponentName = "SpagoBI Metadata Database";

		metadataInitializers = new ArrayList<SpagoBIInitializer>();
		metadataInitializers.add(new TenantsInitializer());
		metadataInitializers.add(new OAuth2TenantInitializer());
		metadataInitializers.add(new DomainsInitializer());
		metadataInitializers.add(new EnginesInitializer());
		metadataInitializers.add(new ChecksInitializer());
		metadataInitializers.add(new LovsInitializer());
		metadataInitializers.add(new FunctionalitiesInitializer());
		metadataInitializers.add(new ExportersInitializer());
		metadataInitializers.add(new ConfigurationsInitializer());
		metadataInitializers.add(new AlertListenerInitializer());
		metadataInitializers.add(new AlertActionInitializer());
		// metadataInitializers.add(new KpiPeriodicityInitializer());
		// metadataInitializers.add(new UnitGrantInitializer());
	}
}
