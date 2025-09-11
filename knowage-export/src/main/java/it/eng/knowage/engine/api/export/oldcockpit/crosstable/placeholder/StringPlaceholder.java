/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021-present Engineering Ingegneria Informatica S.p.A.

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

package it.eng.knowage.engine.api.export.oldcockpit.crosstable.placeholder;


import it.eng.knowage.engine.api.export.oldcockpit.crosstable.CrossTab;

/**
 * Placeholder for string values.
 *
 * VERY IMPORTANT: Crosstable values should always be numbers, but we have some customers who have strings inside crosstab. This is due to old crosstables
 * created in old versions that have been ported into newer environments. This class is meant to manage ONLY these specific cases.
 *
 * @author Marco Balestri
 */
public class StringPlaceholder implements Placeholder {
	private String value;
	private CrossTab.MeasureInfo measureInfo;

	public StringPlaceholder(final String value, final CrossTab.MeasureInfo measureInfo) {
		this.value = value;
		this.measureInfo = measureInfo;
	}

	@Override
	public Double getValue() {
		return null;
	}

	@Override
	public CrossTab.MeasureInfo getMeasureInfo() {
		return measureInfo;
	}

	@Override
	public String toString() {
		return getValueAsString();
	}

	@Override
	public String getValueAsString() {
		return value;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean isAggregation() {
		return false;
	}

	@Override
	public CrossTab.CellType getCellType() {
		return CrossTab.CellType.DATA;
	}

}