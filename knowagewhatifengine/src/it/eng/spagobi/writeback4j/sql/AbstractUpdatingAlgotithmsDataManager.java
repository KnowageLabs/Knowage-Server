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

package it.eng.spagobi.writeback4j.sql;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.IMemberCoordinates;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Member;

public abstract class AbstractUpdatingAlgotithmsDataManager extends AbstractSqlSchemaManager {

	protected boolean useInClause = true;

	public String executeUpdate(Member[] members, double prop, Connection connection, Integer version) throws Exception {
		// list of the coordinates for the members
		List<IMemberCoordinates> memberCordinates = new ArrayList<IMemberCoordinates>();

		// init the query with the update set statement
		StringBuffer query = new StringBuffer();

		// gets the measures and the coordinates of the dimension members
		for (int i = 0; i < members.length; i++) {
			Member aMember = members[i];

			try {
				if (!(aMember.getDimension().getDimensionType().equals(Type.MEASURE))) {
					memberCordinates.add(retriver.getMemberCordinates(aMember));
				} else {
					buildUpdate(query, aMember, prop);
				}
			} catch (OlapException e) {
				logger.error("Error loading the type of the dimension of the member " + aMember.getUniqueName(), e);
				throw new SpagoBIEngineException("Error loading the type of the dimension of the member " + aMember.getUniqueName(), e);
			}
		}

		String whereClause = buildFromAndWhereClauseForCell(members, version, useInClause);
		query.append(whereClause);

		String queryString = query.toString();

		SqlUpdateStatement updateStatement = new SqlUpdateStatement(queryString);
		updateStatement.executeStatement(connection);

		return queryString.toString();

	}

	protected abstract void buildUpdate(StringBuffer buffer, Member measure, Object... values) throws SpagoBIEngineException;
}
