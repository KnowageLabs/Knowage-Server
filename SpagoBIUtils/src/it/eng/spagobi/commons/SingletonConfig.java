/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;

/**
 * Defines the Singleton SpagoBI implementations.
 * 
 * @author Monia Spinelli
 */

public class SingletonConfig {

	private static SingletonConfig instance = null;
	private static transient Logger logger = Logger.getLogger(SingletonConfig.class);
	
	private ISingletonConfigCache cache=null;
	
	public synchronized static SingletonConfig getInstance() {
		try{
			if (instance == null)
					instance = new SingletonConfig();
		}catch(Exception e) {
			logger.debug("Impossible to load configuration",e);
		}
		return instance;
	}

	private SingletonConfig() throws Exception {
		logger.debug("IN");
		try {
			cache = (ISingletonConfigCache)Class.forName("it.eng.spagobi.commons.SingletonConfigCache").newInstance();
		} catch (Exception e) {
      logger.warn("Impossible to create it.eng.spagobi.commons.SingletonConfigCache",e);
			//throw new SpagoBIRuntimeException("Error creating SingletonConfigCache", e);
		}
	}

	/**
	 * Gets the config.
	 * 
	 * @return SourceBean contain the configuration
	 * 
	 * QUESTO METODO LO UTILIZZI PER LEGGERE LA CONFIGURAZIONE DEI SINGOLI ELEMENTI:
	 * ES:    String configurazione= SingletonConfig.getInstance().getConfigValue("home.banner");
	 */
	public synchronized String getConfigValue(String key) {
			return cache.get(key);

	}
	/**
	 * QUESTO METODO LO UTILIZZI ALL'INTERNO DEL SERVIZIO DI SALVATAGGIO CONFIGURAZIONE
	 * OGNI VOLTA CHE SALVIAMO UNA RIGA SVUOTIAMO LA CACHE
	 */
	public synchronized void clearCache() {
		try{
			instance= null;
		}catch(Exception e){
			logger.debug("Impossible to create a new istance", e);
		}
	}
}