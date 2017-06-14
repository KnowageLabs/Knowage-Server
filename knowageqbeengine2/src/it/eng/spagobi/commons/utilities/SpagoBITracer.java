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

import it.eng.spago.tracing.TracerSingleton;

/**
 * This class is for centralize the logging activities off all SpagoBI components, 
 * and to give a common format to logging message.
 * 
 * Internally the TracerSingleton class of spago is used for implementation
 *
 *  @author Zoppello
 */
public class SpagoBITracer {

	/**
	 * Comment for <code>SPAGO_BI</code>
	 */
	public static final String SPAGO_BI = "SpagoBI";
	    
	/**
	 * Given the input Strings for Module Name , Class Name, Method Name, returns an Output 
	 * String containing all them, sepatating the ones from the others by a "::" String.
	 * 
	 * @param moduleName The module Name String
	 * @param className	The Class Name String
	 * @param methodName The Method Name String 
	 * @return	An Output String unifiyng all the input Strings, separated by "::"
	 */
	private static String getPrefix(String moduleName, String className, String methodName){
		return moduleName + "::" + className + "::" + methodName;
	}
	
    /**
     * Adds a tracing information into the SpagoBI log file, with DEBUG importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void debug(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.DEBUG, getPrefix(moduleName, className, methodName) + message);
    }
    
    /**
     * Adds a tracing information into the SpagoBI log file, with INFORMATION importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void info(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.INFORMATION, getPrefix(moduleName, className, methodName) + message);
    }
    
    /**
     * Adds a tracing information into the SpagoBI log file, with WARNING importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void warning(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.WARNING, getPrefix(moduleName, className, methodName) + message);
    }
    
    /**
     * Adds a tracing information into the SpagoBI log file, with MINOR importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void minor(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.MINOR, getPrefix(moduleName, className, methodName) + message);
    }
    
    /**
     * Adds a tracing information into the SpagoBI log file, with MAJOR importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void major(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.MAJOR, getPrefix(moduleName, className, methodName) + message);
    }
    
    /**
     * Adds a tracing information into the SpagoBI log file, with CRITICAL importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     */
    public static void critical(String moduleName, String className, String methodName, String message){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.CRITICAL, getPrefix(moduleName, className, methodName) + message);
    }
    
    
    
   
    /**
     * Adds a tracing information into the SpagoBI log file, with DEBUG importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void debug(String moduleName, String className, String methodName, String message, Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.DEBUG, getPrefix(moduleName, className, methodName) + message, e);
    }
    
   
    /**
     * Adds a tracing information into the SpagoBI log file, with INFORMATION importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void info(String moduleName, String className, String methodName, String message, Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.INFORMATION, getPrefix(moduleName, className, methodName) + message, e);
    }
    
    
    /**
     * Adds a tracing information into the SpagoBI log file, with WARNING importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void warning(String moduleName, String className, String methodName, String message, Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.WARNING, getPrefix(moduleName, className, methodName) + message, e);
    }
    
   
    /**
     * Adds a tracing information into the SpagoBI log file, with MINOR importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void minor(String moduleName, String className, String methodName, String message,  Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.MINOR, getPrefix(moduleName, className, methodName) + message, e);
    }
    
    
    /**
     * Adds a tracing information into the SpagoBI log file, with MAJOR importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void major(String moduleName, String className, String methodName, String message, Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.MAJOR, getPrefix(moduleName, className, methodName) + message, e);
    }
    
   
    /**
     * Adds a tracing information into the SpagoBI log file, with CRITICAL importance (see
     * Spago Tracing docuentation for more details). it has as input all information needed
     * for tracing: the module into whom tracing has been call, those of the relative class
     * and method, the tracing message String.
     * It is a second implementation giving the possibility to take as input also an exception:
     * it is useful because many times tracing is called after having caught an exception.
     * 
     * @param moduleName The Module name String
     * @param className The Class name String
     * @param methodName The Method name String
     * @param message The Tracing message
     * @param e The occurred Exception
     */
    public static void critical(String moduleName, String className, String methodName, String message, Exception e){
    	TracerSingleton.log(SPAGO_BI, TracerSingleton.CRITICAL, getPrefix(moduleName, className, methodName) + message, e);
    }
    
}


