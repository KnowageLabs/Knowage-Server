package it.eng.spagobi.federateddataset.rest;

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
			FederatedDataset federatedDataset = new FederatedDataset();
			ISbiFederatedDatasetDAO federatedDatasetDao = DAOFactory.getFedetatedDatasetDAO();
			
			FederatedDataset fdsNew = recoverFederatedDatasetDetails(requestBodyJSON);
						
			federatedDatasetDao.saveSbiFederatedDataSet(fdsNew);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "ok";
		
		
	}
	
	private FederatedDataset recoverFederatedDatasetDetails (JSONObject requestBodyJSON) {
		
		FederatedDataset fds = new FederatedDataset();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("ID_SBI_FEDERATED_DATA_SET");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		
		String label = (String) requestBodyJSON.opt("LABEL");
		String name = (String) requestBodyJSON.opt("NAME");
		String description = (String) requestBodyJSON.opt("DESCRIPTION");
		String relationships = requestBodyJSON.optJSONArray("RELATIONSHIPS").toString();
		
		fds.setId_sbi_federated_data_set(id.intValue());
		fds.setLabel(label);
		fds.setName(name);
		fds.setDescription(description);
		fds.setRelationships(relationships);

		return fds;
	}

	
}
