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
package it.eng.spagobi.workspace.bo;

import java.io.Serializable;
import java.util.Date;

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
public class FunctionsOrganizer implements Serializable {

	// Fields

	private Integer functId;
	private Integer parentFunct;
	private String code;
	private String name;
	private String descr;
	private String path;
	private Integer prog;
	private Date timeIn;
	private String userIn;

	// Constructors

	public FunctionsOrganizer() {
		super();
	}

	public FunctionsOrganizer(Integer functId, Integer parentFunct, String code, String name, String descr, String path, Integer prog, Date timeIn,
			String userIn) {
		super();
		this.functId = functId;
		this.parentFunct = parentFunct;
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.path = path;
		this.prog = prog;
		this.timeIn = timeIn;
		this.userIn = userIn;
	}

	// Getters and Setters

	public Date getTimeIn() {
		return timeIn;
	}

	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}

	public String getUserIn() {
		return userIn;
	}

	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	public Integer getFunctId() {
		return functId;
	}

	public void setFunctId(Integer functId) {
		this.functId = functId;
	}

	public Integer getParentFunct() {
		return parentFunct;
	}

	public void setParentFunct(Integer parentFunct) {
		this.parentFunct = parentFunct;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

}
