package it.eng.spagobi.engines.datamining.common;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.compute.DataMiningPythonExecutor;
import it.eng.spagobi.engines.datamining.compute.DataMiningRExecutor;
import it.eng.spagobi.engines.datamining.compute.IDataMiningExecutor;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FunctionExecutor {

	public static List<DataMiningResult> executeFunction(DataMiningEngineInstance dataminingEngineInstance, UserProfile userProfile) {
		List<DataMiningCommand> commands = dataminingEngineInstance.getCommands();
		HashMap params = (HashMap) dataminingEngineInstance.getAnalyticalDrivers();
		IDataMiningExecutor executor = null;
		DataMiningResult result = null;
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();
		if (dataminingEngineInstance.getLanguage().equals("Python")) {
			executor = new DataMiningPythonExecutor(dataminingEngineInstance, userProfile);
		} else {
			executor = new DataMiningRExecutor(dataminingEngineInstance, userProfile);

		}

		for (DataMiningCommand c : commands) {
			List<Output> outputs = c.getOutputs();
			for (Output o : outputs) {
				try {
					result = executor.execute(params, c, o, userProfile, true, "function_catalog");
					results.add(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return results;
	}

}
