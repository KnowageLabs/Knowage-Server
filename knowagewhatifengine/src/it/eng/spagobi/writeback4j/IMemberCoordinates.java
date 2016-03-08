/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
