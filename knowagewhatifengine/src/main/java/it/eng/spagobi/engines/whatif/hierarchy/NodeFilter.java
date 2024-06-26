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

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;

/**
 * @author Dragan Pirkovic
 *
 */
public class NodeFilter implements Comparable<NodeFilter> {

	private final String id;
	private final String name;
	private final String uniqueName;
	private boolean collapsed;
	private boolean visible;
	private final boolean leaf;

	private final List<NodeFilter> children;

	public NodeFilter(Member m) throws OlapException {
		super();

		this.id = m.getUniqueName();
		this.uniqueName = m.getUniqueName();
		this.name = m.getCaption();
		this.visible = false;
		this.collapsed = false;
		this.children = new ArrayList<NodeFilter>();
		this.leaf = m.getChildMemberCount() == 0;

		if (m != null) {
			List<Member> list = (List<Member>) m.getChildMembers();
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					NodeFilter nf = new NodeFilter(list.get(i));
					children.add(nf);
				}
			}
		}
	}

	public NodeFilter(Member m, int depth, List<Member> leaves, List<Member> visibleMembers, boolean showSiblings) throws OlapException {

		super();
		if (visibleMembers != null && visibleMembers.contains(m)) {
			this.visible = true;
			visibleMembers.remove(m);
		} else {
			this.visible = false;
		}

		this.id = m.getUniqueName();
		this.uniqueName = m.getUniqueName();
		this.name = m.getCaption();
		this.collapsed = false;
		this.children = new ArrayList<NodeFilter>();
		this.leaf = m.getChildMemberCount() == 0;

		int curDepth = m.getDepth();
		if (curDepth <= depth && !leaves.isEmpty()) {

			for (Member member : m.getChildMembers()) {

				if (isPotentialChild(leaves, member)) {
					this.collapsed = true;

					if (showSiblings && leaves.contains(member)) {

						for (Member member2 : m.getChildMembers()) {
							children.add(new NodeFilter(member2, depth, leaves, visibleMembers, showSiblings));
						}

						break;

					} else {
						children.add(new NodeFilter(member, depth, leaves, visibleMembers, showSiblings));

					}

					leaves.remove(member);
				}

			}

		}

	}

	/**
	 * @return the children
	 */
	public List<NodeFilter> getChildren() {
		return children;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the uniqueName
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @return the collapsed
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * @return the leaf
	 */
	public boolean isLeaf() {
		return leaf;
	}

	public boolean isPotentialChild(List<Member> leaves, Member member) {
		if (leaves.contains(member)) {
			return true;
		}
		for (Member leaf : leaves) {

			if (isDescendant(member, leaf)) {
				return true;
			}

		}

		return false;
	}

	/**
	 * @param member
	 * @param treeMember
	 * @return
	 */
	private boolean isDescendant(Member member, Member treeMember) {
		return treeMember.getUniqueName().contains(member.getUniqueName());
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param collapsed the collapsed to set
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NodeFilter o) {
		return this.uniqueName.compareTo(o.uniqueName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + (collapsed ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (leaf ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeFilter other = (NodeFilter) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (collapsed != other.collapsed)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (leaf != other.leaf)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uniqueName == null) {
			if (other.uniqueName != null)
				return false;
		} else if (!uniqueName.equals(other.uniqueName))
			return false;
		if (visible != other.visible)
			return false;
		return true;
	}

}
