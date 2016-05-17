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
package it.eng.spagobi.engines.whatif.member;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class SbiMember {

	private String name;
	private String uniqueName;
	private String id;
	private String text;
	private boolean leaf;
	private boolean visible;
	private String qtip;

	public SbiMember() {
	};

	//
	// public SbiMember(Member member, boolean visible){
	// this(member, visible, "");
	// }

	public SbiMember(Member member, boolean visible, String description) {

		this.uniqueName = member.getUniqueName();
		this.id = member.getUniqueName();
		this.name = member.getCaption();
		this.text = calculateText(member.getName(), member.getCaption());

		try {
			this.leaf = member.getChildMemberCount() == 0;
		} catch (OlapException e) {
			throw new SpagoBIEngineRuntimeException("Error getting the childs count for the member " + member.getUniqueName(), e);
		}
		this.visible = visible;
		this.qtip = description;
	}

	public String getName() {
		return name;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	@JsonIgnore
	public Member getMember(Cube cube) throws OlapException {
		return CubeUtilities.getMember(cube, uniqueName);
	}

	public boolean isLeaf() {
		return leaf;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setChecked(Object o) {
	}

	private String calculateText(String name, String caption) {
		if (caption != null && !caption.equals(name)) {
			return name + "-" + caption;
		}
		return name;
	}

	public String getQtip() {
		return qtip;
	}

	public void setQtip(String qtip) {
		this.qtip = qtip;
	}

}
