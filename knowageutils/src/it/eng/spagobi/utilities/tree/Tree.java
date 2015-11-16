/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
