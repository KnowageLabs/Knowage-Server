/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.hierarchiesmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Yifan Peng (original code modified)
 */
public class HierarchyTreeNode implements Iterable<HierarchyTreeNode> {

	private final Object obj;

	private HierarchyTreeNode parent;

	private String key;

	private List<HierarchyTreeNode> children;

	private final boolean allowsChildren;

	private final List<String> childrenKeys;

	static private final List<HierarchyTreeNode> EMPTY_LIST = Collections.emptyList();
	static private final Iterator<HierarchyTreeNode> EMPTY_ITERATOR = Collections.<HierarchyTreeNode> emptyList().iterator();

	public HierarchyTreeNode(Object obj, String key) {
		this.obj = obj;
		this.key = key;
		parent = null;
		allowsChildren = true;
		childrenKeys = new ArrayList<String>();
	}

	public void add(HierarchyTreeNode newChild, String key) {
		HierarchyTreeNode oldParent = newChild.getParent();

		if (oldParent != null) {
			oldParent.remove(newChild);
		}
		newChild.setParent(this);
		if (children == null) {
			children = new LinkedList<HierarchyTreeNode>();
		}
		int childIndex = getChildCount();
		children.add(childIndex, newChild);
		childrenKeys.add(key);
	}

	public void remove(HierarchyTreeNode aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(aChild)) {
			throw new IllegalArgumentException("argument is not a child");
		}
		remove(indexOf(aChild)); // linear search
	}

	public void remove(int childIndex) {
		HierarchyTreeNode child = getChild(childIndex);
		removeChildrenKey(child.getKey());
		children.remove(childIndex);
		child.setParent(null);
	}

	public void removeChildrenKey(String Key) {
		this.childrenKeys.remove(Key);
	}

	public HierarchyTreeNode getChild(int index) {
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}
		return children.get(index);
	}

	/**
	 * Returns the number of levels above this node -- the distance from the
	 * root to this node. If this node is the root, returns 0.
	 * 
	 * @return the number of levels above this node
	 */
	public int getLevel() {
		HierarchyTreeNode ancestor = this;
		int levels = 0;

		while ((ancestor = ancestor.getParent()) != null) {
			levels++;
		}

		return levels;
	}

	public void setParent(HierarchyTreeNode newParent) {
		parent = newParent;
	}

	public boolean isNodeChild(HierarchyTreeNode aNode) {
		boolean retval;

		if (aNode == null) {
			retval = false;
		} else {
			if (getChildCount() == 0) {
				retval = false;
			} else {
				retval = (aNode.getParent() == this);
			}
		}

		return retval;
	}

	/**
	 * Returns the number of children of this node.
	 * 
	 * @return an int giving the number of children of this node
	 */
	public int getChildCount() {
		if (children == null) {
			return 0;
		} else {
			return children.size();
		}
	}

	/**
	 * Returns this node's user object.
	 * 
	 * @return the Object stored at this node by the user
	 * @see #setObject
	 * @see #toString
	 */
	public Object getObject() {
		return obj;
	}

	/**
	 * Returns this node's parent or null if this node has no parent.
	 * 
	 * @return this node's parent TreeNode, or null if this node has no parent
	 */
	public HierarchyTreeNode getParent() {
		return parent;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 
	 * @return a list of this node's children, or empty list if it is a leaf
	 */
	public List<HierarchyTreeNode> children() {
		if (children == null) {
			return EMPTY_LIST;
		} else {
			return children;
		}
	}

	public List<String> getChildrensKeys() {
		return childrenKeys;
	}

	/**
	 * Returns the index of the specified child in this node's child array. If
	 * the specified node is not a child of this node, returns <code>-1</code>.
	 * This method performs a linear search and is O(n) where n is the number of
	 * children.
	 * 
	 */
	public int indexOf(HierarchyTreeNode aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(aChild)) {
			return -1;
		}
		return children.indexOf(aChild); // linear search
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<HierarchyTreeNode> iterator() {
		return new BreadthFirstIterator(this);
	}

	public Iterator<HierarchyTreeNode> childrenIterator() {
		if (children == null) {
			return EMPTY_ITERATOR;
		} else {
			return children.iterator();
		}
	}

	public Iterator<HierarchyTreeNode> getPreorderIterator() {
		return new PreorderIterator(this);
	}

	/*
	 * ------------------------------------------------------- Utilities Methods
	 * -------------------------------------------------------
	 */

	public List<HierarchyTreeNode> getPathFromRoot() {
		List<HierarchyTreeNode> elderList = getPathToRoot();
		Collections.reverse(elderList);
		return elderList;
	}

	public List<HierarchyTreeNode> getPathToRoot() {
		List<HierarchyTreeNode> elderList = new LinkedList<HierarchyTreeNode>();
		for (HierarchyTreeNode p = this; p != null; p = p.getParent()) {
			elderList.add(p);
		}
		return elderList;
	}

	public boolean hasNextSiblingNode() {
		return getNextSibling() != null;
	}

	public HierarchyTreeNode getNextSibling() {
		HierarchyTreeNode retval;

		HierarchyTreeNode myParent = getParent();

		if (myParent == null) {
			retval = null;
		} else {
			retval = myParent.getChildAfter(this); // linear search
		}

		if (retval != null && !isNodeSibling(retval)) {
			throw new Error("child of parent is not a sibling");
		}

		return retval;
	}

	public HierarchyTreeNode getChildAfter(HierarchyTreeNode aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		int index = indexOf(aChild); // linear search

		if (index == -1) {
			throw new IllegalArgumentException("node is not a child");
		}

		if (index < getChildCount() - 1) {
			return getChild(index + 1);
		} else {
			return null;
		}
	}

	public boolean isNodeSibling(HierarchyTreeNode anotherNode) {
		boolean retval;

		if (anotherNode == null) {
			retval = false;
		} else if (anotherNode == this) {
			retval = true;
		} else {
			HierarchyTreeNode myParent = getParent();
			retval = (myParent != null && myParent == anotherNode.getParent());

			if (retval && !(getParent()).isNodeChild(anotherNode)) {
				throw new Error("sibling has different parent");
			}
		}

		return retval;
	}

	/*********************************************************************
	 * Internal class for BreadthFirstIterator
	 *********************************************************************/

	final class BreadthFirstIterator implements Iterator<HierarchyTreeNode> {

		protected Queue<Iterator<HierarchyTreeNode>> queue;

		public BreadthFirstIterator(HierarchyTreeNode rootNode) {
			super();
			Vector<HierarchyTreeNode> v = new Vector<HierarchyTreeNode>(1);
			v.addElement(rootNode); // PENDING: don't really need a vector
			queue = new LinkedList<Iterator<HierarchyTreeNode>>();
			queue.offer(v.iterator());
		}

		public boolean hasNext() {
			return (!queue.isEmpty() && queue.peek().hasNext());
		}

		public HierarchyTreeNode next() {
			Iterator<HierarchyTreeNode> enumer = queue.peek();
			HierarchyTreeNode node = enumer.next();
			Iterator<HierarchyTreeNode> children = node.childrenIterator();

			if (!enumer.hasNext()) {
				queue.poll();
			}
			if (children.hasNext()) {
				queue.offer(children);
			}
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException("remove() is not supported.");
		}
	}

	/*********************************************************************
	 * Internal class for PreorderIterator
	 *********************************************************************/

	private final class PreorderIterator implements Iterator<HierarchyTreeNode> {

		private final Stack<Iterator<HierarchyTreeNode>> stack = new Stack<Iterator<HierarchyTreeNode>>();

		PreorderIterator(HierarchyTreeNode rootNode) {
			super();
			List<HierarchyTreeNode> l = new Vector<HierarchyTreeNode>(1);
			l.add(rootNode); // PENDING: don't really need a vector
			stack.push(l.iterator());
		}

		public boolean hasNext() {
			return (!stack.empty() && stack.peek().hasNext());
		}

		public HierarchyTreeNode next() {
			Iterator<HierarchyTreeNode> itr = stack.peek();
			HierarchyTreeNode node = itr.next();
			Iterator<HierarchyTreeNode> childrenItr = node.childrenIterator();

			if (!itr.hasNext()) {
				stack.pop();
			}
			if (childrenItr.hasNext()) {
				stack.push(childrenItr);
			}
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException("remove() is not supported.");
		}

	}
}
