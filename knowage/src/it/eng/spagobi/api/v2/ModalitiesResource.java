package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

@Path("/2.0/modalities")
@ManageAuthorization
public class ModalitiesResource extends AbstractSpagoBIResource {

	@GET
	// @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Check> getPredefined() {
		ICheckDAO checksDao = null;
		List<Check> fullList = null;

		try {

			checksDao = DAOFactory.getChecksDAO();
			checksDao.setUserProfile(getUserProfile());
			fullList = checksDao.loadPredefinedChecks();
			if (fullList != null && !fullList.isEmpty()) {
				return fullList;
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return new ArrayList<Check>();
	}
}
