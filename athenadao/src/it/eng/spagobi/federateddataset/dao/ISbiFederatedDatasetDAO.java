package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.federateddataset.bo.FederatedDataset;

import java.util.List;

public interface ISbiFederatedDatasetDAO extends ISpagoBIDao {

	public void saveSbiFederatedDataSet(FederatedDataset dataset);

	public void getSbiFederatedDataSet(Integer id);
 
	public List<FederatedDataset> loadAllFederatedDataSets() throws EMFUserError;

}
