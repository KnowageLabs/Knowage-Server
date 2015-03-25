/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.drivers;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import java.util.Locale;
import java.util.Map;



/**
 * Defines the methods implements by the SpagoBI drivers that, 
 * starting from a SpagoBI BIOBject, produce the parameters for a 
 * specific engine to which they are associated. The names anv values of the map parameters 
 * will be used by the system to produce a POST request to the engine application.
 * Each driver can extract and trasform the BIParameter of the BIObject in order to create a 
 * a right request based on the engine specificaion.
 * The methods can be used also to do some setting operation like for example handshake 
 * security requests.    
 */
public interface IEngineDriver {

    
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param biobject the biobject
	 * 
	 * @return Map The map of the execution call parameters
	 */
    public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName);
	
    /**
     * Returns a map of parameters which will be send in the request to the
     * engine application.
     * 
     * @param subObject SubObject to execute
     * @param profile Profile of the user
     * @param roleName the name of the execution role
     * @param object the object
     * 
     * @return Map The map of the execution call parameters
     */
    public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName);  
    
    /**
     * Returns the EngineURL for the creation of a new template for the document.
     * 
     * @param biobject the biobject
     * @param profile the profile
     * 
     * @return the EngineURL for the creation of a new template for the document
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest;
    
    /**
     * Returns the EngineURL for the modification of the document template.
     * 
     * @param biobject the biobject
     * @param profile the profile
     * 
     * @return the EngineURL for the modification of the document template
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest;
    
    /**
     * Returns the template elaborated.
     * 
     * @param byte[] the template
     * @param profile the profile
     * 
     * @return the byte[] with the modification of the document template
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public byte[] ElaborateTemplate(byte[] template) throws InvalidOperationRequest;
    
    /**
     * Returns the template elaborated.
     * 
     * @param byte[] the template
     * @param profile the profile
     * 
     * @return the byte[] with the modification of the document template
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public void applyLocale (Locale locale) ;
    
    
}
