/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package mondrian.olap4j;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.impl.AbstractNamedList;
import org.olap4j.impl.Named;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

import mondrian.olap.OlapElement;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapMeasure;
import mondrian.server.Locus;

/**
 * Implementation of {@link Member} for the Mondrian OLAP engine, as a wrapper around a mondrian {@link mondrian.olap.Member}.
 *
 * @author jhyde
 * @since May 25, 2007
 */
class MondrianOlap4jMember extends MondrianOlap4jMetadataElement implements Member, Named {

	final mondrian.olap.Member member;

	final MondrianOlap4jSchema olap4jSchema;

	MondrianOlap4jMember(MondrianOlap4jSchema olap4jSchema, mondrian.olap.Member mondrianMember) {
		assert mondrianMember != null;
		assert mondrianMember instanceof RolapMeasure == this instanceof MondrianOlap4jMeasure;
		this.olap4jSchema = olap4jSchema;
		this.member = mondrianMember;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MondrianOlap4jMember && member.equals(((MondrianOlap4jMember) obj).member);
	}

	@Override
	public int hashCode() {
		return member.hashCode();
	}

	@Override
	public String toString() {
		return getUniqueName();
	}

	@Override
	public NamedList<MondrianOlap4jMember> getChildMembers() throws OlapException {
		final RolapConnection conn = olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection.getMondrianConnection();
		final List<mondrian.olap.Member> children = Locus.execute(conn, "MondrianOlap4jMember.getChildMembers", new Locus.Action<List<mondrian.olap.Member>>() {
			@Override
			public List<mondrian.olap.Member> execute() {
				return conn.getSchemaReader().getMemberChildren(member);
			}
		});
		return new AbstractNamedList<MondrianOlap4jMember>() {
			@Override
			public String getName(Object member) {
				return ((MondrianOlap4jMember) member).getName();
			}

			@Override
			public MondrianOlap4jMember get(int index) {
				return new MondrianOlap4jMember(olap4jSchema, children.get(index));
			}

			@Override
			public int size() {
				return children.size();
			}
		};
	}

	@Override
	public int getChildMemberCount() throws OlapException {
		final RolapConnection conn = olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection.getMondrianConnection();
		return Locus.execute(conn, "MondrianOlap4jMember.getChildMemberCount", new Locus.Action<Integer>() {
			@Override
			public Integer execute() {
				return conn.getSchemaReader().getMemberChildren(member).size();
			}
		});
	}

	@Override
	public MondrianOlap4jMember getParentMember() {
		final mondrian.olap.Member parentMember = member.getParentMember();
		if (parentMember == null) {
			return null;
		}
		final RolapConnection conn = olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection.getMondrianConnection2();
		final boolean isVisible = Locus.execute(conn, "MondrianOlap4jMember.getParentMember", new Locus.Action<Boolean>() {
			@Override
			public Boolean execute() {
				return conn.getSchemaReader().isVisible(parentMember);
			}
		});
		if (!isVisible) {
			return null;
		}
		return new MondrianOlap4jMember(olap4jSchema, parentMember);
	}

	@Override
	public Level getLevel() {
		return new MondrianOlap4jLevel(olap4jSchema, member.getLevel());
	}

	@Override
	public Hierarchy getHierarchy() {
		return new MondrianOlap4jHierarchy(olap4jSchema, member.getHierarchy());
	}

	@Override
	public Dimension getDimension() {
		return new MondrianOlap4jDimension(olap4jSchema, member.getDimension());
	}

	@Override
	public Type getMemberType() {
		return Type.valueOf(member.getMemberType().name());
	}

	@Override
	public boolean isAll() {
		return member.isAll();
	}

	@Override
	public boolean isChildOrEqualTo(Member member) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCalculated() {
		return getMemberType() == Type.FORMULA;
	}

	@Override
	public int getSolveOrder() {
		return member.getSolveOrder();
	}

	@Override
	public ParseTreeNode getExpression() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Member> getAncestorMembers() {
		final List<Member> list = new ArrayList<Member>();
		MondrianOlap4jMember m = getParentMember();
		while (m != null) {
			list.add(m);
			m = m.getParentMember();
		}
		return list;
	}

	@Override
	public boolean isCalculatedInQuery() {
		return member.isCalculatedInQuery();
	}

	@Override
	public Object getPropertyValue(Property property) {
		return member.getPropertyValue(property.getName());
	}

	@Override
	public String getPropertyFormattedValue(Property property) {
		return member.getPropertyFormattedValue(property.getName());
	}

	@Override
	public void setProperty(Property property, Object value) throws OlapException {
		member.setProperty(property.getName(), value);
	}

	@Override
	public NamedList<Property> getProperties() {
		return getLevel().getProperties();
	}

	@Override
	public int getOrdinal() {
		final Number ordinal = (Number) member.getPropertyValue(Property.StandardMemberProperty.MEMBER_ORDINAL.getName());
		return ordinal.intValue();
	}

	@Override
	public boolean isHidden() {
		return member.isHidden();
	}

	@Override
	public int getDepth() {
		return member.getDepth();
	}

	@Override
	public Member getDataMember() {
		final mondrian.olap.Member dataMember = member.getDataMember();
		if (dataMember == null) {
			return null;
		}
		return new MondrianOlap4jMember(olap4jSchema, dataMember);
	}

	@Override
	public String getName() {
		return member.getName();
	}

	@Override
	public String getUniqueName() {
		return member.getUniqueName();
	}

	@Override
	public String getCaption() {
		return member.getCaption();
	}

	@Override
	public String getDescription() {
		return member.getDescription();
	}

	@Override
	public boolean isVisible() {
		Object isVisibleProperty = member.getPropertyValue(mondrian.olap.Property.VISIBLE.getName());
		if (isVisibleProperty == null) {
			return true;
		}
		return (Boolean) isVisibleProperty;
	}

	@Override
	protected OlapElement getOlapElement() {
		return member;
	}
}

// End MondrianOlap4jMember.java
