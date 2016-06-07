package it.eng.spagobi.tools.alert.action;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.tools.alert.exception.AlertActionException;
import it.eng.spagobi.tools.alert.job.AbstractAlertAction;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExecuteETLAction extends AbstractAlertAction {

	private static Logger logger = Logger.getLogger(ExecuteETLAction.class);

	@Override
	public void executeAction(String jsonOptions, Map<String, String> externalParameters) throws AlertActionException {
		logger.info("Running ExecuteETLAction...");
		logger.debug("jsonOptions: " + jsonOptions);
		try {
			if (jsonOptions != null && !jsonOptions.isEmpty()) {
				JSONObject json = new JSONObject(new JSONObject(jsonOptions).getString("jsonActionParameters"));
				if (json.has("listDocIdSelected")) {
					JSONArray ids = json.getJSONArray("listDocIdSelected");
					for (int i = 0; i < ids.length(); i++) {
						Integer id = ids.getJSONObject(i).getInt("DOCUMENT_ID");
						logger.debug(">>> id=" + id);

						BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(id);

						// create the execution controller
						ExecutionController execCtrl = new ExecutionController();
						execCtrl.setBiObject(biobj);

						ExecutionProxy proxy = new ExecutionProxy();
						proxy.setBiObject(biobj);

						IEngUserProfile profile = UserProfile.createSchedulerUserProfile();

						proxy.exec(profile, "", null);
					}
				}
			}

		} catch (EMFUserError | JSONException e) {
			logger.error("Send mail failed", e);
			throw new AlertActionException("Executing ETL failed", e);
		}

	}
}