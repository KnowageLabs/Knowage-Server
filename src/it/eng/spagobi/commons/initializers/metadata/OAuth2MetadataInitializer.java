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
		metadataInitializers.add(new KpiPeriodicityInitializer());
		metadataInitializers.add(new UnitGrantInitializer());
	}
}
