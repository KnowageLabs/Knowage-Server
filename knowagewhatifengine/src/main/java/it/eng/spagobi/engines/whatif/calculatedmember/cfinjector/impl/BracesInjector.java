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
package it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.impl;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.mdx.CallNode;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Member.Type;
import org.olap4j.metadata.NamedList;

import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.AbstractInjector;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class BracesInjector extends AbstractInjector {

	public static transient Logger logger = Logger.getLogger(BracesInjector.class);

	/**
	 * @param callNode
	 */
	public BracesInjector(CallNode callNode) {
		super(callNode);
	}

	@Override

	public void injectField(Member parentMember, IdentifierNode calculatedNode) {

		try {
			if (contains(rootNode, parentMember) && isMeasureOrHasAnyChildren(parentMember)) {
				rootNode.getArgList().add(getCFPosition(parentMember.getChildMembers()), getCFNode(calculatedNode));

			}
		} catch (OlapException e) {
			logger.error("Error while injecting cf in braces", e);
			throw new SpagoBIEngineRuntimeException("Error while injecting cf in braces", e);
		}

	}

	/**
	 * @param parentMember
	 * @return
	 * @throws OlapException
	 */
	private boolean isMeasureOrHasAnyChildren(Member parentMember) throws OlapException {
		return isMeasure(parentMember) || containsAny(rootNode, parentMember.getChildMembers());
	}

	/**
	 * @param parentMember
	 * @return
	 */
	private boolean isMeasure(Member parentMember) {
		return parentMember.getMemberType() == Type.MEASURE;
	}

	/**
	 * @param childMembers
	 * @return
	 */
	private int getCFPosition(NamedList<? extends Member> childMembers) {

		for (int i = childMembers.size() - 1; i >= 0; i--) {
			Member member = childMembers.get(i);
			for (int j = rootNode.getArgList().size() - 1; j >= 0; j--) {
				if (rootNode.getArgList().get(j).toString().contains(member.getUniqueName())) {
					return j + 1;
				}
			}
		}
		return rootNode.getArgList().size();
	}

	private boolean containsAny(CallNode callNode, NamedList<? extends Member> members) {
		for (Member member : members) {
			if (contains(callNode, member)) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(CallNode callNode, Member member) {

		for (ParseTreeNode node : callNode.getArgList()) {

			if (node.toString().equals(member.getUniqueName())) {
				return true;
			}
		}
		return false;
	}

}
