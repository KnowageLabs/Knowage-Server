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

package it.eng.knowage.export.pdf;

/**
 * @authors Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class PageNumbering {

	public static final PageNumbering EXCLUDE_FIRST_AND_LAST = new PageNumbering(false, true, false);
	public static final PageNumbering ALL = EXCLUDE_FIRST_AND_LAST.includeFirst().includeLast();
	public static final PageNumbering INCLUDE_FIRST = EXCLUDE_FIRST_AND_LAST.includeFirst();
	public static final PageNumbering INCLUDE_LAST = EXCLUDE_FIRST_AND_LAST.includeLast();

	private final boolean firstIncluded;
	private final boolean othersIncluded;
	private final boolean lastIncluded;

	public PageNumbering(boolean firstIncluded, boolean othersIncluded, boolean lastIncluded) {
		this.firstIncluded = firstIncluded;
		this.othersIncluded = othersIncluded;
		this.lastIncluded = lastIncluded;
	}

	public boolean isFirstIncluded() {
		return firstIncluded;
	}

	public boolean isOthersIncluded() {
		return othersIncluded;
	}

	public boolean isLastIncluded() {
		return lastIncluded;
	}

	public PageNumbering includeFirst() {
		return new PageNumbering(true, othersIncluded, lastIncluded);
	}

	public PageNumbering includeOthers() {
		return new PageNumbering(firstIncluded, true, lastIncluded);
	}

	public PageNumbering includeLast() {
		return new PageNumbering(firstIncluded, othersIncluded, true);
	}
}
