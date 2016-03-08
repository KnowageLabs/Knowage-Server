/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j.mondrian;

import it.eng.spagobi.writeback4j.IMemberCoordinates;
import it.eng.spagobi.writeback4j.sql.EquiJoin;
import it.eng.spagobi.writeback4j.sql.TableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.CubeDimension;
import mondrian.olap.MondrianDef.Hierarchy;

import org.olap4j.metadata.Member;

/**
 * @author Alberto Ghedin
 * 
 */
public class MondrianMemberCoordinates implements IMemberCoordinates {

	MondrianDef.CubeDimension dimension;
	MondrianDef.Hierarchy hieararchy;
	Map<TableEntry, Member> level2Member;
	List<TableEntry> levels;

	public MondrianMemberCoordinates(CubeDimension dimension, Hierarchy hieararchy, Map<TableEntry, Member> level2Member) {
		super();
		this.dimension = dimension;
		this.hieararchy = hieararchy;
		this.level2Member = level2Member;
	}

	public String getDimensionName() {
		return getDimension().name;
	}

	public MondrianDef.CubeDimension getDimension() {
		return dimension;
	}

	public void setDimension(MondrianDef.CubeDimension dimension) {
		this.dimension = dimension;
	}

	public MondrianDef.Hierarchy getHieararchy() {
		return hieararchy;
	}

	public void setHieararchy(MondrianDef.Hierarchy hieararchy) {
		this.hieararchy = hieararchy;
	}

	public Map<TableEntry, Member> getLevel2Member() {
		return level2Member;
	}

	public void setLevel2Member(Map<TableEntry, Member> level2Member) {
		this.level2Member = level2Member;
	}

	public boolean isAllMember() {
		return level2Member.size() == 0;
	}

	public String getTableName() {
		return MondrianSchemaRetriver.getTableName(getHieararchy());
	}

	public String getPrimaryKey() {
		return getHieararchy().primaryKey;
	}

	public String getForeignKey() {
		return getDimension().foreignKey;
	}

	public EquiJoin getInnerDimensionJoinConditions() {
		MondrianDef.RelationOrJoin relOrJoin = getHieararchy().relation;
		if (relOrJoin instanceof MondrianDef.Join) {
			MondrianDef.Join join = (MondrianDef.Join) relOrJoin;
			MondrianDef.Table leftT = (MondrianDef.Table) join.left;
			MondrianDef.Table rightT = (MondrianDef.Table) join.right;
			TableEntry leftTable = new TableEntry(join.leftKey, leftT.name);
			TableEntry rightTable = new TableEntry(join.rightKey, rightT.name);
			return new EquiJoin(leftTable, rightTable);
		}
		return null;
	}

	@Override
	public String toString() {
		return "MondrianMemberCoordinates [dimension=" + dimension.name
				+ ", hieararchy=" + hieararchy.name + "]";
	}

	public List<TableEntry> getLevels() {
		if (levels == null) {
			levels = new ArrayList<TableEntry>();
			MondrianDef.Level[] schemaLevels = hieararchy.levels;

			for (int i = 0; i < schemaLevels.length; i++) {
				MondrianDef.Level aLevel = schemaLevels[i];

				String table = aLevel.table;
				if (table == null) {
					table = MondrianSchemaRetriver.getTableName(hieararchy);
				}
				levels.add(new TableEntry(aLevel.column, table));

			}
		}
		return levels;
	}
}