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
package it.eng.spagobi.engines.whatif.calculatedmember.mdxvisitor;

import org.olap4j.mdx.AxisNode;
import org.olap4j.mdx.CallNode;
import org.olap4j.mdx.CubeNode;
import org.olap4j.mdx.DimensionNode;
import org.olap4j.mdx.DrillThroughNode;
import org.olap4j.mdx.HierarchyNode;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.LevelNode;
import org.olap4j.mdx.LiteralNode;
import org.olap4j.mdx.MemberNode;
import org.olap4j.mdx.ParameterNode;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.PropertyValueNode;
import org.olap4j.mdx.SelectNode;
import org.olap4j.mdx.WithMemberNode;
import org.olap4j.mdx.WithSetNode;
import org.olap4j.metadata.Member;

/**
 * @author Dragan Pirkovic
 *
 */
public class UnionVisitor extends FunctionVisitor {

	/**
	 * @param member
	 * @param calculatedNode
	 * @param rootNode
	 */
	public UnionVisitor(Member member, IdentifierNode calculatedNode, CallNode rootNode) {
		super(member, calculatedNode, rootNode);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.SelectNode)
	 */
	@Override
	public ParseTreeNode visit(SelectNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.AxisNode)
	 */
	@Override
	public ParseTreeNode visit(AxisNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.WithMemberNode)
	 */
	@Override
	public ParseTreeNode visit(WithMemberNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.WithSetNode)
	 */
	@Override
	public ParseTreeNode visit(WithSetNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.CallNode)
	 */
	@Override
	public ParseTreeNode visit(CallNode callNode) {
		if (isChildrenNode(callNode) && containsMember(callNode, parentMember)) {
			((CallNode) rootNode.getArgList().get(0)).getArgList().add(calculatedNode);
		}
		return null;
	}

	/**
	 * @param callNode
	 * @param member
	 * @return
	 */
	private boolean containsMember(CallNode callNode, Member member) {
		return ((IdentifierNode) callNode.getArgList().get(0)).toString().equals(member.getUniqueName());
	}

	/**
	 * @param callNode
	 * @return
	 */
	private boolean isChildrenNode(CallNode callNode) {
		return callNode.getOperatorName().equalsIgnoreCase("Children");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.IdentifierNode)
	 */
	@Override
	public ParseTreeNode visit(IdentifierNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.ParameterNode)
	 */
	@Override
	public ParseTreeNode visit(ParameterNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.CubeNode)
	 */
	@Override
	public ParseTreeNode visit(CubeNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.DimensionNode)
	 */
	@Override
	public ParseTreeNode visit(DimensionNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.HierarchyNode)
	 */
	@Override
	public ParseTreeNode visit(HierarchyNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.LevelNode)
	 */
	@Override
	public ParseTreeNode visit(LevelNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.MemberNode)
	 */
	@Override
	public ParseTreeNode visit(MemberNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.LiteralNode)
	 */
	@Override
	public ParseTreeNode visit(LiteralNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.PropertyValueNode)
	 */
	@Override
	public ParseTreeNode visit(PropertyValueNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.olap4j.mdx.ParseTreeVisitor#visit(org.olap4j.mdx.DrillThroughNode)
	 */
	@Override
	public ParseTreeNode visit(DrillThroughNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
