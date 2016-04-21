package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;

import java.util.HashMap;

public interface IDataMiningExecutor {
	public DataMiningResult execute(HashMap params, DataMiningCommand command, Output output, IEngUserProfile userProfile, Boolean rerun, String documentLabel)
			throws Exception;
}
