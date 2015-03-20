/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import java.util.HashMap;

public class SessionBridge {
    
    private static HashMap data=new HashMap();
    private static SessionBridge instance=null;
    
    /**
     * Gets the single instance of SessionBridge.
     * 
     * @return single instance of SessionBridge
     */
    public static synchronized SessionBridge getInstance(){
		if (instance==null){
		    instance=new SessionBridge();
		}
		return instance;	    
    }
    
    /**
     * Put object.
     * 
     * @param key the key
     * @param obj the obj
     */
    public synchronized void putObject(String key,Object obj){
	data.put(key, obj);
    } 
    
    /**
     * Removes the object.
     * 
     * @param key the key
     * 
     * @return the object
     */
    public synchronized Object removeObject(String key){
	   Object tmp=data.get(key);
	   if (tmp!=null){
	       data.remove(key);
	   }
	   return tmp;
    }     

}
