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

package it.eng.spagobi.writeback4j;

import it.eng.spagobi.writeback4j.sql.EquiJoin;
import it.eng.spagobi.writeback4j.sql.TableEntry;

import java.util.List;
import java.util.Map;

import org.olap4j.metadata.Member;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * @class IMemberCordinates The coordinates of a Member.. For coordinates we
 *        means the column and the table with the leafs for the member..
 */
public interface IMemberCoordinates {
	/**
	 * Is the member an all member?
	 * 
	 * @return true if the member is an all member
	 */
	public boolean isAllMember();

	/**
	 * Gets the table and column of the member and of all the ancestors
	 * 
	 * @return a map table/column -> member
	 */
	public Map<TableEntry, Member> getLevel2Member();

	/**
	 * Gets the name of the table linked to the fact
	 * 
	 * @return
	 */
	public String getTableName();

	/**
	 * Gets the name of the column linked to the fact
	 * 
	 * @return
	 */
	public String getPrimaryKey();

	/**
	 * Gets the name of the foreign key of the cube that links the fact table
	 * and the dimension that contains the cube
	 * 
	 * @return
	 */
	public String getForeignKey();

	/**
	 * Gets the inner joins.. If the hierarchy is the Join of more than one
	 * table, returns the join condition between tables
	 * 
	 * @return
	 */
	public EquiJoin getInnerDimensionJoinConditions();

	/**
	 * Gets a table entry for each level of the hierarchy
	 * 
	 * @return
	 */
	public List<TableEntry> getLevels();

	/**
	 * Gets the name of the dimension
	 * 
	 * @return
	 */
	public String getDimensionName();

}
