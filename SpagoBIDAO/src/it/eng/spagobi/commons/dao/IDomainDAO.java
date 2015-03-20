/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 13-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.metadata.SbiDomains;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to operate with a domain.
 */
public interface IDomainDAO extends ISpagoBIDao{
	/**
	 * Loads all possible domain that refer to a given domain type, storing each
	 * of them into a <code>Domain</objects> and after putting all objects into
	 * a list filtered by tenant, which is returned.
	 * 
	 * @param domainType The String identifying the domain type
	 * 
	 * @return The list of all domains
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List loadListDomainsByTypeAndTenant(String domainType) throws EMFUserError;
	/**
	 * Loads all possible domain that refer to a given domain type, storing each
	 * of them into a <code>Domain</objects> and after putting all objects into
	 * a list, which is returned.
	 * 
	 * @param domainType The String identifying the domain type
	 * 
	 * @return The list of all domains
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List loadListDomainsByType(String domainType) throws EMFUserError;

	/**
	 * Returns the domain identified by the input parameter <code>id</code>,
	 * storing it in a <code>Domain</code> object.
	 * 
	 * @param id The identifier domain id
	 * 
	 * @return The <code>Domain</code> object storing the domain
	 * 
	 * @throws EMFUserError if an Exception occurs
	 */
	public  Domain loadDomainById(Integer id)
			throws EMFUserError;
	
	/**
	 * Returns the domain identified by the two input parameters <code>codeDomain</code>
	 * and <code>codeValue</code>, storing it in a <code>Domain</code> object.
	 * 
	 * @param codeDomain The identifier domain code
	 * @param codeValue The identifier domain value code
	 * 
	 * @return The <code>Domain</code> object storing the domain
	 * 
	 * @throws EMFUserError if an Exception occurs
	 */
	public  Domain loadDomainByCodeAndValue(String codeDomain, String codeValue)
			throws EMFUserError;
	
	/**
	 * Returns the domain identified by the two input parameters <code>codeDomain</code>
	 * and <code>codeValue</code>, storing it in a <code>Domain</code> object.
	 * 
	 * @param codeDomain The identifier domain code
	 * @param codeValue The identifier domain value code
	 * 
	 * @return The <code>Domain</code> object storing the domain
	 * 
	 * @throws EMFUserError if an Exception occurs
	 */
	public  SbiDomains loadSbiDomainByCodeAndValue(String codeDomain, String codeValue)
			throws EMFUserError;
	
	
	/**
	 * Loads all possible domain, storing each
	 * of them into a <code>Domain</objects> and after putting all objects into
	 * a list, which is returned.
	 * 
	 * @return The list of all domains
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List loadListDomains() throws EMFUserError;
	
	/**
	 * Save a domain
	 * 
	 * @return Save domains
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void saveDomain(Domain d)throws EMFUserError;

	
	/**
	 * Delete a domain
	 * 
	 * @return Delete domains
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void delete(Integer idDomain) throws EMFUserError;
	
}