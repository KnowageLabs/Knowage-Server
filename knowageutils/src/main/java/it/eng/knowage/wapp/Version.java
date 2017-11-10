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

import java.lang.management.ManagementFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.eng.knowage.commons.utilities.TimeUtilities;

public final class Version {
	
	// <MAJOR>.<MINOR>.<PATCH>[-string]
	private String version = VersionInfo.COMPLETE_VERSION;
	// YYYY-MM-DD
	private String releaseDate = VersionInfo.RELEASE_DATE;
	// N d G h Z m T s P ms 
	private String uptime = TimeUtilities.getUptimeInDays(ManagementFactory.getRuntimeMXBean().getUptime());
	private String doc = VersionInfo.API_DOCUMENTATION;
	
	public Version() {
		
	}

	public String getVersion() {
		return version;
	}

	@JsonProperty("release_date")
	public String getReleaseDate() {
		return releaseDate;
	}

	public String getUptime() {
		return uptime;
	}

	public String getDoc() {
		return doc;
	}
}
