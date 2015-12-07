package it.eng.spagobi.tools.dataset.listener;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

public class CometDExample  {

	/**
	 * Example of DataSet Listener and Comet service
	 * @param dataSet
	 * @param dataSetLabel
	 * @param profile
	 */
	public void subscribeDataSetListener(IDataSet dataSet, final String dataSetLabel, UserProfile profile) {
		DataSetListenerManager manager = DataSetListenerManagerFactory.getManager();
		//get the uuid from the profile
		final String uuid = profile.getUserUniqueIdentifier().toString();

		//add the listener (if it's not present) to check when the dataSet changes
		manager.addIDataSetListenerIfAbsent(uuid, dataSetLabel, new IDataSetListener() {

			public void dataStoreChanged(DataStoreChangedEvent event) throws DataSetListenerException {
				
				//notify the frontend clients about the dataStore changes
				CometServiceManager manager = CometServiceManagerFactory.getManager();
				manager.dataStoreChanged(uuid, dataSetLabel, event,"1");
			}

		}, "1");

	}
	
	/**
	 * Notify the changes of dataSet
	 * 
	 * @param dataSet
	 * @param dataSetLabel
	 * @param profile
	 */
	public void notifyChanges(IDataSet dataSet, final String dataSetLabel, UserProfile profile) {	
		DataSetListenerManager manager = DataSetListenerManagerFactory.getManager();
		//get the uuid from the profile
		final String uuid = profile.getUserUniqueIdentifier().toString();
		
		manager.changedDataSet(uuid, dataSetLabel, dataSet);
	}
	
	
}
