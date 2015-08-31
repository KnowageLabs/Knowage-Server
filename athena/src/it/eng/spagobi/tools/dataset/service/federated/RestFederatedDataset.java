package it.eng.spagobi.tools.dataset.service.federated;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.federateddataset.bo.FederatedDataset;
import it.eng.spagobi.federateddataset.dao.ISbiFederatedDatasetDAO;
import it.eng.spagobi.utilities.rest.RestUtilities;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.json.JSONObject;


@Path("/federateddataset")
public class RestFederatedDataset {
	
	@POST
	@Path("/post")
	public String createTrackInJSON(@Context HttpServletRequest req) {
		
		
		
		try {
			
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			ISbiFederatedDatasetDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();
			
			FederatedDataset fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);
						
			federatedDatasetDao.saveSbiFederatedDataSet(fdsNew);
			return "ok";
		} catch (Exception e) {
			
			e.printStackTrace();
			return "not ok";
		}
		
		
		
	}
	
	private FederatedDataset recoverFederatedDatasetDetails (JSONObject requestBodyJSON) {
		
		FederatedDataset fds = new FederatedDataset();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("ID_SBI_FEDERATED_DATA_SET");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		
		String label = (String) requestBodyJSON.opt("label");
		String name = (String) requestBodyJSON.opt("name");
		String description = (String) requestBodyJSON.opt("description");
		String relationships = requestBodyJSON.optJSONArray("relationships").toString();
		
		fds.setId_sbi_federated_data_set(id.intValue());
		fds.setLabel(label);
		fds.setName(name);
		fds.setDescription(description);
		fds.setRelationships(relationships);

		return fds;
	}

	
}
