/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.olap4j.Axis;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.impl.IdentifierParser;
import org.olap4j.impl.Named;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Datatype;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;
import org.pivot4j.PivotException;

public class OlapUtils {

	private Cube cube;

	private MemberHierarchyCache memberHierarchyCache;

	/**
	 * @param cube
	 */
	public OlapUtils(Cube cube) {
		if (cube == null) {
			throw new NullArgumentException("cube");
		}

		this.cube = cube;
	}

	/**
	 * @return the cube
	 */
	public Cube getCube() {
		return cube;
	}

	/**
	 * @return the memberHierarchyCache
	 */
	public MemberHierarchyCache getMemberHierarchyCache() {
		return memberHierarchyCache;
	}

	/**
	 * @param memberHierarchyCache
	 *            the memberHierarchyCache to set
	 */
	public void setMemberHierarchyCache(MemberHierarchyCache memberHierarchyCache) {
		this.memberHierarchyCache = memberHierarchyCache;
	}

	/**
	 * @param identifier
	 * @return
	 */
	public Member lookupMember(String identifier) {
		return lookupMember(identifier, cube);
	}

	/**
	 * @param identifier
	 * @param cube
	 * @return
	 */
	public static Member lookupMember(String identifier, Cube cube) {
		try {
			return cube.lookupMember(IdentifierNode.parseIdentifier(identifier).getSegmentList());
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @param member
	 * @return
	 */
	public static boolean isVisible(Member member) {
		try {
			if (member.getDimension().getDimensionType() == Type.MEASURE) {
				return member.isVisible();
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		return true;
	}

	/**
	 * @param elem
	 * @param otherElem
	 * @return
	 */
	public static boolean equals(MetadataElement elem, MetadataElement otherElem) {
		if (elem == null) {
			return otherElem == null;
		} else if (otherElem == null) {
			return false;
		}

		String uniqueName = elem.getUniqueName();
		String otherUniqueName = otherElem.getUniqueName();

		return ObjectUtils.equals(uniqueName, otherUniqueName);
	}

	/**
	 * @param position
	 * @param otherPosition
	 * @return
	 */
	public static boolean equals(Position position, Position otherPosition) {
		return equals(position, otherPosition, -1);
	}

	/**
	 * @param position
	 * @param otherPosition
	 * @param memberIndex
	 * @return
	 */
	public static boolean equals(Position position, Position otherPosition, int memberIndex) {
		if (position == otherPosition) {
			return true;
		}

		if (position == null || otherPosition == null) {
			return false;
		}

		int size = position.getMembers().size();

		if (memberIndex < 0) {
			memberIndex = size;

			if (size != otherPosition.getMembers().size()) {
				return false;
			}
		}

		for (int i = 0; i < memberIndex; i++) {
			Member member = position.getMembers().get(i);
			Member lastMember = otherPosition.getMembers().get(i);

			if (!equals(member, lastMember)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param axis
	 * @param otherAxis
	 */
	public static boolean equals(Axis axis, Axis otherAxis) {
		if (axis == otherAxis) {
			return true;
		}

		if (axis == null || otherAxis == null) {
			return false;
		}

		return axis.axisOrdinal() == otherAxis.axisOrdinal();
	}

	/**
	 * @param member
	 * @return
	 */
	private Member getParentMember(Member member) {
		Member parent;

		if (memberHierarchyCache == null) {
			parent = member.getParentMember();
		} else {
			parent = memberHierarchyCache.getParentMember(member);
		}

		return parent;
	}

	/**
	 * @param member
	 * @return
	 */
	private boolean isRaggedMember(Member member) {
		if (member instanceof RaggedMemberWrapper) {
			return true;
		}

		Member parent = getParentMember(member);

		return member.getDepth() > 1 && parent == null;
	}

	/**
	 * @param member
	 * @return
	 */
	private boolean hasRaggedParent(Member member) {
		if (member instanceof RaggedMemberWrapper) {
			return true;
		}

		Member parent = member;

		while ((parent = getParentMember(parent)) != null) {
			if (isRaggedMember(parent)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param member
	 * @return
	 */
	public Member wrapRaggedIfNecessary(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		if (member instanceof RaggedMemberWrapper || (!isRaggedMember(member) && !hasRaggedParent(member))) {
			return member;
		}

		return new RaggedMemberWrapper(member, cube);
	}

	/**
	 * @param members
	 * @return
	 */
	public List<Member> wrapRaggedIfNecessary(List<Member> members) {
		if (members == null) {
			throw new NullArgumentException("members");
		}

		List<Member> wrappedMembers = new ArrayList<Member>(members.size());

		for (Member member : members) {
			wrappedMembers.add(wrapRaggedIfNecessary(member));
		}

		return wrappedMembers;
	}

	/**
	 * @param member
	 * @return
	 */
	public Member getTopLevelRaggedMember(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		Member topMember;

		if (member instanceof RaggedMemberWrapper) {
			topMember = ((RaggedMemberWrapper) member).getTopMember();
		} else {
			topMember = getParentMember(member);
		}

		return topMember;
	}

	/**
	 * @param member
	 * @return
	 */
	public Member getBaseRaggedMember(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		Member baseMember;

		if (member instanceof RaggedMemberWrapper) {
			baseMember = ((RaggedMemberWrapper) member).getBaseMember();
		} else {
			baseMember = member;
		}

		return baseMember;
	}

	/**
	 * @param property
	 * @param level
	 * @return
	 */
	public static Property wrapProperty(Property property, Level level) {
		if (property == null) {
			throw new NullArgumentException("property");
		}

		if (level == null) {
			throw new NullArgumentException("level");
		}

		if (property instanceof PropertyWrapper) {
			return property;
		}

		return new PropertyWrapper(property, level);
	}

	/**
	 * Check to see if an empty set expression is supported by the backend
	 * provider.
	 *
	 * See : http://jira.pentaho.com/browse/MONDRIAN-1597
	 *
	 * @param metadata
	 * @return
	 */
	public static boolean isEmptySetSupported(OlapDatabaseMetaData metadata) {
		try {
			String driverName = metadata.getDriverName().toLowerCase();
			if (driverName.contains("xmla") || driverName.contains("xml/a")) {
				return !metadata.getDatabaseProductName().toLowerCase().contains("mondrian");
			}
		} catch (SQLException e) {
			throw new PivotException(e);
		}

		return true;
	}

	static class RaggedMemberWrapper implements Member, Named {

		private String uniqueName;

		private List<Member> ancestors;

		private Member baseMember;

		private Member topMember;

		private NamedList<RaggedMemberWrapper> children;

		private Level level;

		/**
		 * @param member
		 * @param cube
		 */
		RaggedMemberWrapper(Member member, Cube cube) {
			this.baseMember = member;
			this.level = member.getLevel();
			this.ancestors = new LinkedList<Member>();

			List<IdentifierSegment> segments = Collections.unmodifiableList(IdentifierParser.parseIdentifier(member.getUniqueName()));
			List<IdentifierSegment> resolvableSegments = new LinkedList<IdentifierSegment>();

			int i = 0;
			int count = segments.size();

			List<Level> levels = member.getHierarchy().getLevels();

			for (IdentifierSegment segment : segments) {
				resolvableSegments.add(segment);

				Level currentLevel = levels.get(i);

				Member ancestor;

				try {
					ancestor = cube.lookupMember(resolvableSegments);

					if (ancestor == null && i == 0) {
						ancestor = member.getHierarchy().getDefaultMember();
					}
				} catch (OlapException e) {
					throw new PivotException(e);
				}

				if (i == 0 && ancestor == null) {
					throw new PivotException("Can't determine top level parent for the ragged member : " + member.getUniqueName());
				}

				i++;

				if (i >= count) {
					this.uniqueName = getUniqueName(resolvableSegments);
				} else if (ancestor == null) {
					String currentName = getUniqueName(segments.subList(0, i));

					ancestor = new RaggedMemberWrapper(currentName, currentLevel, new LinkedList<Member>(ancestors), baseMember, topMember);
					ancestors.add(0, ancestor);

					resolvableSegments.remove(segment);
				} else {
					ancestors.add(0, ancestor);

					this.topMember = ancestor;
				}
			}

			if (topMember == null) {
				throw new IllegalArgumentException("Unable to find a non-ragged ancestor of the specified member : " + member);
			}
		}

		RaggedMemberWrapper(String uniqueName, Level level, List<Member> ancestors, Member baseMember, Member topMember) {
			this.uniqueName = uniqueName;
			this.level = level;
			this.baseMember = baseMember;
			this.topMember = topMember;
			this.ancestors = ancestors;

			this.children = new NamedListImpl<RaggedMemberWrapper>();
		}

		private static String getUniqueName(List<IdentifierSegment> segments) {
			StringBuilder builder = new StringBuilder();

			boolean first = true;

			for (IdentifierSegment segment : segments) {
				if (first) {
					first = false;
				} else {
					builder.append(".");
				}

				builder.append(segment.toString());
			}

			return builder.toString();
		}

		/**
		 * @return the baseMember
		 */
		public Member getBaseMember() {
			return baseMember;
		}

		/**
		 * @return the topMember
		 */
		public Member getTopMember() {
			return topMember;
		}

		protected boolean isBaseMember() {
			return level.getDepth() == baseMember.getDepth();
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getName()
		 */
		public String getName() {
			if (isBaseMember()) {
				return baseMember.getName();
			}

			return "";
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getUniqueName()
		 */
		public String getUniqueName() {
			return uniqueName;
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getCaption()
		 */
		public String getCaption() {
			if (isBaseMember()) {
				return baseMember.getCaption();
			}

			return "";
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getDescription()
		 */

		public String getDescription() {
			if (isBaseMember()) {
				return baseMember.getDescription();
			}

			return "";
		}

		/**
		 * @see org.olap4j.metadata.Member#getChildMembers()
		 */

		public NamedList<? extends Member> getChildMembers() throws OlapException {
			if (isBaseMember()) {
				return baseMember.getChildMembers();
			}

			return children;
		}

		/**
		 * @see org.olap4j.metadata.Member#getChildMemberCount()
		 */

		public int getChildMemberCount() throws OlapException {
			if (isBaseMember()) {
				return baseMember.getChildMemberCount();
			}

			return 1;
		}

		/**
		 * @see org.olap4j.metadata.Member#getParentMember()
		 */

		public Member getParentMember() {
			if (ancestors.isEmpty()) {
				return null;
			} else {
				return ancestors.get(0);
			}
		}

		/**
		 * @see org.olap4j.metadata.Member#getAncestorMembers()
		 */
		public List<Member> getAncestorMembers() {
			return ancestors;
		}

		/**
		 * @see org.olap4j.metadata.Member#getLevel()
		 */

		public Level getLevel() {
			return level;
		}

		/**
		 * @see org.olap4j.metadata.Member#getHierarchy()
		 */

		public Hierarchy getHierarchy() {
			return baseMember.getHierarchy();
		}

		/**
		 * @see org.olap4j.metadata.Member#getDimension()
		 */

		public Dimension getDimension() {
			return baseMember.getDimension();
		}

		/**
		 * @see org.olap4j.metadata.Member#getMemberType()
		 */

		public Type getMemberType() {
			if (isBaseMember()) {
				return baseMember.getMemberType();
			}

			return Type.UNKNOWN;
		}

		/**
		 * @see org.olap4j.metadata.Member#isAll()
		 */

		public boolean isAll() {
			return false;
		}

		/**
		 * @see org.olap4j.metadata.Member#isChildOrEqualTo(org.olap4j.metadata.Member)
		 */

		public boolean isChildOrEqualTo(Member member) {
			if (isBaseMember()) {
				return baseMember.isChildOrEqualTo(member);
			}

			if (!(member instanceof RaggedMemberWrapper)) {
				return false;
			}

			RaggedMemberWrapper other = (RaggedMemberWrapper) member;

			return OlapUtils.equals(baseMember, other.baseMember) && level.getDepth() <= other.level.getDepth();
		}

		/**
		 * @see org.olap4j.metadata.Member#getExpression()
		 */

		public ParseTreeNode getExpression() {
			if (isBaseMember()) {
				return baseMember.getExpression();
			}

			return null;
		}

		/**
		 * @see org.olap4j.metadata.Member#isCalculated()
		 */

		public boolean isCalculated() {
			return false;
		}

		/**
		 * @see org.olap4j.metadata.Member#isCalculatedInQuery()
		 */

		public boolean isCalculatedInQuery() {
			return false;
		}

		public int getSolveOrder() {
			if (isBaseMember()) {
				return baseMember.getSolveOrder();
			}

			return 0;
		}

		/**
		 * @see org.olap4j.metadata.Member#getPropertyValue(org.olap4j.metadata.Property)
		 */

		public Object getPropertyValue(Property property) throws OlapException {
			if (isBaseMember()) {
				return baseMember.getPropertyValue(property);
			}

			return null;
		}

		/**
		 * @see org.olap4j.metadata.Member#getPropertyFormattedValue(org.olap4j.metadata.Property)
		 */

		public String getPropertyFormattedValue(Property property) throws OlapException {
			if (isBaseMember()) {
				return baseMember.getPropertyFormattedValue(property);
			}

			return null;
		}

		/**
		 * @see org.olap4j.metadata.Member#setProperty(org.olap4j.metadata.Property,
		 *      java.lang.Object)
		 */

		public void setProperty(Property property, Object value) throws OlapException {
			if (isBaseMember()) {
				baseMember.setProperty(property, value);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		/**
		 * @see org.olap4j.metadata.Member#getProperties()
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })

		public NamedList<Property> getProperties() {
			if (isBaseMember()) {
				return baseMember.getProperties();
			}

			return new NamedListImpl();
		}

		/**
		 * @see org.olap4j.metadata.Member#getOrdinal()
		 */

		public int getOrdinal() {
			if (isBaseMember()) {
				return baseMember.getOrdinal();
			}

			return 0;
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#isVisible()
		 */

		public boolean isVisible() {
			return baseMember.isVisible();
		}

		/**
		 * @see org.olap4j.metadata.Member#isHidden()
		 */

		public boolean isHidden() {
			return baseMember.isHidden();
		}

		/**
		 * @see org.olap4j.metadata.Member#getDepth()
		 */

		public int getDepth() {
			return level.getDepth();
		}

		/**
		 * @see org.olap4j.metadata.Member#getDataMember()
		 */

		public Member getDataMember() {
			return this;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(getUniqueName()).toHashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Member)) {
				return false;
			}

			Member other = (Member) obj;

			if (isBaseMember() && OlapUtils.equals(baseMember, other)) {
				return true;
			}

			return OlapUtils.equals(this, other);
		}

		/**
		 * @see java.lang.Object#toString()
		 */

		@Override
		public String toString() {
			return getUniqueName();
		}
	}

	static class PropertyWrapper implements Property {

		private Property property;

		private String uniqueName;

		PropertyWrapper(Property property, Level level) {
			this.property = property;
			this.uniqueName = level.getUniqueName() + "." + property.getUniqueName();
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getUniqueName()
		 */

		public String getUniqueName() {
			return uniqueName;
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getName()
		 */

		public String getName() {
			return property.getName();
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getCaption()
		 */

		public String getCaption() {
			return property.getCaption();
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#getDescription()
		 */

		public String getDescription() {
			return property.getDescription();
		}

		/**
		 * @see org.olap4j.metadata.MetadataElement#isVisible()
		 */

		public boolean isVisible() {
			return property.isVisible();
		}

		/**
		 * @see org.olap4j.metadata.Property#getContentType()
		 */

		public ContentType getContentType() {
			return property.getContentType();
		}

		/**
		 * @see org.olap4j.metadata.Property#getDatatype()
		 */

		public Datatype getDatatype() {
			return property.getDatatype();
		}

		/**
		 * @see org.olap4j.metadata.Property#getType()
		 */

		public Set<TypeFlag> getType() {
			return property.getType();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(getUniqueName()).toHashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PropertyWrapper)) {
				return false;
			}

			PropertyWrapper other = (PropertyWrapper) obj;

			return OlapUtils.equals(this, other);
		}

		/**
		 * @see java.lang.Object#toString()
		 */

		@Override
		public String toString() {
			return getUniqueName();
		}
	}
}
