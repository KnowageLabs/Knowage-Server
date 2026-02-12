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
package it.eng.spago.dbaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spago.configuration.ConfigSingleton;
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
                if (_instance == null) {
					_instance = new Configurator();
				}
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
