/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 *
 * Utilities class that provides some usefull method to access the informations of the cube
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
package it.eng.spagobi.engines.whatif.cube;

import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.pivot4j.mdx.MdxQueryExecutor;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.writeback4j.SbiAliases;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;

public class CubeUtilities {

	public static final String PATH_DELIM = "[";
	public static transient Logger logger = Logger.getLogger(CubeUtilities.class);

	/**
	 * Looks for the member with id memberUniqueName in the cube
	 *
	 * @param cube
	 *            the cube wherein to find the member
	 * @param memberUniqueName
	 *            the member to find
	 * @return the olap Member found.. null otherwise
	 * @throws OlapException
	 */
	public static Member getMember(Cube cube, String memberUniqueName) throws OlapException {
		Hierarchy hierarchy = null;
		NamedList<Hierarchy> hierarchies = cube.getHierarchies();
		String t = memberUniqueName.substring(1, memberUniqueName.indexOf("]"));
		for (int i = 0; i < hierarchies.size(); i++) {
			String hName = hierarchies.get(i).getName();
			if (hName.equals(t)) {
				hierarchy = hierarchies.get(i);
				break;
			}
		}

		return getMember(hierarchy.getLevels().get(0).getMembers(), memberUniqueName);
	}

	/**
	 * Looks for the member with id memberUniqueName in the hierarchy
	 *
	 * @param hierarchy
	 *            the hierarchy wherein to find the member
	 * @param memberUniqueName
	 *            the member to find
	 * @return the olap Member found.. null otherwise
	 * @throws OlapException
	 */
	public static Member getMember(Hierarchy hierarchy, String memberUniqueName) throws OlapException {
		return getMember(hierarchy.getLevels().get(0).getMembers(), memberUniqueName);
	}

	/**
	 * Check if the member is the root
	 *
	 * @param memberUniqueName
	 *            the name of the member
	 * @return true if the member is the root
	 * @throws OlapException
	 */
	public static boolean isRoot(String memberUniqueName) throws OlapException {
		return memberUniqueName == null || memberUniqueName.substring(1).indexOf(PATH_DELIM) == -1;
	}

	public static Member getMember(List<Member> members, String memberUniqueName) throws OlapException {
		for (int i = 0; i < members.size(); i++) {
			Member m = members.get(i);
			if (m.getUniqueName().equals(memberUniqueName)) {
				return m;
			} else if (memberUniqueName.contains(m.getUniqueName()) && memberUniqueName.indexOf(m.getUniqueName()) == 0) {
				return getMember((List<Member>) m.getChildMembers(), memberUniqueName);
			}
		}
		// all member
		if (members.size() == 1) {
			return getMember((List<Member>) members.get(0).getChildMembers(), memberUniqueName);
		}
		return null;
	}

	/**
	 * Retrive a position from the position unique name
	 *
	 * @param positions
	 *            the list of position
	 * @param positionUniqueName
	 *            the position unique name
	 * @return
	 * @throws OlapException
	 */
	public static Position getPosition(List<Position> positions, String positionUniqueName) {
		logger.debug("IN");
		positionUniqueName = positionUniqueName.replace(" ", "");
		for (int i = 0; i < positions.size(); i++) {
			Position p = positions.get(i);
			String member = p.getMembers().toString();
			member = member.replace(" ", "");
			if (member.equals(positionUniqueName)){
				logger.debug("OUT: fund a member "+member);
				return p;
			}
		}
		logger.debug("OUT: ");
		return null;
	}

	/**
	 * Searches in the cube for the hierarchy
	 *
	 * @param cube
	 *            the cube
	 * @param hierarchyUniqueName
	 *            the unique name of the hierarchy to search
	 * @return
	 * @throws OlapException
	 */
	public static Hierarchy getHierarchy(Cube cube, String hierarchyUniqueName) throws OlapException {
		Hierarchy hierarchy = null;
		NamedList<Hierarchy> hierarchies = cube.getHierarchies();
		for (int i = 0; i < hierarchies.size(); i++) {
			String hName = hierarchies.get(i).getUniqueName();
			if (hName.equals(hierarchyUniqueName)) {
				hierarchy = hierarchies.get(i);
				break;
			}
		}
		return hierarchy;
	}

	public static List<Dimension> getDimensions(List<Hierarchy> hierarchies) {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		if (hierarchies != null) {
			for (int i = 0; i < hierarchies.size(); i++) {
				Hierarchy aHierarchy = hierarchies.get(i);
				Dimension aDimension = aHierarchy.getDimension();
				if (!dimensions.contains(aDimension)) {
					dimensions.add(aDimension);
				}
			}
		}

		return dimensions;

	}

	/**
	 * Return the axis for the position
	 *
	 * @param axis
	 * @return
	 */
	public static Axis getAxis(int axis) {
		if (axis == Axis.COLUMNS.axisOrdinal()) {
			return Axis.COLUMNS;
		}
		if (axis == Axis.ROWS.axisOrdinal()) {
			return Axis.ROWS;
		}
		return Axis.FILTER;

	}

	public static Hierarchy getVersionHierarchy(Cube cube, ModelConfig modelConfig) {
		List<Dimension> dimensions = cube.getDimensions();
		Dimension versionDimension = null;
		for (java.util.Iterator<Dimension> iterator = dimensions.iterator(); iterator.hasNext();) {
			Dimension dimension = iterator.next();
			if (dimension.getUniqueName().equals(WhatIfConstants.VERSION_DIMENSION_UNIQUENAME)) {
				versionDimension = dimension;
			}
			;
		}
		if (versionDimension == null) {
			logger.error("Could not find version dimension");
			throw new SpagoBIEngineRuntimeException("Could not find version dimension");
		}
		logger.debug("Found dimension " + versionDimension.getUniqueName());

		// get Hierarchy Used by dimension version
		NamedList<Hierarchy> hierarchies = versionDimension.getHierarchies();
		Hierarchy hierarchy = null;
		if (hierarchies == null || hierarchies.size() == 0) {
			logger.error("Could not find hierarchies for version dimension");
			throw new SpagoBIEngineRuntimeException("Could not find hierarchies for version dimension");
		} else if (hierarchies.size() == 1) {
			hierarchy = hierarchies.get(0);
		} else {
			String hierarchyUsed = modelConfig.getDimensionHierarchyMap().get(WhatIfConstants.VERSION_DIMENSION_UNIQUENAME);
			hierarchy = hierarchies.get(hierarchyUsed);
		}

		return hierarchy;
	}

	/**
	 * Calculate the members value based on the passed expression
	 */
	public static Double getMemberValue(LinkedList membersExpression, SpagoBICellWrapper cellWrapper, PivotModel pivotModel, OlapDataSource olapDataSource,
			Map<String, String> dimensionHierarchyMap, SbiAliases aliases) {
		Double toReturn = null;

		// Members are the dimensional "coordinates" that identify the specific
		// value inserted in the cell
		Member[] cellMembersOriginal = cellWrapper.getMembers();
		Member[] cellMembers = new Member[cellMembersOriginal.length];
		System.arraycopy(cellMembersOriginal, 0, cellMembers, 0, cellMembersOriginal.length);

		// Iterate the list for each member specified
		for (Object memberExp : membersExpression) {
			String memberExpression = (String) memberExp;
			String[] memberExpressionParts;
			if (memberExpression.contains("[")) {
				// The member is using the notation with square brackets. Ex.
				// [Dimension].[MemberName]
				memberExpressionParts = splitSquareBracketNames(memberExpression);
			} else {
				memberExpressionParts = memberExpression.split("\\.");
			}

			boolean memberFound = searchMember(cellMembers, memberExpressionParts, dimensionHierarchyMap, aliases);

			if (!memberFound) {
				logger.error("ERROR: Cannot calculate Value, Member not found: " + memberExpression);
				throw new SpagoBIEngineRuntimeException("Cannot calculate Value, Member not found: " + memberExpression);
			}

		}

		// Calculate the new value
		MdxQueryExecutor mdxQueryExecutor = new MdxQueryExecutor(olapDataSource);
		Cube cube = pivotModel.getCube();
		SpagoBIPivotModel spagoBIPivotModel = null;
		if (pivotModel instanceof SpagoBIPivotModel) {
			spagoBIPivotModel = (SpagoBIPivotModel) pivotModel;
		} else {
			logger.error("ERROR: Cannot calculate Member Value, PivotModel not of type SpagoBIPivotModel");
			throw new SpagoBIEngineRuntimeException("Cannot calculate Member Value, PivotModel not of type SpagoBIPivotModel");
		}
		Object value = mdxQueryExecutor.getValueForTuple(cellMembers, cube, spagoBIPivotModel);
		if (value instanceof Double) {
			toReturn = (Double) value;
		}
		return toReturn;
	}

	/*
	 * Search if the specified member(s) currently exists, retrieve the corresponding object(s) and insert it in the cellMembers array (with a substitution)
	 */
	private static boolean searchMember(Member[] cellMembers, String[] memberExpressionParts, Map<String, String> dimensionHierarchyMap, SbiAliases aliases) {
		boolean memberFound = false;
		String memberExpressionDimension = memberExpressionParts[0];
		boolean hierarchySpecified = false;
		boolean searchByUniqueName = false;

		// search for aliases
		if (aliases != null) {
			// Using an alias to refer to a dimension ?
			String dimensionOriginalName = aliases.getDimensionNameFromAlias(memberExpressionDimension);
			if (dimensionOriginalName != null) {
				// Use the original dimension name instead of the alias for the
				// next steps
				memberExpressionDimension = dimensionOriginalName;
			}
		}

		if (memberExpressionParts.length > 2) {
			String memberExpressionHierarchy = memberExpressionParts[1];

			// search for aliases
			if (aliases != null) {
				// Using an alias to refer to a Hierarchy ?
				String hierarchyOriginalName = aliases.getHierarchyNameFromAlias(memberExpressionHierarchy);
				if (hierarchyOriginalName != null) {
					// Use the original hierarchy name instead of the alias for
					// the next steps
					memberExpressionHierarchy = hierarchyOriginalName;
				}
			}

			// Notation with Hierarchy specified
			memberExpressionDimension = memberExpressionDimension + "." + memberExpressionHierarchy;
			hierarchySpecified = true;
		}

		// Hierarchy is not specified in the notation, I have to check if I need
		// to use the default one or another one
		if (!hierarchySpecified) {
			// check if the selected hierarchy, corresponding to the specified
			// dimension, isn't the default one
			String hiearchyName = dimensionHierarchyMap.get("[" + memberExpressionDimension + "]");
			if (hiearchyName != null) {
				hiearchyName = hiearchyName.replaceAll("\\[|\\]", "");
				memberExpressionDimension = hiearchyName;
			}
		}

		for (int i = 0; i < cellMembers.length; i++) {
			Member aMember = cellMembers[i];
			String memberUniqueName = aMember.getUniqueName();

			String uniqueNameParts[] = splitSquareBracketNames(memberUniqueName);
			String dimensionName = uniqueNameParts[0];
			// Search the member to modify first by dimensionName (first part of
			// the uniqueName)
			if (dimensionName.equalsIgnoreCase(memberExpressionDimension)) {

				// Compose the uniqueName of the member to search using
				// the prefix of the current member of the selected cell
				String memberToSearchUniqueName = "";

				// Just the Name of the member without the unique prefix
				String memberToSearchSimpleName = "";

				int endIndex = memberUniqueName.lastIndexOf(".");
				if (endIndex != -1) {
					memberToSearchUniqueName = memberUniqueName.substring(0, endIndex);
				}
				if (hierarchySpecified) {
					memberToSearchSimpleName = memberExpressionParts[2];
				} else {
					memberToSearchSimpleName = memberExpressionParts[1];
				}

				// the member name contains a specific level path ex:
				// [Product][Drink.Beverages]
				if (memberToSearchSimpleName.contains(".")) {
					// memberToSearchUniqueName = "["+ dimensionName +"]" + "."
					// + formatNameWithSquareBracket(memberToSearchSimpleName);
					memberToSearchUniqueName = generateUniqueName(uniqueNameParts, memberToSearchSimpleName);
					searchByUniqueName = true;
				} else {
					memberToSearchUniqueName = memberToSearchUniqueName + "." + "[" + memberToSearchSimpleName + "]";
				}

				// get Level of the interested member
				Level levelOfMember = aMember.getLevel();
				try {
					List<Member> matchingLevelMembers = new ArrayList<Member>();
					List<Member> levelMembers = levelOfMember.getMembers();
					for (Member levelMember : levelMembers) {
						if (searchByUniqueName) {
							String levelMemberName = levelMember.getUniqueName();

							if (levelMemberName.equalsIgnoreCase(memberToSearchUniqueName)) {
								matchingLevelMembers.add(levelMember);
								break;
							}
						} else {
							String levelMemberName = levelMember.getName();
							if (levelMemberName.equalsIgnoreCase(memberToSearchSimpleName)) {
								matchingLevelMembers.add(levelMember);
							}
						}
					}

					// Single member found with specified name, NO ambiguity
					if (matchingLevelMembers.size() == 1) {
						// Found the member specified in the expression, use it
						// to substitute
						// the original member in the cellMembers
						cellMembers[i] = matchingLevelMembers.get(0);
						memberFound = true;

					} else if (matchingLevelMembers.size() > 1) {
						// >1 members found (ambiguity)

						// try to resolve ambiguity with local precedence
						for (Member levelMember : matchingLevelMembers) {
							if (levelMember.getUniqueName().equalsIgnoreCase(memberToSearchUniqueName)) {
								cellMembers[i] = levelMember;
								memberFound = true;
								break;
							}
						}
						if (!memberFound) {
							logger.error("ERROR: Cannot calculate Value, Member name not found: " + memberToSearchSimpleName);
							throw new SpagoBIEngineRuntimeException("Cannot calculate Member Value, Member name is ambiguous: " + memberToSearchSimpleName);
						}

					} else {
						// zero members found (wrong name)
						memberFound = false;
						logger.error("ERROR: Cannot calculate Value, Member name not found: " + memberToSearchSimpleName);
						throw new SpagoBIEngineRuntimeException("Cannot calculate Member Value, Member name not found: " + memberToSearchSimpleName);

					}

				} catch (OlapException e) {
					throw new SpagoBIEngineRuntimeException("Cannot calculate Member Value, OlapException: " + e.getMessage());
				}
				if (memberFound) {
					break;
				}

			}

		}
		return memberFound;
	}

	/*
	 * uniqueNameParts: parts of the unique name of the current cell selected memberToSearchSimpleName: specified level part in the member expression, ex:
	 * Drink.Dairy in the member name [Product].[Drink.Dairy]
	 */
	private static String generateUniqueName(String uniqueNameParts[], String memberToSearchSimpleName) {
		String[] uniqueNamesPartsCopy = new String[uniqueNameParts.length];
		System.arraycopy(uniqueNameParts, 0, uniqueNamesPartsCopy, 0, uniqueNameParts.length);
		String[] memberParts = memberToSearchSimpleName.split("\\.");
		int memberPartsLength = memberParts.length;

		int i = uniqueNamesPartsCopy.length - memberPartsLength;
		for (int c = 0; c < memberPartsLength; c++) {
			uniqueNamesPartsCopy[i] = memberParts[c];
			i++;
		}
		String toReturn = StringUtils.join(uniqueNamesPartsCopy, ".");
		toReturn = formatNameWithSquareBracket(toReturn);
		return toReturn;

	}

	private static String[] splitSquareBracketNames(String memberExpression) {
		ArrayList<String> memberExpressionParts = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(memberExpression, "[]", false);
		while (st.hasMoreTokens()) {
			String memberPart = st.nextToken();
			if (!memberPart.equals(".")) {
				memberExpressionParts.add(memberPart);
			}
		}

		return memberExpressionParts.toArray(new String[0]);
	}

	/*
	 * Transform a string separated with dot in a string with square brackets separated by dot Ex: Name.Level -> [Name].[Level]
	 */
	private static String formatNameWithSquareBracket(String name) {
		ArrayList<String> nameParts = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(name, ".", true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!token.equals(".")) {
				nameParts.add("[" + token + "]");
			} else {
				nameParts.add(token);
			}
		}
		StringBuffer formattedName = new StringBuffer();
		for (String namePart : nameParts) {
			formattedName.append(namePart);
		}

		return formattedName.toString();
	}

	public static long getLeafs(Member member) throws OlapException {
		NamedList list = member.getProperties();

		Long leafs = (Long) member.getPropertyValue(WhatIfConstants.MEMBER_PROPERTY_LEAF);
		if (leafs != null) {
			return leafs;
		} else {
			leafs = 0l;
		}

		if (member.getChildMemberCount() == 0) {
			return 1l;
		}

		NamedList children = member.getChildMembers();
		for (int i = 0; i < children.size(); i++) {
			Member aChild = (Member) children.get(i);
			leafs = leafs + getLeafs(aChild);
		}

		member.setProperty(WhatIfConstants.MEMBER_PROPERTY_LEAF, leafs);
		return leafs;
	}

	/**
	 * Checks if the member is visible in the cube
	 *
	 * @param model
	 *            the pivot model
	 * @param member
	 *            the member to check
	 * @param axis
	 *            the axis that contains the member
	 * @return true if the model is visible
	 */
	public static boolean isMemberVisible(PivotModel model, Member member, Axis axis) {

		List<Member> visibleMembers = null;

		PlaceMembersOnAxes pm = model.getTransform(PlaceMembersOnAxes.class);
		visibleMembers = pm.findVisibleMembers(axis);

		for (int i = 0; i < visibleMembers.size(); i++) {
			Member m = visibleMembers.get(i);
			if (m.equals(member)) {
				return true;
			}
		}
		return false;

	}

}
