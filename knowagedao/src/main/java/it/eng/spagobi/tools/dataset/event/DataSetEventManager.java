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

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 *
 * This class notify an event every time a CRUD operation is called to a dataset
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class DataSetEventManager extends Observable {

	private static final Logger LOGGER = Logger.getLogger(DataSetEventManager.class);
	private static final DataSetEventManager INSTANCE = new DataSetEventManager();

	public static synchronized DataSetEventManager getINSTANCE() {
		return INSTANCE;
	}

	/**
	 * Register a listener
	 * @param listener
	 */
	public void addEventListener(Observer listener){
		addObserver(listener);
		LOGGER.debug("A new listenr has been bound to the DataSetEventManager: "+listener.getClass().getName());
	}

	public void notifyInsert(IDataSet dataSet){
		setChanged();
		notifyObservers(new DataSetEvent(DataSetEvent.INSERT, dataSet));
		LOGGER.debug("Notify insert of the dataset with label "+dataSet.getLabel());
	}

	public void notifyChange(IDataSet dataSet){
		setChanged();
		notifyObservers(new DataSetEvent(DataSetEvent.CHANGE, dataSet));
		LOGGER.debug("Notify change of the dataset with label "+dataSet.getLabel());
	}

	public void notifyRestoreVersion(IDataSet oldDataSet, IDataSet newDataSet){
		setChanged();
		notifyObservers(new DataSetEvent(DataSetEvent.CHANGE, oldDataSet, newDataSet));
		LOGGER.debug("Notify restore of the dataset with label "+oldDataSet.getLabel());
	}

	public void notifyDelete(IDataSet dataSet){
		setChanged();
		notifyObservers(new DataSetEvent(DataSetEvent.DELETE, dataSet));
		LOGGER.debug("Notify delete of the dataset with label "+dataSet.getLabel());
	}

}
