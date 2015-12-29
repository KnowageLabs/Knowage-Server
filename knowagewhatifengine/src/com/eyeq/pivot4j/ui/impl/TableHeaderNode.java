/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */

/**
 * Modifications:
 * overwrite the method addMemberProperties in line 249
 */
package com.eyeq.pivot4j.ui.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.util.OlapUtils;
import com.eyeq.pivot4j.util.TreeNode;
import com.eyeq.pivot4j.util.TreeNodeCallback;

public class TableHeaderNode extends TreeNode<TableAxisContext> {

	private Position position;

	private Member member;

	private Property property;

	private Hierarchy hierarchy;

	private Integer colSpan;

	private Integer rowSpan;

	private Integer colIndex;

	private Integer rowIndex;

	private Integer maxRowIndex;

	private Integer hierarchyDescendants;

	private Integer memberChildren;

	private boolean aggregation = false;

	private Aggregator aggregator;

	/**
	 * @param context
	 */
	public TableHeaderNode(TableAxisContext context) {
		super(context);
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(Member member) {
		this.member = member;
	}

	/**
	 * @return the level
	 */
	public Level getMemberLevel() {
		return member == null ? null : member.getLevel();
	}

	/**
	 * @return the hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return the property
	 */
	public Property getProperty() {
		return property;
	}

	/**
	 * @param property
	 *            the property to set
	 */
	public void setProperty(Property property) {
		this.property = property;
	}

	public void clearCache() {
		this.colIndex = null;
		this.rowIndex = null;
		this.colSpan = null;
		this.rowSpan = null;
		this.maxRowIndex = null;
		this.hierarchyDescendants = null;
		this.memberChildren = null;
	}

	public int getHierarchyIndex() {
		if (hierarchy == null) {
			return -1;
		}
		return getReference().getHierarchies().indexOf(hierarchy);
	}

	public Level getRootLevel() {
		int index = getHierarchyIndex();
		if (index < 0) {
			return null;
		}

		return getReference().getLevels(getHierarchy()).get(0);
	}

	public int getMaxRowIndex() {
		if (maxRowIndex == null) {
			if (getChildCount() == 0) {
				this.maxRowIndex = getRowIndex();
			} else {
				this.maxRowIndex = 0;

				for (TreeNode<TableAxisContext> child : getChildren()) {
					TableHeaderNode nodeChild = (TableHeaderNode) child;
					maxRowIndex = Math.max(maxRowIndex, nodeChild.getMaxRowIndex());
				}
			}
		}

		return maxRowIndex;
	}

	void addHierarhcyHeaders() {
		List<TreeNode<TableAxisContext>> children = new ArrayList<TreeNode<TableAxisContext>>(getChildren());

		for (TreeNode<TableAxisContext> child : children) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;

			Hierarchy childHierarchy = nodeChild.getHierarchy();

			if (childHierarchy != null && !OlapUtils.equals(hierarchy, childHierarchy)) {
				int index = getChildren().indexOf(child);

				removeChild(child);

				TableHeaderNode hierarchyNode = new TableHeaderNode(getReference());
				hierarchyNode.setHierarchy(childHierarchy);

				addChild(index, hierarchyNode);
				hierarchyNode.addChild(child);
			}

			nodeChild.addHierarhcyHeaders();
		}
	}

	void addParentMemberHeaders() {
		List<TreeNode<TableAxisContext>> children = new ArrayList<TreeNode<TableAxisContext>>(getChildren());

		for (TreeNode<TableAxisContext> child : children) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;

			Member mem = nodeChild.getMember();
			if (mem != null) {
				int index = getChildren().indexOf(child);

				removeChild(child);

				TreeNode<TableAxisContext> childNode = child;

				Member parent = mem;

				while (parent != null) {
					parent = getReference().getParentMember(parent);

					if (parent == null) {
						break;
					}
					TableHeaderNode parentNode = new TableHeaderNode(getReference());
					parentNode.setPosition(nodeChild.getPosition());
					parentNode.setHierarchy(parent.getHierarchy());
					parentNode.setMember(parent);
					parentNode.addChild(childNode);

					childNode = parentNode;
				}

				addChild(index, childNode);
			}

			nodeChild.addParentMemberHeaders();
		}
	}

	/**
	 * @param collector
	 */
	void addMemberProperties() {
		if (getReference().getAxis() != Axis.ROWS) {
			return;
		}

		List<TreeNode<TableAxisContext>> children = null;

		if (getMember() != null) {
			List<Level> levels = getReference().getLevels(getHierarchy());

			int index = levels.indexOf(getMember().getLevel());
			int endIndex = getReference().getPivotRenderer().getShowParentMembers() ? index + 1 : levels.size();

			/**
			 * Add the check index>=0 . To solve an index out of bound exception.. If it can find the level of the current member (we've found the problem with
			 * the all members) it skips the operation
			 */
			if (index >= 0) {
				List<Level> lowerLevels = levels.subList(index, endIndex);

				for (Level level : lowerLevels) {
					List<Property> properties = getReference().getProperties(level);

					if (!properties.isEmpty()) {
						children = new ArrayList<TreeNode<TableAxisContext>>(getChildren());
						clear();

						TableHeaderNode parentNode = this;

						for (Property prop : properties) {
							TableHeaderNode propertyNode = new TableHeaderNode(getReference());
							propertyNode.setPosition(position);
							propertyNode.setHierarchy(getHierarchy());
							propertyNode.setMember(getMember());
							propertyNode.setProperty(prop);

							parentNode.addChild(propertyNode);

							parentNode = propertyNode;
						}

						for (TreeNode<TableAxisContext> child : children) {
							parentNode.addChild(child);
						}
					}
				}
			}

		}

		if (children == null) {
			children = getChildren();
		}

		for (TreeNode<TableAxisContext> child : children) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;
			nodeChild.addMemberProperties();
		}
	}

	void mergeChildren() {
		List<TreeNode<TableAxisContext>> children = new ArrayList<TreeNode<TableAxisContext>>(getChildren());

		TableHeaderNode lastChild = null;

		for (TreeNode<TableAxisContext> child : children) {
			TableHeaderNode headerNode = (TableHeaderNode) child;

			if (lastChild == null) {
				lastChild = headerNode;
				continue;
			}

			if (lastChild.canMergeWith(headerNode)) {
				for (TreeNode<TableAxisContext> c : child.getChildren()) {
					lastChild.addChild(c);
				}

				removeChild(child);
			} else {
				lastChild = headerNode;
			}
		}

		for (TreeNode<TableAxisContext> child : getChildren()) {
			TableHeaderNode headerNode = (TableHeaderNode) child;
			headerNode.mergeChildren();
		}
	}

	/**
	 * @param sibling
	 * @return
	 */
	protected boolean canMergeWith(TableHeaderNode sibling) {
		if (!OlapUtils.equals(hierarchy, sibling.getHierarchy())) {
			return false;
		}

		if (!OlapUtils.equals(member, sibling.getMember())) {
			return false;
		}

		if (!OlapUtils.equals(property, sibling.getProperty())) {
			return false;
		}

		if (!OlapUtils.equals(property, sibling.getProperty())) {
			return false;
		}

		if (aggregator == null) {
			if (sibling.getAggregator() != null) {
				return false;
			}
		} else {
			Aggregator other = sibling.getAggregator();

			if (other == null) {
				return false;
			}

			if (!ObjectUtils.equals(aggregator.getName(), other.getName())) {
				return false;
			}

			if (!ObjectUtils.equals(aggregator.getLevel(), other.getLevel())) {
				return false;
			}
		}

		return getRowSpan() == sibling.getRowSpan();
	}

	public int getColIndex() {
		if (colIndex == null) {
			if (getParent() == null) {
				this.colIndex = 0;
				return colIndex;
			}

			int index = ((TableHeaderNode) getParent()).getColIndex();
			int childIndex = getParent().getChildren().indexOf(this);

			for (int i = 0; i < childIndex; i++) {
				index += getParent().getChildren().get(i).getWidth();
			}

			this.colIndex = index;
		}

		return colIndex;
	}

	public int getRowIndex() {
		if (rowIndex == null) {
			if (getParent() == null) {
				this.rowIndex = 0;
				return rowIndex;
			} else {
				TableHeaderNode headerParent = (TableHeaderNode) getParent();
				this.rowIndex = headerParent.getRowIndex() + headerParent.getRowSpan();
			}
		}
		return rowIndex;
	}

	public int getColSpan() {
		if (colSpan == null) {
			this.colSpan = getWidth();
		}

		return colSpan;
	}

	public int getRowSpan() {
		if (rowSpan == null) {
			if ((member == null || property != null) && aggregator == null) {
				this.rowSpan = 1;
				return rowSpan;
			}

			final Map<Hierarchy, Integer> maxSpans = new HashMap<Hierarchy, Integer>(getReference().getHierarchies().size());

			if (aggregator != null) {
				getRoot().walkTree(new TreeNodeCallback<TableAxisContext>() {

					public int handleTreeNode(TreeNode<TableAxisContext> node) {
						TableHeaderNode nodeChild = (TableHeaderNode) node;

						if (nodeChild.getMember() == null) {
							return TreeNodeCallback.CONTINUE;
						} else {
							Integer maxSpan = maxSpans.get(nodeChild.getHierarchy());
							if (maxSpan == null) {
								maxSpan = 0;
							}

							int current = nodeChild.getHierarchyDescendents();

							TableHeaderNode parent = nodeChild;
							while (parent != null) {
								parent = (TableHeaderNode) parent.getParent();

								if (OlapUtils.equals(nodeChild.getHierarchy(), parent.getHierarchy()) && parent.getMember() == null) {
									current++;
								} else {
									break;
								}
							}

							if (current > maxSpan) {
								maxSpans.put(nodeChild.getHierarchy(), current);
							}
						}

						return TreeNodeCallback.CONTINUE;
					}
				});
			}

			if (member == null) {
				int totalSpans = 0;

				for (Integer span : maxSpans.values()) {
					totalSpans += span;
				}

				this.rowSpan = totalSpans;

				if (hierarchy != null) {
					for (Hierarchy hier : getReference().getHierarchies()) {
						if (OlapUtils.equals(hier, hierarchy)) {
							break;
						}

						this.rowSpan -= maxSpans.get(hier);
					}

					TableHeaderNode parent = this;
					while (true) {
						parent = (TableHeaderNode) parent.getParent();

						if (parent == null || !OlapUtils.equals(hierarchy, parent.getHierarchy())) {
							break;
						} else {
							this.rowSpan -= parent.getRowSpan();
						}
					}
				}

				TableHeaderNode child = this;
				while (child != null) {
					if (child.getChildCount() > 0) {
						child = (TableHeaderNode) child.getChildren().get(0);
						this.rowSpan -= child.getRowSpan();
					} else {
						break;
					}
				}
			} else {
				final int[] childSpan = new int[] { 0 };
				final int[] maxSpan = new int[] { 0 };

				walkChildrenAtColIndex(new TreeNodeCallback<TableAxisContext>() {

					public int handleTreeNode(TreeNode<TableAxisContext> node) {
						TableHeaderNode nodeChild = (TableHeaderNode) node;

						if (nodeChild == TableHeaderNode.this) {
							return TreeNodeCallback.CONTINUE;
						} else if (OlapUtils.equals(hierarchy, nodeChild.getHierarchy())) {
							childSpan[0] += nodeChild.getRowSpan();
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.BREAK;
						}
					}
				}, getColIndex());

				getRoot().walkTree(new TreeNodeCallback<TableAxisContext>() {

					public int handleTreeNode(TreeNode<TableAxisContext> node) {
						TableHeaderNode nodeChild = (TableHeaderNode) node;

						Level level = null;
						Member nodeMember = nodeChild.getMember();

						if (nodeChild == TableHeaderNode.this) {
							return TreeNodeCallback.CONTINUE;
						} else if (nodeMember != null) {
							level = nodeMember.getLevel();
						} else if (nodeChild.getAggregator() != null) {
							level = nodeChild.getAggregator().getLevel();
						}

						if (OlapUtils.equals(member.getLevel(), level)) {
							if (nodeMember != null
									&& (getReference().getAncestorMembers(nodeMember).contains(member) || getReference().getAncestorMembers(member).contains(
											nodeMember))) {
								return TreeNodeCallback.CONTINUE;
							}

							int span = nodeChild.getHierarchyDescendents();

							// Handling a corner case of #54
							if (aggregator == null && nodeChild.getAggregator() != null && member instanceof Measure
									&& getReference().getHierarchies().size() == 1) {
								span++;
							}

							maxSpan[0] = Math.max(maxSpan[0], span);
						}

						return TreeNodeCallback.CONTINUE;
					}
				});

				this.rowSpan = Math.max(1, maxSpan[0] - childSpan[0]);

				if (aggregator != null) {
					boolean child = false;

					for (Hierarchy hier : getReference().getHierarchies()) {
						if (OlapUtils.equals(hier, hierarchy)) {
							child = true;
							continue;
						}

						Type type;

						try {
							type = hier.getDimension().getDimensionType();
						} catch (OlapException e) {
							throw new PivotException(e);
						}

						if (child && type != Type.MEASURE) {
							this.rowSpan += maxSpans.get(hier);
						}
					}
				}
			}
		}

		return rowSpan;
	}

	/**
	 * @return the aggregation
	 */
	public boolean isAggregation() {
		return aggregation;
	}

	/**
	 * @param aggregation
	 *            the aggregation to set
	 */
	public void setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * @return the aggregator
	 */
	public Aggregator getAggregator() {
		return aggregator;
	}

	/**
	 * @param aggregator
	 *            the aggregator to set
	 */
	public void setAggregator(Aggregator aggregator) {
		this.aggregator = aggregator;
	}

	public TableHeaderNode getHierarchyRoot() {
		TableHeaderNode parent = this;
		while (true) {
			TableHeaderNode node = (TableHeaderNode) parent.getParent();

			if (node != null && OlapUtils.equals(hierarchy, node.getHierarchy())) {
				parent = node;
			} else {
				break;
			}
		}
		return parent;
	}

	public int getHierarchyDescendents() {
		if (member == null || getChildCount() == 0) {
			return 1;
		}

		if (hierarchyDescendants == null) {
			int height = 1;
			for (TreeNode<TableAxisContext> child : getChildren()) {
				TableHeaderNode nodeChild = (TableHeaderNode) child;
				if (OlapUtils.equals(hierarchy, nodeChild.getHierarchy())) {
					height = Math.max(height, 1 + nodeChild.getHierarchyDescendents());
				}
			}
			this.hierarchyDescendants = height;
		}

		return hierarchyDescendants;
	}

	protected List<Member> getMemberPath() {
		List<Member> path = new LinkedList<Member>();

		TableHeaderNode node = (TableHeaderNode) getParent();

		while (node != null) {
			path.add(0, node.getMember());
			node = (TableHeaderNode) node.getParent();
		}

		return path;
	}

	/**
	 * @param parentPath
	 * @param childPath
	 * @return
	 */
	private static boolean isSubPath(List<Member> parentPath, List<Member> childPath) {
		Iterator<Member> it = childPath.iterator();
		for (Member member : parentPath) {
			if (!OlapUtils.equals(member, it.next())) {
				return false;
			}
		}

		return true;
	}

	public int getMemberChildren() {
		if (member == null) {
			return 0;
		}

		if (memberChildren == null) {
			final List<Member> path = getMemberPath();

			final int[] childCount = new int[] { 0 };

			final int depth = member.getDepth();

			getRoot().walkChildren(new TreeNodeCallback<TableAxisContext>() {

				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode nodeChild = (TableHeaderNode) node;

					if (node == TableHeaderNode.this) {
						return TreeNodeCallback.CONTINUE;
					}

					if (OlapUtils.equals(hierarchy, nodeChild.getHierarchy())) {
						List<Member> childPath = nodeChild.getMemberPath();

						if (path.size() > childPath.size() || !isSubPath(path, childPath)) {
							return TreeNodeCallback.CONTINUE;
						}

						Member childMember = nodeChild.getMember();

						if (childMember != null) {
							int childDepth = childMember.getDepth();

							if (getReference().getAncestorMembers(childMember).contains(member)) {
								childCount[0]++;

								return TreeNodeCallback.CONTINUE_SIBLING;
							} else if (depth == childDepth) {
								if (!OlapUtils.equals(childMember, member)) {
									return TreeNodeCallback.CONTINUE_SIBLING;
								}
							} else if (depth < childDepth || !getReference().getAncestorMembers(member).contains(childMember)) {
								return TreeNodeCallback.CONTINUE_SIBLING;
							}
						}
					} else if (nodeChild.getMember() != null) {
						TableHeaderNode parent = TableHeaderNode.this;

						while (true) {
							parent = (TableHeaderNode) parent.getParent();

							if (parent == null) {
								return TreeNodeCallback.CONTINUE_PARENT;
							}

							Member parentMember = parent.getMember();

							if (OlapUtils.equals(parent.getHierarchy(), nodeChild.getHierarchy()) && parentMember != null) {
								if (OlapUtils.equals(parentMember, nodeChild.getMember())
										|| getReference().getAncestorMembers(parentMember).contains(nodeChild.getMember())) {
									return TreeNodeCallback.CONTINUE;
								} else {
									return TreeNodeCallback.CONTINUE_SIBLING;
								}
							}
						}
					}

					return TreeNodeCallback.CONTINUE;
				}
			});

			this.memberChildren = childCount[0];
		}

		return memberChildren;
	}

	/**
	 * @param callbackHandler
	 * @param rowIndex
	 * @return
	 */
	public int walkChildrenAtRowIndex(TreeNodeCallback<TableAxisContext> callbackHandler, int rowIndex) {
		int code = 0;
		for (TreeNode<TableAxisContext> child : getChildren()) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;
			int childIndex = nodeChild.getRowIndex();

			if (rowIndex == childIndex) {
				code = callbackHandler.handleTreeNode(child);
				if (code >= TreeNodeCallback.CONTINUE_PARENT) {
					return code;
				}
			} else if (rowIndex > child.getLevel()) {
				nodeChild.walkChildrenAtRowIndex(callbackHandler, rowIndex);
			}
		}
		return code;
	}

	/**
	 * @param colIndex
	 * @return
	 */
	public TableHeaderNode getLeafNodeAtColIndex(int colIndex) {
		if (getChildCount() == 0 && getColIndex() == colIndex) {
			return this;
		}

		for (TreeNode<TableAxisContext> child : getChildren()) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;

			int startIndex = nodeChild.getColIndex();
			int endIndex = startIndex + nodeChild.getColSpan();

			if (colIndex >= startIndex && colIndex < endIndex) {
				return nodeChild.getLeafNodeAtColIndex(colIndex);
			} else if (endIndex > colIndex) {
				break;
			}
		}
		return null;
	}

	/**
	 * @param callbackHandler
	 * @param colIndex
	 */
	public int walkChildrenAtColIndex(TreeNodeCallback<TableAxisContext> callbackHandler, int colIndex) {
		int code = 0;

		if (getColIndex() == colIndex) {
			code = callbackHandler.handleTreeNode(this);
			if (code >= TreeNodeCallback.CONTINUE_PARENT) {
				return code;
			}
		}

		for (TreeNode<TableAxisContext> child : getChildren()) {
			TableHeaderNode nodeChild = (TableHeaderNode) child;
			int startIndex = nodeChild.getColIndex();
			int endIndex = startIndex + nodeChild.getColSpan();

			if (colIndex < startIndex) {
				code = TreeNodeCallback.CONTINUE_SIBLING;
			} else if (colIndex >= endIndex) {
				code = TreeNodeCallback.CONTINUE_PARENT;
			} else {
				code = nodeChild.walkChildrenAtColIndex(callbackHandler, colIndex);
				break;
			}
		}

		return code;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (member != null) {
			return member.getCaption();
		} else if (hierarchy != null) {
			return hierarchy.getCaption();
		} else {
			return getReference().getAxis().name();
		}
	}
}
