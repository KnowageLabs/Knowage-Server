package it.eng.spagobi.federateddataset.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.federateddataset.bo.FederatedDataset;


public interface ISbiFederatedDatasetDAO extends ISpagoBIDao{
	
	public void saveSbiFederatedDataSet(FederatedDataset dataset);
	
	public void getSbiFederatedDataSet(Integer id);
	
}
