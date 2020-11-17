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
package it.eng.spagobi.engines.whatif.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class FilterTreeBuilder {

	public static transient Logger logger = Logger.getLogger(FilterTreeBuilder.class);
	private Hierarchy hierarchy;
	private List<Member> treeMembers;
	private List<Member> visibleMembers;
	private boolean showSiblings;
	private int nodeLimit;

	/**
	 * @param hierarchy
	 * @param nodeLimit
	 * @param treeMembers
	 * @param visibleMembers
	 * @param showSiblings
	 */
	public FilterTreeBuilder() {
		this.nodeLimit = WhatIfEngineConfig.getInstance().getDepthLimit();
		nodeLimit = nodeLimit > 0 ? nodeLimit : Integer.MAX_VALUE;
	}

	/**
	 * @return the hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * @return the treeMembers
	 */
	public List<Member> getTreeMembers() {
		return treeMembers;
	}

	/**
	 * @return the visibleMembers
	 */
	public List<Member> getVisibleMembers() {
		return visibleMembers;
	}

	/**
	 * @return the showSiblings
	 */
	public boolean isShowSiblings() {
		return showSiblings;
	}

	/**
	 * @param hierarchy the hierarchy to set
	 */
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @param showSiblings the showSiblings to set
	 */
	public void setShowSiblings(boolean showSiblings) {
		this.showSiblings = showSiblings;
	}

	/**
	 * @param list the treeMembers to set
	 */
	public void setTreeLeaves(List<Member> list) {
		this.treeMembers = list;
	}

	/**
	 * @param visibleMembers the visibleMembers to set
	 */
	public void setVisibleMembers(List<Member> visibleMembers) {
		this.visibleMembers = visibleMembers;
	}

	/**
	 * @return
	 */
	public List<NodeFilter> build() {
		List<NodeFilter> nodes = new ArrayList<NodeFilter>();
		try {
			for (Member member : hierarchy.getRootMembers()) {

				nodes.add(new NodeFilter(member, nodeLimit, treeMembers, visibleMembers, showSiblings));
			}
		} catch (OlapException e) {
			logger.error("Error while creating filter tree", e);
			throw new SpagoBIRuntimeException("Error while creating filter tree", e);
		}

		return nodes;
	}

}
