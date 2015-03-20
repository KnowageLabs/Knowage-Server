/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.event;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

/**
 * 
 * This class notify an event every time a CRUD operation is called to a dataset
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class DataSetEventManager extends Observable {

	public static DataSetEventManager instance;
	private static transient Logger logger = Logger.getLogger(DataSetEventManager.class);
	
	public synchronized static DataSetEventManager getInstance() {
		try{
			if (instance == null)
					instance = new DataSetEventManager();
		}catch(Exception e) {
			logger.error("Impossible to create the dataset event manager",e);
		}
		return instance;
	}
	
	/**
	 * Register a listener
	 * @param listener
	 */
	public void addEventListener(Observer listener){
		instance.addObserver(listener);
		logger.debug("A new listenr has been bound to the DataSetEventManager: "+listener.getClass().getName());
	}
	
	public void notifyInsert(IDataSet dataSet){
		instance.setChanged();
		instance.notifyObservers(new DataSetEvent(DataSetEvent.INSERT, dataSet));
		logger.debug("Notify insert of the dataset with label "+dataSet.getLabel());
	}
	
	public void notifyChange(IDataSet dataSet){
		instance.setChanged();
		instance.notifyObservers(new DataSetEvent(DataSetEvent.CHANGE, dataSet));
		logger.debug("Notify change of the dataset with label "+dataSet.getLabel());
	}
	
	public void notifyRestoreVersion(IDataSet oldDataSet, IDataSet newDataSet){
		instance.setChanged();
		instance.notifyObservers(new DataSetEvent(DataSetEvent.CHANGE, oldDataSet, newDataSet));
		logger.debug("Notify restore of the dataset with label "+oldDataSet.getLabel());
	}
	
	public void notifyDelete(IDataSet dataSet){
		instance.setChanged();
		instance.notifyObservers(new DataSetEvent(DataSetEvent.DELETE, dataSet));
		logger.debug("Notify delete of the dataset with label "+dataSet.getLabel());
	}
	
	
	
}
