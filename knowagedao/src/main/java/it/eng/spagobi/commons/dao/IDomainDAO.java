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
package it.eng.spagobi.commons.dao;

import java.util.List;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines the interfaces for all methods needed to operate with a domain.
 */
public interface IDomainDAO extends ISpagoBIDao {
	/**
	 * Loads all possible domain that refer to a given domain type, storing each of them into a <code>Domain</objects> and after putting all objects into a list
	 * filtered by tenant, which is returned.
	 *
	 * @param domainType
	 *            The String identifying the domain type
	 *
	 * @return The list of all domains
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public List loadListDomainsByTypeAndTenant(String domainType) throws EMFUserError;

	/**
	 * Loads all possible domain that refer to a given domain type, storing each of them into a <code>Domain</objects> and after putting all objects into a
	 * list, which is returned.
	 *
	 * @param domainType
	 *            The String identifying the domain type
	 *
	 * @return The list of all domains
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public List<Domain> loadListDomainsByType(String domainType) throws EMFUserError;

	public List<Integer> loadListMetaModelDomainsByRole(Integer roleId) throws SpagoBIRuntimeException;

	/**
	 * Returns the domain identified by the input parameter <code>id</code>, storing it in a <code>Domain</code> object.
	 *
	 * @param id
	 *            The identifier domain id
	 *
	 * @return The <code>Domain</code> object storing the domain
	 *
	 * @throws EMFUserError
	 *             if an Exception occurs
	 */
	public Domain loadDomainById(Integer id) throws EMFUserError;

	/**
	 * Returns the domain identified by the input parameter <code>id</code>, storing it in a <code>Domain</code> object.
	 *
	 * @param id
	 *            The identifier domain id
	 *
	 * @return The <code>SbiDomains</code> object storing the domain
	 *
	 * @throws EMFUserError
	 *             if an Exception occurs
	 */
	public SbiDomains loadSbiDomainById(Integer id) throws EMFUserError;

	/**
	 * Returns the domain identified by the two input parameters <code>codeDomain</code> and <code>codeValue</code>, storing it in a <code>Domain</code> object.
	 *
	 * @param codeDomain
	 *            The identifier domain code
	 * @param codeValue
	 *            The identifier domain value code
	 *
	 * @return The <code>Domain</code> object storing the domain
	 *
	 * @throws EMFUserError
	 *             if an Exception occurs
	 */
	public Domain loadDomainByCodeAndValue(String codeDomain, String codeValue) throws EMFUserError;

	/**
	 * Same as loadDomainByCodeAndValue but with (optional) external session
	 *
	 * @param codeDomain
	 * @param codeValue
	 * @param session
	 * @return
	 * @throws EMFUserError
	 */
	public Domain loadDomainByCodeAndValue(String codeDomain, String codeValue, Session session) throws EMFUserError;

	/**
	 * Returns the domain identified by the two input parameters <code>codeDomain</code> and <code>codeValue</code>, storing it in a <code>Domain</code> object.
	 *
	 * @param codeDomain
	 *            The identifier domain code
	 * @param codeValue
	 *            The identifier domain value code
	 *
	 * @return The <code>Domain</code> object storing the domain
	 *
	 * @throws EMFUserError
	 *             if an Exception occurs
	 */
	public SbiDomains loadSbiDomainByCodeAndValue(String codeDomain, String codeValue) throws EMFUserError;

	/**
	 * Loads all possible domain, storing each of them into a <code>Domain</objects> and after putting all objects into a list, which is returned.
	 *
	 * @return The list of all domains
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public List loadListDomains() throws EMFUserError;

	/**
	 * Save a domain
	 *
	 * @return Save domains
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void saveDomain(Domain d) throws EMFUserError;

	/**
	 * Save a domain using an optional session and return an id
	 *
	 * @param d
	 * @param session
	 * @return
	 * @throws EMFUserError
	 */
	public Integer saveDomain(Domain d, Session session) throws EMFUserError;

	/**
	 * Delete a domain
	 *
	 * @return Delete domains
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void delete(Integer idDomain) throws EMFUserError;

}