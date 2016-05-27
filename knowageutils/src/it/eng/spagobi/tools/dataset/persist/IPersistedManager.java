package it.eng.spagobi.tools.dataset.persist;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

public interface IPersistedManager {

	public void persistDataSet(IDataSet dataset) throws Exception;

}
