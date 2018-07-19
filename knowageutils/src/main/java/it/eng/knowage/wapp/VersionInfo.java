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

package it.eng.knowage.wapp;

public final class VersionInfo {

	public static final int MAJOR = 6;
	public static final int MINOR = 2;
	public static final int PATCH = 0;
	public static final String VERSION_SEPARATOR = ".";
	// this can be "-rc" "-snapshot" or any optional string
	public static final String OPTIONAL = "";

	public static final String COMPLETE_VERSION = VersionInfo.MAJOR + VersionInfo.VERSION_SEPARATOR + VersionInfo.MINOR + VersionInfo.VERSION_SEPARATOR
			+ VersionInfo.PATCH + VersionInfo.OPTIONAL;
	public static final String VERSION_FOR_EXPORT = VersionInfo.MAJOR + VersionInfo.VERSION_SEPARATOR + VersionInfo.MINOR;

	public static final String YEAR = "2018";
	public static final String MONTH = "07";
	public static final String DAY = "19";
	public static final String DATE_SEPARATOR = "/";

	public static final String RELEASE_DATE = VersionInfo.YEAR + VersionInfo.DATE_SEPARATOR + VersionInfo.MONTH + VersionInfo.DATE_SEPARATOR + VersionInfo.DAY;

	public static final String API_DOCUMENTATION = "http://docs.knowage.apiary.io/";

}
