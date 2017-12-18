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
package it.eng.spagobi.utilities.tree;

import java.util.Iterator;
import java.util.List;


/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class Tree<E> {

	public static final String NODES_PATH_SEPARATOR = "/";
	
	private Node<E> root;
	
	public Tree(){}
	
	public Tree(Node<E> node){
		this.root = node;
	}
	
	public Node<E> getRoot() {
		return root;
	}
	
	public boolean containsPath(String path) {
		return containsPath(root, path);
	}
	
	private boolean containsPath(Node node, String path) {
		if (node.getPath().equals(path)) {
			return true;
		}
		if (path.startsWith(node.getPath() + NODES_PATH_SEPARATOR)) {
			List<Node<E>> children = node.getChildren();
			Iterator<Node<E>> it = children.iterator();
			while (it.hasNext()) {
				Node<E> aChild = it.next();
				if (containsPath(aChild, path))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Root: " + root);
		List<Node<E>> children = root.getChildren();
		if (children != null && children.size() > 0) {
			Iterator<Node<E>> it = children.iterator();
			while (it.hasNext()) {
				append(buffer, it.next());
			}
		}
		return buffer.toString();
	}
	
	public void append(StringBuffer buffer, Node<E> node) {
		buffer.append("\n" + node);
		List<Node<E>> children = node.getChildren();
		if (children != null && children.size() > 0) {
			Iterator<Node<E>> it = children.iterator();
			while (it.hasNext()) {
				append(buffer, it.next());
			}
		}
	}

}
