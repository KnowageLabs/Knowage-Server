/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.graph.associativity.container;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.utilities.database.DataBaseException;
import org.apache.solr.client.solrj.SolrServerException;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAssociativeDatasetContainer {

	public IDataSet getDataSet();

	public boolean addFilter(SimpleFilter filter);

	public boolean update(EdgeGroup group, List<String> columnNames, Set<Tuple> tuples);

	public Set<EdgeGroup> getGroups();

	public Set<EdgeGroup> getUsedGroups();

	public boolean removeGroup(EdgeGroup group);

	public boolean addGroup(EdgeGroup group);

	public Set<EdgeGroup> getUnresolvedGroups();

	public boolean isResolved();

	public void resolve();

	public void unresolve();

	public void unresolveGroups();

	public Map<String, String> getParameters();

	public abstract Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException, DataBaseException, IOException, SolrServerException;

	public Set<Tuple> getTupleOfValues(String parameter);
}
