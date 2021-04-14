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
package it.eng.knowage.engine.cockpit.api.crosstable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab.CellType;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class Node implements Cloneable, Comparable<Node> {
	public static final String CROSSTAB_NODE_JSON_CHILDS = "node_childs";
	public static final String CROSSTAB_NODE_JSON_KEY = "node_key";
	public static final String CROSSTAB_NODE_JSON_DESCRIPTION = "node_description";
	public static final String CROSSTAB_NODE_JSON_COLUMN = "node_column";
	public static final String CROSSTAB_NODE_COLUMN_ROOT = "rootC";
	public static final String CROSSTAB_NODE_ROW_ROOT = "rootR";

	private String columnName = null;// column name
	private final String value;// the value of the node
	private final String description;// the description of the node
	private final boolean measure;
	private CellType cellType;// the type of the node
	private List<Node> children;// list of childs
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

	private Integer distanceFromRoot;
	private final JSONObject jsonObject;

	public Node(String value) {
		this.value = value;
		this.description = value;
		measure = false;
		children = new ArrayList<Node>();
		jsonObject = null;
	}

	public Node(String value, String description) {
		this.value = value;
		this.description = description;
		measure = false;
		children = new ArrayList<Node>();
		jsonObject = null;
	}

	public Node(String columnName, String value, String description) {
		this.columnName = columnName;
		this.value = value;
		this.description = description;
		measure = false;
		children = new ArrayList<Node>();
		jsonObject = null;
	}

	public Node(String columnName, String value, String description, boolean measure) {
		this.columnName = columnName;
		this.value = value;
		this.description = description;
		this.measure = measure;
		children = new ArrayList<Node>();
		jsonObject = null;
	}

	public Node(String columnName, String value, String description, JSONObject jsonObject) {
		this.columnName = columnName;
		this.value = value;
		this.description = description;
		this.measure = false;
		children = new ArrayList<Node>();
		this.jsonObject = jsonObject;
	}

	public String getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * @return the jsonObject
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public Node getParentNode() {
		return this.fatherNode;

	}

	public Node getFirstAncestor() {
		Node father = this.getParentNode();
		while (father.getParentNode() != null) {
			father = father.getParentNode();
		}
		return father;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		for (int i = 0; i < children.size(); i++) {
			children.get(i).setFatherNode(this);
		}
		this.children = children;
	}

	private void setFatherNode(Node father) {
		this.fatherNode = father;
	}

	public void addOrderedChild(Node child) {
		children.add(child);
		child.fatherNode = this;
		if (children != null) {
			Collections.sort(children);
		}
	}

	public void addOrderedChild(Node child, Comparator<Node> comp) {
		children.add(child);
		child.fatherNode = this;
		if (children != null) {
			if (comp == null)
				Collections.sort(children);
			else
				Collections.sort(children, comp);
		}
	}

	public void addChild(Node child) {
		children.add(child);
		child.fatherNode = this;
	}

	public boolean isChild(Node child) {
		return children.contains(child);
	}

	public boolean isRoot() {
		if (this.getDescription().equalsIgnoreCase(Node.CROSSTAB_NODE_COLUMN_ROOT) || this.getDescription().equalsIgnoreCase(Node.CROSSTAB_NODE_ROW_ROOT))
			return true;
		else
			return false;
	}

	/**
	 * Get the number of leafs in the tree
	 *
	 * @return
	 */
	public int getLeafsNumber() {
		if (children.size() == 0) {
			return 1;
		} else {
			int leafsNumber = 0;
			for (int i = 0; i < children.size(); i++) {
				leafsNumber = leafsNumber + children.get(i).getLeafsNumber();
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

		thisNode.put(CROSSTAB_NODE_JSON_COLUMN, this.columnName);
		thisNode.put(CROSSTAB_NODE_JSON_KEY, this.value);
		thisNode.put(CROSSTAB_NODE_JSON_DESCRIPTION, this.description);

		if (children.size() > 0) {
			JSONArray nodeChilds = new JSONArray();
			for (int i = 0; i < children.size(); i++) {
				nodeChilds.put(children.get(i).toJSONObject());
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
		if (children.size() == 0) {
			leafPosition = pos;
			pos++;
		} else {
			for (int i = 0; i < children.size(); i++) {
				pos = children.get(i).setLeafPositions(pos);
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
		List<Node> nodes = new ArrayList<Node>();
		if (level == 0) {
			nodes.add(this);
		} else {
			if (children.size() == 0) {
				return null;
			}
			for (int i = 0; i < children.size(); i++) {
				nodes.addAll(children.get(i).getLevel(level - 1));
			}
		}
		return nodes;
	}

	/**
	 * set the list of nodes of the passed level
	 *
	 * @param level
	 * @param nodes
	 */
	// public void setLevel(int level, List<Node> nodes) {
	//
	// List<Node> oldNodes = childs.getLevel(level);
	// for (int i = 0; i < childs.size(); i++) {
	// nodes.addAll(childs.get(i).getLevel(level - 1));
	// }
	//
	// if (level == 0) {
	// nodes.add(this);
	// } else {
	// if (childs.size() == 0) {
	// return null;
	// }
	// for (int i = 0; i < childs.size(); i++) {
	// nodes.addAll(childs.get(i).getLevel(level - 1));
	// }
	// }
	// }

	/**
	 * Returns the depth level of the node: root is depth 0, its children are depth 1 and so on...
	 *
	 * @return the depth level of the node: root is depth 0, its children are depth 1 and so on...
	 */
	public int getDistanceFromRoot() {
		if (this.distanceFromRoot == null) {
			if (this.fatherNode == null) {
				distanceFromRoot = 0;
			} else {
				distanceFromRoot = this.fatherNode.getDistanceFromRoot() + 1;
			}
		}
		return this.distanceFromRoot;
	}

	/**
	 * Returns the level distance between the node and its leaves (it is assumed that the tree is balanced, therefore every leaf has the same distance to this
	 * node). If the node is a leaf, 0 is returned.
	 *
	 * @return the level distance between the node and its leaves (it is assumed that the tree is balanced, therefore every leaf has the same distance to this
	 *         node). If the node is a leaf, 0 is returned.
	 */
	public int getDistanceFromLeaves() {
		if (this.getChildren() == null || this.getChildren().isEmpty()) {
			return 0;
		} else {
			return this.getChildren().get(0).getDistanceFromLeaves() + 1;
		}
	}

	/**
	 * Return the list of leafs of the subtree with this node as radix
	 *
	 * @return
	 */
	public List<Node> getLeafs() {
		List<Node> list = new ArrayList<Node>();
		if (children.size() == 0) {
			list.add(this);
		} else {
			for (int i = 0; i < children.size(); i++) {
				list.addAll(children.get(i).getLeafs());
			}
		}
		return list;
	}

//	/**
//	 * Update the fathers of this tree
//	 */
//	public void updateFathers() {
//		for (int i = 0; i < children.size(); i++) {
//			children.get(i).fatherNode = this;
//			children.get(i).updateFathers();
//		}
//	}

	public int getSubTreeDepth() {
		if (children.size() == 0) {
			return 1;
		} else {
			return 1 + children.get(0).getSubTreeDepth();
		}
	}

	/**
	 * Remove this node from the tree.. IThe fathers must be valued for all the tree
	 */
	public void removeNodeFromTree() {
		if (fatherNode != null) {
			List<Node> fatherChilds = fatherNode.getChildren();
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
		if (children.size() == 0) {
			return leafPosition;
		}
		return children.get(children.size() - 1).getRightMostLeafPositionCF();
	}

	/**
	 * Clone only the value and the children
	 */
	@Override
	public Node clone() {
		Node n = new Node(columnName, value, description, measure);
		if (children.size() > 0) {
			for (int j = 0; j < children.size(); j++) {
				n.addChild(children.get(j).clone());
			}
		}
		return n;
	}

//	@Override
//	public String toString() {
//		String string;
//
//		if (children.size() == 0) {
//			return "[C:" + String.valueOf(columnName) + "-V:" + value.toString() + "-D:" + description + "]";
//		} else {
//			string = "[C:" + String.valueOf(columnName) + "-V:" + value.toString() + "-D:" + description + ",[";
//			for (int i = 0; i < children.size() - 1; i++) {
//				string = string + children.get(i).toString() + ",";
//			}
//			string = string + children.get(children.size() - 1).toString() + "]]";
//		}
//		return string;
//	}

	@Override
	public String toString() {
		return "{C:" + String.valueOf(columnName) + "-V:" + value.toString() + "-D:" + description + "}";
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
	 * @see java.lang.Comparable#compareTo(java.lang.Object) order always for value
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

	public void orderedSubtree(Map<Integer, NodeComparator> sortKeys) {
		if (children != null) {
			Comparator<Node> comparator = null;
			if (sortKeys != null) {
				comparator = sortKeys.get(this.getDistanceFromRoot());
				if (comparator != null) {
					Collections.sort(children, comparator);
				} else {
					Collections.sort(children);
				}
			}
		}
		for (int i = 0; i < children.size(); i++) {
			children.get(i).orderedSubtree(sortKeys);
		}
	}

	public String getPath() {
		return getPath(this);
	}

	private String getPath(Node node) {
		StringBuilder sb = new StringBuilder();

		if (node.fatherNode != null) {
			sb.append(getPath(node.fatherNode)).append("/");
		}

		sb.append("[C:" + String.valueOf(columnName) + "-V:" + node.value.toString() + "-D:" + node.description + "]");

		return sb.toString();
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isMeasure() {
		return measure;
	}

	public boolean isTotal() {
		boolean total = false;
		if (value != null && value.equals("Total"))
			total = true;
		return total;
	}

	public boolean isSubTotal() {
		boolean subTotal = false;
		if (value != null && value.equals("SubTotal"))
			subTotal = true;
		return subTotal;
	}

}
