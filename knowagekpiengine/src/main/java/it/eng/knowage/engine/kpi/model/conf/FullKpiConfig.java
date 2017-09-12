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
package it.eng.knowage.engine.kpi.model.conf;

public class FullKpiConfig {

	/*
	 * - Velocity Model properties vmPath="/chart/templates/{name}/"
	 * vmName="{type}_chart.vm"
	 *
	 * - Library Initializer properties libIniPath="/chartlib/"
	 * libIniNAme="{name}Initializer.jspf"
	 */

	private final String type;
	private final String name;
	private final String vmPath;
	private final String vmName;
	private final String libIniPath;
	private final String libIniName;
	private final String enabeldInCockpit;

	public FullKpiConfig(String type, String name, String vmPath, String vmName, String libIniPath, String libIniName, String enabledInCockpit) {
		this.type = type;
		this.name = name;
		this.vmPath = isEmpty(vmPath) ? "/chart/templates/" + name + "/" : vmPath;
		this.vmName = isEmpty(vmName) ? type + "_chart.vm" : vmName;
		this.libIniPath = isEmpty(libIniPath) ? "chartlib/" : libIniPath;
		this.enabeldInCockpit = enabledInCockpit;

		/**
		 * Initializer files for two chart libraries used by the project is
		 * changed from .jspf to .jsp file (extension).
		 *
		 * @modifiedBy: danristo (danilo.ristovski@mht.net)
		 */
		this.libIniName = isEmpty(libIniName) ? name + "Initializer.jsp" : libIniName;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getVelocityModelPath() {
		return this.vmPath + this.vmName;
	}

	public String getLibraryInitializerPath() {
		return this.libIniPath + this.libIniName;
	}

	public String getLibIniName() {
		return libIniName;
	}

	private boolean isEmpty(String s) {
		return s == null || s.equals("");
	}

	public boolean isEnabledInCockpit() {
		return enabeldInCockpit.equals("true");
	}

}