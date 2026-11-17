package it.eng.spagobi.commons.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spagobi.security.saml.FederatedSessionStore;
import it.eng.spagobi.security.saml.SamlConfiguration;
import it.eng.spagobi.security.saml.SamlProtocolService;
import it.eng.spagobi.services.rest.annotations.PublicService;

// Keep the full root path to take precedence over EclipseLink's /{context}/metadata/.
@Path("/saml/metadata")
public class SamlMetadataResource {
	@GET
	@PublicService
	public Response metadata() throws Exception {
		if (!SamlConfiguration.enabled()) {
			return Response.status(404).build();
		}
		SamlConfiguration config = SamlConfiguration.load();
		return Response.ok(new SamlProtocolService(config, FederatedSessionStore.configured()).metadata(), MediaType.APPLICATION_XML).build();
	}
}
