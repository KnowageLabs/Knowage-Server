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
package it.eng.qbe.query.catalogue;

import it.eng.qbe.query.Query;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * The Class SingleDataMartWizardObjectSourceBeanImpl.
 */
public class QueryCatalogue {
	
	Map queries;
	String firstQueryId;
	
	private long counter;
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QueryCatalogue.class);
	
    
    public QueryCatalogue() {
		this(0);
	}
    
	public QueryCatalogue(long counterOffset) {
		this.queries = new HashMap();
		this.firstQueryId = null;
		this.counter = counterOffset;
		logger.debug("Query's id counter has been initialized to [" + this.counter + "]");
	}
	
	/*
	 * Externalize the id creation strategy is little bit to mutch for the moment. 
	 * If you want to modify this method keep in mind by the way that it have a dependence with 
	 * the statement class.Infact the id is used as a prefix to all entitiy aliases so it must not
	 * broke the valid alias syntax (avoid spaces and special char) 
	 * 
	 * @todo id generation must be consistent also across different execution. Add an initial offset
	 * to setup properly the counter in order to not override preloaded queries.
	 */
	public String getNextValidId() {
		counter += 1;
		logger.debug("Query's id counter has been incremented. Counter is now equals to [" + counter + "]");
		return "q" + (counter);
	}
	
	public boolean isValidId(String id) {
		return Pattern.matches("q[0-9]+", id);
	}
	
	private void updateCounter(String id) {
		String queryIdNumPart = id.substring(1);
		long queryIdNum = Long.parseLong( queryIdNumPart );
		if(counter < queryIdNum) {
			counter = queryIdNum;
			logger.debug("Catalogue id internal counter has benn updated to [" + counter + "]");
		}
	}
	
	public Set getIds() {
		return new HashSet(queries.keySet());
	}
	
	public boolean containsQuery(String id) {
		return this.queries.containsKey(id);
	}
	
	public Query getQuery(String id) {
		return (Query)this.queries.get(id);
	}
	
	public Set getQueryByName(String name) {
		return getQueryByName(name, false);
	}
	public Set getQueryByName(String name, boolean includeSubqueries) {
		Set results = null;
		
		Set q = getAllQueries(includeSubqueries);
		
		results = new HashSet();
		Iterator it = q.iterator();
		while(it.hasNext()) {
			String queryId = (String)it.next();
			Query query = (Query)queries.get(queryId);
			if(query.getName().equals(name)) {
				results.add(query);
			}
		}
		
		return results;
	}
	
	public Set getAllQueries() {
		return getAllQueries(false);
	}
	public Set getAllQueries(boolean includeSubqueries) {
		Set results = null;
		
		if(includeSubqueries) {
			results = queries.entrySet();
		} else {
			results = new HashSet();
			Iterator it = queries.keySet().iterator();
			while(it.hasNext()) {
				String queryId = (String)it.next();
				Query query = (Query)queries.get(queryId);
				if(!query.hasParentQuery()) {
					results.add(query);
				}
			}
		}
		
		return results;
	}
	
	
	

	public Query getFirstQuery() {
		/*
		Query query = (Query) getAllQueries(false).iterator().next();
		return query;
		*/
		//Query firstQuery;
		
		logger.debug("IN");
		/*
		firstQuery = null;
		if(firstQueryId != null) {
			firstQuery = (Query)queries.get(firstQueryId);
		} else {
			firstQuery = null;
		}
		*/
		Query query;
		Set<String> sortedSet = new TreeSet<String>( queries.keySet() ); 
	
		query = null;
		if(sortedSet.size() > 0) {
			String queryId = sortedSet.iterator().next();
			query = (Query)queries.get(queryId);
			//logger.debug("TEST METHOD:First query id is equal to [" + query.getId() + "; " + query.getName() +"]");
		}
		
		if(query != null){
		logger.debug("First query id is equal to [" + query.getId() + "; " + query.getName() +"]");
		}
		else{
			logger.error("query is null");
		}
		logger.debug("OUT");
		
		return query;
	}
	
	public String addQuery(Query query) {
		Iterator subqueriesIterator;
		Query subquery;
		
		logger.debug("IN");
		
		Assert.assertNotNull(query, "Is not possible to add a null query to the catalogue");
		
		try {
		
			if(query.getId() == null) {
				logger.debug("Query has not yet a valid id. A new id will be automaticcaly generated");
				query.setId( getNextValidId() );
				Assert.assertTrue(!queries.containsKey(query.getId()), "The new valid id generated is alredy present into the catalogue");
			} else {
				Assert.assertTrue(isValidId( query.getId() ), "Impossible to add query. Id [" + query.getId() + "] is not valid");
			}
			
			logger.debug("Query id is [" + query.getId() + "]");
			
			if(query.getName() == null) {
				logger.debug("Query has not yet a valid name. A new name will be automaticcaly generated");
				query.setName( "query-" + query.getId() );
			}
			logger.debug("Query name is [" + query.getName() + "]");
			
			if(query.getDescription() == null) {
				logger.debug("Query has not yet a valid name. A new name will be automaticcaly generated");
				query.setDescription( "query-" + query.getId() );
			}
			logger.debug("Query description is [" + query.getDescription() + "]");
			
			if(queries.containsKey(query.getId())){
				logger.debug("A query with id  equals to [" + query.getId() + "] already exist into the catalogue. The added one will update it");
			} else {
				logger.debug("A query with id  equals to [" + query.getId() + "] daoes not exist already the catalogue. It will be added");
			}
			
			queries.put( query.getId(), query);
			logger.debug("Query [" + query.getId() + "] added succesfully to the catalogue. Queries in the catalogue are now [" + queries.keySet().size() + "]");
			updateCounter(query.getId());
			
			// recursively add (or update) all subqueries to the catalogue
			logger.debug("Recursively adding all subqueries of query [" + query.getId() + "] to the catalogue ...");	
			logger.debug("Query [" + query.getId() + "] have [" + query.getSubqueryIds().size() + "] subqueries");	
			subqueriesIterator = query.getSubqueryIds().iterator();		
			while(subqueriesIterator.hasNext()) {
				String id = (String)subqueriesIterator.next();
				subquery = query.getSubquery(id);
				logger.debug("Adding subquery [" + subquery.getId() + "] of query [" + query.getId() + "]");	
				addQuery(subquery);
				logger.debug("Subquery [" + subquery.getId() + "] of query [" + query.getId() + "] has been added succesfully");
			}
			
			if(firstQueryId == null) { 
				logger.debug("First query of catalogue is [" + query.getId() + "; " + query.getName() +"]");
				firstQueryId = query.getId(); 
				logger.debug("First query set [" + firstQueryId + "]");
			}
		} finally {
			logger.debug("OUT");
		}
		
		return query.getId();
	}
	
	public Query removeQuery(String id) {
		Query query;
		Query parentQuery;
		Iterator subqueriesIterator;
		Query subquery;
		
		query = (Query)queries.remove(id);			
		removeSubqueries(query);
		
		if(query.hasParentQuery()) {
			parentQuery = query.getParentQuery();
			parentQuery.removeSubquery(query.getId());
		}
		
		return query;
	}
	
	private void removeSubqueries(Query query) {
		Iterator subqueriesIterator;
		Query subquery;
		
		// recursively remove all subqueries from the catalogue
		subqueriesIterator = query.getSubqueryIds().iterator();		
		while(subqueriesIterator.hasNext()) {
			String id = (String)subqueriesIterator.next();
			subquery = query.getSubquery(id);
			queries.remove(subquery.getId());
			removeSubqueries(subquery);
		}
	}

	
	
}
