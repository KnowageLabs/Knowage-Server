/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.calculatedmember;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.mdx.AxisNode;
import org.olap4j.mdx.CallNode;
import org.olap4j.mdx.DimensionNode;
import org.olap4j.mdx.HierarchyNode;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.mdx.LevelNode;
import org.olap4j.mdx.MemberNode;
import org.olap4j.mdx.NameSegment;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.PropertyValueNode;
import org.olap4j.mdx.SelectNode;
import org.olap4j.mdx.Syntax;
import org.olap4j.mdx.WithMemberNode;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.metadata.Member;

public class CalculatedMemberManager {
	public static transient Logger logger = Logger.getLogger(CalculatedMemberManager.class);

	public static String injectCalculatedFieldsIntoMdxQuery(SpagoBIPivotModel model) throws SpagoBIEngineException {
		String currentMdx = model.getCurrentMdx();
		List<CalculatedMember> calculatedFields = model.getCalculatedFields();
		if (calculatedFields != null) {
			for (int i = 0; i < calculatedFields.size(); i++) {
				boolean add = false;

				CalculatedMember aCalculatedField = calculatedFields.get(i);

				List<Member> members;
				try {
					Member mp = CubeUtilities.getMember(model.getCube(), aCalculatedField.getParentMember().getUniqueName());
					members = (List<Member>) mp.getChildMembers();
					for (int k = 0; k < members.size(); k++) {
						Member m = members.get(k);
						boolean calculatedField = m.isCalculated();
						boolean visibleMember = CubeUtilities.isMemberVisible(model, m, aCalculatedField.getParentMemberAxis());
						add = add || (!calculatedField && visibleMember);
					}
				} catch (OlapException e) {
					logger.error("");
				}

				if (add) {
					currentMdx = injectCalculatedIntoMdxQuery(currentMdx, model, calculatedFields.get(i));
				}
			}
		}
		return currentMdx;
	}

	/**
	 * Service to inject the calculated member in the tree
	 * 
	 * @param calculateFieldName
	 *            the name of the calculated member
	 * @param calculateFieldFormula
	 *            the formula
	 * @param parentMember
	 *            the parent member
	 * @param parentMemberAxis
	 *            the axis of the parent member
	 */
	public static String injectCalculatedIntoMdxQuery(String currentMdx, SpagoBIPivotModel model, CalculatedMember calculatedMember)
			throws SpagoBIEngineException {

		MdxParser p = createParser(model);
		SelectNode selectNode = p.parseSelect(currentMdx);
		IdentifierNode nodoCalcolato = new IdentifierNode(new NameSegment("Measures"), new NameSegment(calculatedMember.getCalculateFieldName()));
		ParseTreeNode expression = p.parseExpression(calculatedMember.getCalculateFieldFormula()); // parse
		// the
		// calculated
		// member
		// formula
		try {
			if (!calculatedMember.getParentMember().getDimension().getDimensionType().name().equalsIgnoreCase(new String("MEASURE")))
			{
				nodoCalcolato = new IdentifierNode(getParentSegments(calculatedMember.getParentMember(), calculatedMember.getCalculateFieldName()));// build
				// identifier
				// node
				// from
				// identifier
				// segments
			}
		} catch (OlapException olapEx) {
			throw new SpagoBIEngineException("Error building identifier node from segments for Measures", olapEx);
		}

		WithMemberNode withMemberNode = new WithMemberNode(null, nodoCalcolato, expression, Collections.<PropertyValueNode> emptyList());
		selectNode.getWithList().add(withMemberNode);

		ParseTreeNode tree = new CallNode(null, "()", Syntax.Parentheses, nodoCalcolato);

		ParseTreeNode row = selectNode.getAxisList().get(Axis.ROWS.axisOrdinal()).getExpression();
		ParseTreeNode column = selectNode.getAxisList().get(Axis.COLUMNS.axisOrdinal()).getExpression();

		selectNode.getAxisList().clear();

		if (calculatedMember.getParentMemberAxis().axisOrdinal() == (Axis.ROWS.axisOrdinal())) {
			selectNode.getAxisList().add(new AxisNode(null,
					false, Axis.COLUMNS,
					new ArrayList<IdentifierNode>(),
					new CallNode(null, "{}", Syntax.Braces, column)));

			insertCalculatedInParentNode(null, 0, row, tree, calculatedMember.getParentMember().getUniqueName());
			selectNode.getAxisList().add(new AxisNode(null,
					false, Axis.ROWS,
					new ArrayList<IdentifierNode>(),
					new CallNode(null, "{}", Syntax.Braces, row)));
		} else {

			insertCalculatedInParentNode(null, 0, column, tree, calculatedMember.getParentMember().getUniqueName());
			selectNode.getAxisList().add(new AxisNode(null,
					false, Axis.COLUMNS,
					new ArrayList<IdentifierNode>(),
					new CallNode(null, "{}", Syntax.Braces, column)));

			selectNode.getAxisList().add(new AxisNode(null,
					false, Axis.ROWS,
					new ArrayList<IdentifierNode>(),
					new CallNode(null, "{}", Syntax.Braces, row)));
		}

		return selectNode.toString();

		//
		//
		// try {
		// ei.getPivotModel().setMdx(queryString);
		// ei.getPivotModel().refresh();
		// } catch (Exception e) {
		// ei.getPivotModel().setMdx(currentMdx);
		// ei.getPivotModel().refresh();
		// throw new SpagoBIEngineException("Error calculating the field", e);
		// }

	}

	private static List<IdentifierSegment> getParentSegments(Member parentMember, String calculateFieldName) {

		List<IdentifierSegment> parentSegments = new ArrayList<IdentifierSegment>();

		String parentMemberUniqueName = parentMember.getUniqueName();

		StringTokenizer tokenizer = new StringTokenizer(parentMemberUniqueName, "[].");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			parentSegments.add(new NameSegment(token));
		}

		parentSegments.add(new NameSegment(calculateFieldName));
		return parentSegments;

	}

	private static String getIdentifierUniqueName(IdentifierNode node) {

		List<IdentifierSegment> parentSegments = node.getSegmentList();

		StringBuffer uniqueName = new StringBuffer();

		for (int i = 0; i < parentSegments.size(); i++) {
			uniqueName.append(parentSegments.get(i).toString());
			uniqueName.append(".");
		}
		if (uniqueName.length() > 0) {
			uniqueName.setLength(uniqueName.length() - 1);
		}

		return uniqueName.toString();

	}

	/**
	 * Service to find where to insert the calculated member in the tree
	 * 
	 * @param parentCallNode
	 * 
	 * @param positionInParentCallNode
	 * 
	 * @param parseNode
	 * 
	 * @param calculatedFieldTree
	 * 
	 * @param parentNodeUniqueName
	 * @return boolean true when the parent is found
	 */

	private static boolean insertCalculatedInParentNode(CallNode parentCallNode, int positionInParentCallNode, ParseTreeNode parseNode, ParseTreeNode calculatedFieldTree,
			String parentNodeUniqueName) {

		if (parseNode instanceof CallNode) {
			CallNode node = (CallNode) parseNode;
			List<ParseTreeNode> args = node.getArgList();
			for (int i = 0; i < args.size(); i++) {
				ParseTreeNode aNode = args.get(i);
				if (insertCalculatedInParentNode(node, i, aNode, calculatedFieldTree, parentNodeUniqueName)) {
					return true;
				}
			}
		} else if (parseNode instanceof DimensionNode) {

		} else if (parseNode instanceof HierarchyNode) {

		} else if (parseNode instanceof IdentifierNode) {
			IdentifierNode node = (IdentifierNode) parseNode;
			String name = getIdentifierUniqueName(node);
			if (parentNodeUniqueName.equals(name)) {
				parentCallNode.getArgList().add(positionInParentCallNode + 1, calculatedFieldTree);// The
				// new
				// calculated
				// member
				// goes
				// next
				// its
				// parent
				// node
				return true;
			}
		} else if (parseNode instanceof LevelNode) {

		} else if (parseNode instanceof MemberNode) {

		}
		return false;

	}

	/**
	 * Service to get an MDX Parser
	 * 
	 * @return The MDX Parser
	 */
	private static MdxParser createParser(SpagoBIPivotModel model) {
		OlapConnection olapConnection = model.getOlapConnection();
		return olapConnection.getParserFactory()
				.createMdxParser(olapConnection);
	}

}
