/* SpagoBI, the Open Source Business Intelligence suite

* © 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spago.dbaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spago.base.Constants;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.commons.SingletonConfig;
 /**
* DATE            CONTRIBUTOR/DEVELOPER    NOTE
* 13-12-2004		  Butano           Eliminato il parser creato appositamente per il DB
*                                          Ora Configurator si appoggia al ConfigSingleton
* 										    
**/
/**
 * Questa Classe ha la responsabilità di effettuare il parser del file indicato dal Tag <DATA_ACCESS_CONFIGURATION_FILE_PATH> nel file xml principale dell'applicazione e di mettere
 * a disposizione del sosttosistema di accesso ai dati queste infomrazioni in particolare è possibile:
 * <li> Recuperare l'oggetto <B>ConnectionPoolDescriptor</B> dato un nome simbolico
 * <li> Recuperare la lista dei nomi dei pool registrati dall'applicativo
 *
 * @author Andrea Zoppello 
 * @version 1.0
 */
 
public class Configurator {
    private Configurator() {
    	ConfigSingleton config = ConfigSingleton.getInstance();
    	_parameterConnectionPoolDescriptors = new HashMap();
    	_parameterRegisteredConnectionPoolNames = new ArrayList();
    	SourceBean dataAccess = (SourceBean)config.getAttribute(DATA_ACCESS);
    	
    	List connectionPools = dataAccess.getAttributeAsList("CONNECTION-POOL");
    	ConnectionPoolDescriptor connectionPoolDescriptor = null;
    	SourceBean connectionPool = null;
    	List poolParameters = null;
    	String poolName = null;
    	if (connectionPools != null) {
	    	for (int i = 0; i < connectionPools.size(); i++) {
	    		connectionPoolDescriptor = new ConnectionPoolDescriptor();
	    		connectionPool = (SourceBean)connectionPools.get(i);  
	    		poolName = (String)connectionPool.getAttribute("connectionPoolName");
				connectionPoolDescriptor.setConnectionPoolName(poolName);
				connectionPoolDescriptor.setConnectionPoolFactory((String)connectionPool.getAttribute("connectionPoolFactoryClass"));
				poolParameters = connectionPool.getAttributeAsList("CONNECTION-POOL-PARAMETER");
				String parameterName = null;
				String parameterValue = null;
				if (poolParameters != null) {
					for (int j = 0; j < poolParameters.size(); j++) {				
						parameterName = (String)(((SourceBean)poolParameters.get(j)).getAttribute("parameterName"));
						parameterValue = (String)(((SourceBean)poolParameters.get(j)).getAttribute("parameterValue"));				
						ConnectionPoolParameter parameter = new ConnectionPoolParameter(parameterName, parameterValue);
						connectionPoolDescriptor.addConnectionPoolParameter(parameter);
					}	
				}
				_parameterConnectionPoolDescriptors.put(poolName, connectionPoolDescriptor);
	    	}
    	} 
    	else
    		TracerSingleton.log(
    	            Constants.NOME_MODULO,
    	            TracerSingleton.INFORMATION,
    	            "Configurator::Configurator: nessuna definizione di CONNECTION-POOL");
    	
		List registerPools= dataAccess.getAttributeAsList("CONNECTION-MANAGER.REGISTER-POOL");
    	SourceBean poolRegister = null;
    	if (registerPools != null) {
	    	for (int i = 0; i < registerPools.size(); i++) {
	    		poolRegister = (SourceBean)registerPools.get(i); 
	    		_parameterRegisteredConnectionPoolNames.add((String)poolRegister.getAttribute("registeredPoolName"));
	    	}
    	} 
    	else
    		TracerSingleton.log(
    	            Constants.NOME_MODULO,
    	            TracerSingleton.INFORMATION,
    	            "Configurator::Configurator: nessuna pool registrato nella busta CONNECTION-MANAGER");

    	// start modifications by Zerbetto on March 9th 2009
    	// date format and timestamp format is read from spagobi.xml instead of data_access.xml
//    	_dateFormat = (String)dataAccess.getAttribute("DATE-FORMAT.format");
//      _timeStampFormat = (String)dataAccess.getAttribute("TIMESTAMP-FORMAT.format");
    	
    	_dateFormat = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
        _timeStampFormat = SingletonConfig.getInstance().getConfigValue("SPAGOBI.TIMESTAMP-FORMAT.format"); 
        // end modifications by Zerbetto on March 9th 2009
    } // private Configurator()

    /**
    * The method for gettinng Instance of Configurator
    * @return the singleton unique istance of <B>Configurator</B>
    */
    public static Configurator getInstance() {
        if (_instance == null) {
            synchronized(Configurator.class) {
                if (_instance == null)
                    _instance = new Configurator();
            } // synchronized(Configurator.class)
        } // if (instance == null)
        return _instance;
    } // public static Configurator getInstance()

    /**
    * The method for getting the list of names of the pool registered in the data-access subsystem
    * @return a <B>List</B> of <B>String</B> representing the names of the pools registered
    */
    public List getRegisteredConnectionPoolNames() {
        return _parameterRegisteredConnectionPoolNames;
    } // public List getRegisteredConnectionPoolNames()

    /**
    * This method is used for get get a Pool Descriptor Object given the pool name
    * @return a <B>List</B> of <B>String</B> representing the names of the pools registered
    */
    public ConnectionPoolDescriptor getConnectionPoolDescriptor(String connectionPoolName) {
        return (ConnectionPoolDescriptor)_parameterConnectionPoolDescriptors.get(connectionPoolName);
    } // public ConnectionPoolDescriptor getConnectionPoolDescriptor(String connectionPoolName)

    /**
    * This method is used for retrieve the timeStamp format String
    * @return <B>String</B> representing the timeStamp format String
    */
    public synchronized String getTimeStampFormat() {
        return _timeStampFormat;
    } // public synchronized String getTimeStampFormatString(){
    /**
    * This method is used for retrieve the date format String
    * @return <B>String</B> representing the date format String
    */
    public synchronized String getDateFormat() {
        return _dateFormat;
    } // public synchronized String getDateFormatString()

    private static Configurator _instance = null;
    private static String DATA_ACCESS = "DATA-ACCESS";
    private Map _parameterConnectionPoolDescriptors;
    private List _parameterRegisteredConnectionPoolNames;   
    private String _timeStampFormat = null;
    private String _dateFormat = null;    
    //private ConfiguratorContentHandler _contentHandler = null;
} // public class Configurator
