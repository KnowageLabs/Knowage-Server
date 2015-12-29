/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 * 
 */
package it.eng.spagobi.engines.whatif.member;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
