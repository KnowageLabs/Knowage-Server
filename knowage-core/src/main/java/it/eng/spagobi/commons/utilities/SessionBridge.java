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
