package it.eng.spagobi.api.v2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/tenant")
public class TenantResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(TenantResource.class);
	private static final String CHARSET = "; charset=UTF-8";

	@GET
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.PROFILE_MANAGEMENT, CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT })
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET)
	public Response getTenatByName(@PathParam("name") String name) {
		ITenantsDAO tenantDao = DAOFactory.getTenantsDAO();
		JSONObject tenantJSON = new JSONObject();
		try {
			SbiTenant tenant = tenantDao.loadTenantByName(name);
			if (tenant != null) {
				tenantJSON.put("root", SerializerFactory.getSerializer("application/json").serialize(tenant, null));
			}
		} catch (Exception t) {
			throw new SpagoBIServiceException("An unexpected error occured while istantiating the dao", t);
		}
		return Response.ok(tenantJSON).build();
	}

}
