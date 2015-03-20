/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable;

import it.eng.spagobi.engines.qbe.crosstable.CrossTab.CellType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class Node implements Cloneable, Comparable<Node> {
	public static final String CROSSTAB_NODE_JSON_CHILDS = "node_childs";
	public static final String CROSSTAB_NODE_JSON_KEY = "node_key";
	public static final String CROSSTAB_NODE_JSON_DESCRIPTION = "node_description";

	private final String value;// the value of the node
	private final String description;// the value of the node
	private CellType cellType;// the value of the node
	private List<Node> childs;// list of childs
	private int leafPosition = -1;// position of the leafs in the tree.. If this
									// is the right most leaf the value is 0 and
									// so on
	private List<Integer> leafPositionsForCF;// Uset for the CF (The node is the
												// result of a merging of
												// nodes.. This list contains
												// the position of the lines of
												// the data matrix with header
												// equals to the value of this
												// node)
	private Node fatherNode; // != from null only if we need the value

	public Node(String value) {
		this.value = value;
		this.description = value;
		childs = new ArrayList<Node>();
	}

	public Node(String value, String description) {
		this.value = value;
		this.description = description;
		childs = new ArrayList<Node>();
	}

	public String getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public List<Node> getChilds() {
		return childs;
	}

	public void setChilds(List<Node> childs) {
		this.childs = childs;
		// Collections.sort(this.childs);
	}

	public void addOrderedChild(Node child) {
		childs.add(child);
		if (childs != null) {
			Collections.sort(childs);
		}
	}

	public void addChild(Node child) {
		childs.add(child);
	}

	public boolean isChild(Node child) {
		return childs.contains(child);
	}

	/**
	 * Get the number of leafs in the tree
	 * 
	 * @return
	 */
	public int getLeafsNumber() {
		if (childs.size() == 0) {
			return 1;
		} else {
			int leafsNumber = 0;
			for (int i = 0; i < childs.size(); i++) {
				leafsNumber = leafsNumber + childs.get(i).getLeafsNumber();
			}
			return leafsNumber;
		}
	}

	/**
	 * Serialize the node and the subtree
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject thisNode = new JSONObject();

		thisNode.put(CROSSTAB_NODE_JSON_KEY, this.value);
		thisNode.put(CROSSTAB_NODE_JSON_DESCRIPTION, this.description);

		if (childs.size() > 0) {
			JSONArray nodeChilds = new JSONArray();
			for (int i = 0; i < childs.size(); i++) {
				nodeChilds.put(childs.get(i).toJSONObject());
			}
			thisNode.put(CROSSTAB_NODE_JSON_CHILDS, nodeChilds);
		}

		return thisNode;
	}

	// public JSONObject toJSONObject() throws JSONException{
	// return toJSONObject(false);
	// }

	public int getLeafPosition() {
		return leafPosition;
	}

	/**
	 * Initialize the variable leafPosition
	 */
	public void setLeafPositions() {
		setLeafPositions(0);
	}

	private int setLeafPositions(int pos) {
		if (childs.size() == 0) {
			leafPosition = pos;
			pos++;
		} else {
			for (int i = 0; i < childs.size(); i++) {
				pos = childs.get(i).setLeafPositions(pos);
			}
		}
		return pos;
	}

	public List<Integer> getLeafPositionsForCF() {
		return leafPositionsForCF;
	}

	public void setLeafPositionsForCF(List<Integer> leafPositionsForCF) {
		this.leafPositionsForCF = leafPositionsForCF;
	}

	/**
	 * return the list of nodes of the passed level
	 * 
	 * @param level
	 * @return
	 */
	public List<Node> getLevel(int level) {
		if (level < 1) {
			level = 0;
		}
		List<Node> nodes = new ArrayList<Node>();
		if (level == 0) {
			nodes.add(this);
		} else {
			if (childs.size() == 0) {
				return null;
			}
			for (int i = 0; i < childs.size(); i++) {
				nodes.addAll(childs.get(i).getLevel(level - 1));
			}
		}
		return nodes;
	}

	/**
	 * Returns the depth level of the node: root is depth 0, its children are
	 * depth 1 and so on...
	 * 
	 * @return the depth level of the node: root is depth 0, its children are
	 *         depth 1 and so on...
	 */
	public int getDistanceFromRoot() {
		if (this.fatherNode == null) {
			return 0;
		} else {
			return this.fatherNode.getDistanceFromRoot() + 1;
		}
	}

	/**
	 * Returns the level distance between the node and its leaves (it is assumed
	 * that the tree is balanced, therefore every leaf has the same distance to
	 * this node). If the node is a leaf, 0 is returned.
	 * 
	 * @return the level distance between the node and its leaves (it is assumed
	 *         that the tree is balanced, therefore every leaf has the same
	 *         distance to this node). If the node is a leaf, 0 is returned.
	 */
	public int getDistanceFromLeaves() {
		if (this.getChilds() == null || this.getChilds().isEmpty()) {
			return 0;
		} else {
			return this.getChilds().get(0).getDistanceFromLeaves() + 1;
		}
	}

	/**
	 * Return the list of leafs of the subtree with this node as radix
	 * 
	 * @return
	 */
	public List<Node> getLeafs() {
		List<Node> list = new ArrayList<Node>();
		if (childs.size() == 0) {
			list.add(this);
		} else {
			for (int i = 0; i < childs.size(); i++) {
				list.addAll(childs.get(i).getLeafs());
			}
		}
		return list;
	}

	/**
	 * Update the fathers of this tree
	 */
	public void updateFathers() {
		for (int i = 0; i < childs.size(); i++) {
			childs.get(i).fatherNode = this;
			childs.get(i).updateFathers();
		}
	}

	public int getSubTreeDepth() {
		if (childs.size() == 0) {
			return 1;
		} else {
			return 1 + childs.get(0).getSubTreeDepth();
		}
	}

	/**
	 * Remove this node from the tree.. IThe fathers must be valued for all the
	 * tree
	 */
	public void removeNodeFromTree() {
		if (fatherNode != null) {
			List<Node> fatherChilds = fatherNode.getChilds();
			for (int i = 0; i < fatherChilds.size(); i++) {
				if (fatherChilds.get(i) == this) {
					fatherChilds.remove(i);
					break;
				}
			}
			if (fatherChilds.size() == 0) {
				fatherNode.removeNodeFromTree();
			}
		}
	}

	public int getRightMostLeafPositionCF() {
		if (childs.size() == 0) {
			return leafPosition;
		}
		return childs.get(childs.size() - 1).getRightMostLeafPositionCF();
	}

	/**
	 * Clone only the value and the children
	 */
	@Override
	public Node clone() {
		Node n = new Node(value, description);
		if (childs.size() > 0) {
			for (int j = 0; j < childs.size(); j++) {
				n.addChild(childs.get(j).clone());
			}
		}
		return n;
	}

	@Override
	public String toString() {
		String string;

		if (childs.size() == 0) {
			return "[V:" + value.toString() + "-D:" + description + "]";
		} else {
			string = "[V:" + value.toString() + "-D:" + description + ",[";
			for (int i = 0; i < childs.size() - 1; i++) {
				string = string + childs.get(i).toString() + ",";
			}
			string = string + childs.get(childs.size() - 1).toString() + "]]";
		}
		return string;
	}

	/**
	 * For test
	 * 
	 * @param height
	 * @param branch
	 */
	public void buildSubTree(int height, int branch) {
		if (height < 2) {
			for (int i = 0; i < branch; i++) {
				addChild(new Node("" + i));
			}
		} else {
			for (int i = 0; i < branch; i++) {
				Node n = new Node(value + "_" + i);
				addChild(n);
				n.buildSubTree(height - 1, branch);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Node other = (Node) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public CellType getCellType() {
		return cellType;
	}

	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object) order always for
	 * value
	 */
	@Override
	public int compareTo(Node arg0) {
		// check if the values are floats
		try {
			Float thisValue = new Float(value);
			Float arg0Value = new Float(arg0.getValue());
			return thisValue.compareTo(arg0Value);
		} catch (Exception e) {
			// if its not possible to convert the values in float, consider them
			// as strings
			return value.compareTo(arg0.getValue());
		}
	}

	public void orderedSubtree() {
		if (childs != null) {
			Collections.sort(childs);
		}
		for (int i = 0; i < childs.size(); i++) {
			childs.get(i).orderedSubtree();
		}
	}

}
