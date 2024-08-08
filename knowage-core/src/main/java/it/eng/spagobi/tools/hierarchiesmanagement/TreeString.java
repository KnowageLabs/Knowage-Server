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
package it.eng.spagobi.tools.hierarchiesmanagement;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Yifan Peng (original code modified)
 */

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Given a <code>HierarchyTreeNode</code> structure, <code>TreeString</code> will print a string like
 *
 * <pre>
 * â”” a
 *   â”œ b
 *   â”œ c
 *   â”‚ â”œ e
 *   â”‚ â”” f
 *   â”” d
 * </pre>
 *
 */
public class TreeString {
	private static Logger logger = Logger.getLogger(TreeString.class);
	public static String toString(HierarchyTreeNode tree) {
		StringBuffer sb = new StringBuffer();

		Iterator<HierarchyTreeNode> itr = tree.getPreorderIterator();

		while (itr.hasNext()) {
			HierarchyTreeNode tn = itr.next();
			// add prefix
			for (HierarchyTreeNode p : tn.getPathFromRoot()) {
				// if parent has sibling node
				if (p == tn) {
					;
				} else if (p.hasNextSiblingNode()) {
					sb.append(BAR + " ");
				} else {
					sb.append("  ");
				}
			}
			// if root has sibling node
			if (tn.hasNextSiblingNode()) {
				sb.append(MIDDLE + " ");
			} else {
				sb.append(END + " ");
			}
			// sb.append(tn.getObject() + "\n");
			HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) tn.getObject();
			boolean isLeaf = false;
			if (nodeData.leafId != null && !nodeData.leafId.equals("")) {
				isLeaf = true;
			}
			sb.append("(" + nodeData.getNodeCode() + ")" + nodeData.getNodeName() + " -- LEAF:" + isLeaf + "\n");
		}

		return sb.toString();
	}

	// â”‚
	public static final String BAR = "|";
	// â””
	public static final String END = "->";
	// â”œ
	public static final String MIDDLE = "+-";

	private static String bar(int i) {
		try {
			switch (i) {
			case 1:
				return new String(new byte[] { -30, -108, -126 }, "utf8");
			case 2:
				return new String(new byte[] { -30, -108, -108 }, "utf8");
			case 3:
				return new String(new byte[] { -30, -108, -100 }, "utf8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException",e);
		}
		return null;
	}

}
