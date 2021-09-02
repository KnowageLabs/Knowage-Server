package it.eng.knowage.resourcemanager.resource;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import it.eng.spagobi.services.security.SecurityServiceService;

@Path("/1.0/resourcemanager")
@Component
@Validated
public class ResourceManagerResource {

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

}
