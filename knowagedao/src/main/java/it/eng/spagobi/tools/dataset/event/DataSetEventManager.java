/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
