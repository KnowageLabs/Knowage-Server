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

package it.eng.spagobi.engines.whatif.calculatedmember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.mdx.NameSegment;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.PropertyValueNode;
import org.olap4j.mdx.SelectNode;
import org.olap4j.mdx.WithMemberNode;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import it.eng.spagobi.engines.whatif.calculatedmember.mdxvisitor.AxisVisitor;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

public class CalculatedMemberManager {
	public static transient Logger logger = Logger.getLogger(CalculatedMemberManager.class);

	public static String injectCalculatedFieldsIntoMdxQuery(SpagoBIPivotModel model) throws SpagoBIEngineException {
		String currentMdx = model.getCurrentMdx();

		for (CalculatedMember calculatedMember : model.getCalculatedFields()) {

			if (shouldAddCF(calculatedMember, model.getCellSet().getAxes())) {

				currentMdx = injectCalculatedIntoMdxQuery(currentMdx, model, calculatedMember);

			}

		}

		return currentMdx;
	}

	/**
	 * @param aCalculatedField
	 * @param axes
	 * @return
	 */
	private static boolean shouldAddCF(CalculatedMember aCalculatedField, List<CellSetAxis> axes) {
		for (CellSetAxis axis : axes) {

			if (isOnlyCFHierarchyOnAxis(aCalculatedField, axis.getAxisMetaData().getHierarchies())) {
				return true;
			}

		}
		return false;

	}

	/**
	 * @param aCalculatedField
	 * @param hierarchies
	 * @return
	 */
	private static boolean isOnlyCFHierarchyOnAxis(CalculatedMember aCalculatedField, List<Hierarchy> hierarchies) {
		return isOneHierarchyOnAxis(hierarchies) && isCFHierarchyOnAxis(aCalculatedField, hierarchies.get(0));
	}

	/**
	 * @param aCalculatedField
	 * @param hierarchy
	 * @return
	 */
	private static boolean isCFHierarchyOnAxis(CalculatedMember aCalculatedField, Hierarchy hierarchy) {
		return hierarchy.getUniqueName().equals(aCalculatedField.getHierarchyUniqueName());
	}

	/**
	 * @param hierarchies
	 * @return
	 */
	private static boolean isOneHierarchyOnAxis(List<Hierarchy> hierarchies) {
		return hierarchies.size() == 1;
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
			if (!calculatedMember.getParentMember().getDimension().getDimensionType().name().equalsIgnoreCase(new String("MEASURE"))) {
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

		WithMemberNode withMemberNode = new WithMemberNode(null, nodoCalcolato, expression, Collections.<PropertyValueNode>emptyList());
		selectNode.getWithList().add(withMemberNode);

		ParseTreeNode row = selectNode.getAxisList().get(Axis.ROWS.axisOrdinal()).getExpression();
		ParseTreeNode column = selectNode.getAxisList().get(Axis.COLUMNS.axisOrdinal()).getExpression();
		row.accept(new AxisVisitor(calculatedMember.getParentMember(), nodoCalcolato));
		column.accept(new AxisVisitor(calculatedMember.getParentMember(), nodoCalcolato));

		return selectNode.toString();

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

	/**
	 * Service to get an MDX Parser
	 *
	 * @return The MDX Parser
	 */
	private static MdxParser createParser(SpagoBIPivotModel model) {
		OlapConnection olapConnection = model.getOlapConnection();
		return olapConnection.getParserFactory().createMdxParser(olapConnection);
	}

}
