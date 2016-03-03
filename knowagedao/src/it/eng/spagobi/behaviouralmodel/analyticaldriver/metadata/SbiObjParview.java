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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiObjParview extends SbiHibernateModel {


	private SbiObjParviewId id;
	private Integer prog;
	private String viewLabel;
	
	
	// Constructors

	/**
	 * default constructor.
	 */
	public SbiObjParview() {
	}

	/**
	 * constructor with id.
	 * 
	 * @param id the id
	 */
	public SbiObjParview(SbiObjParviewId id) {
		this.id = id;
	}
	
	public SbiObjParviewId getId() {
		return id;
	}
	public void setId(SbiObjParviewId id) {
		this.id = id;
	}
	public Integer getProg() {
		return prog;
	}
	public void setProg(Integer prog) {
		this.prog = prog;
	}
	public String getViewLabel() {
		return viewLabel;
	}
	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}

	


}
