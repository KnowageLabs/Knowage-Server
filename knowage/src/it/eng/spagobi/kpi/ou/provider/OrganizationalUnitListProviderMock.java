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
package it.eng.spagobi.kpi.ou.provider;

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.utilities.tree.Node;
import it.eng.spagobi.utilities.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrganizationalUnitListProviderMock extends
		OrganizationalUnitListProvider {
	
	String S = Tree.NODES_PATH_SEPARATOR;

	OrganizationalUnit root = new OrganizationalUnit(null, "root", "root", null);
	OrganizationalUnit o1 = new OrganizationalUnit(null, "o1", "o1", null);
	OrganizationalUnit o2 = new OrganizationalUnit(null, "o2", "o2", null);
	OrganizationalUnit o3 = new OrganizationalUnit(null, "o3", "o3", null);
	OrganizationalUnit o1o1 = new OrganizationalUnit(null, "o1o1", "o1o1", null);
	OrganizationalUnit o1o2 = new OrganizationalUnit(null, "o1o2", "o1o2", null);
	OrganizationalUnit o1o3 = new OrganizationalUnit(null, "o1o3", "o1o3", null);
	OrganizationalUnit o2o1 = new OrganizationalUnit(null, "o2o1", "o2o1", null);
	OrganizationalUnit o2o2 = new OrganizationalUnit(null, "o2o2", "o2o2", null);
	OrganizationalUnit o2o3 = new OrganizationalUnit(null, "o2o3", "o2o3", null);
	OrganizationalUnit o3o1 = new OrganizationalUnit(null, "o3o1", "o3o1", null);
	OrganizationalUnit o3o2 = new OrganizationalUnit(null, "o3o2", "o3o2", null);
	OrganizationalUnit o3o3 = new OrganizationalUnit(null, "o3o3", "o3o3", null);
	OrganizationalUnit o3o1o1 = new OrganizationalUnit(null, "o3o1o1", "o3o1o1", null);
	OrganizationalUnit o3o1o2 = new OrganizationalUnit(null, "o3o1o2", "o3o1o2", null);
	OrganizationalUnit o3o1o3 = new OrganizationalUnit(null, "o3o1o3", "o3o1o3", null);

	OrganizationalUnitHierarchy h1 = new OrganizationalUnitHierarchy(null, "AZ1 - h1", "h1", null, null, "AZ1");
	OrganizationalUnitHierarchy h2 = new OrganizationalUnitHierarchy(null, "AZ1 - h2", "h2", null, null, "AZ1");
	
	
	@Override
	public List<OrganizationalUnitHierarchy> getHierarchies() {
		List<OrganizationalUnitHierarchy> toReturn = new ArrayList<OrganizationalUnitHierarchy>();
		toReturn.add(h1);
		toReturn.add(h2);
		return toReturn;
	}

	@Override
	public List<Tree<OrganizationalUnit>> getHierarchyStructure(
			OrganizationalUnitHierarchy hierarchy) {
		
		List<Tree<OrganizationalUnit>> toReturn = new ArrayList<Tree<OrganizationalUnit>>();
		
		Node<OrganizationalUnit> rootNode = new Node<OrganizationalUnit>(root, S + root.getLabel(), null);
		Tree<OrganizationalUnit> tree = new Tree<OrganizationalUnit>(rootNode);
		
		if (hierarchy.getLabel().equals("AZ1 - h2")) {
			
			Node<OrganizationalUnit> nodeo2 = new Node<OrganizationalUnit>(o2, rootNode.getPath() + S + o2.getLabel(), rootNode);
			Node<OrganizationalUnit> nodeo2o1 = new Node<OrganizationalUnit>(o2o1, nodeo2.getPath() + S + o2o1.getLabel(), nodeo2);
			Node<OrganizationalUnit> nodeo2o2 = new Node<OrganizationalUnit>(o2o2, nodeo2.getPath() + S + o2o2.getLabel(), nodeo2);
			Node<OrganizationalUnit> nodeo2o3 = new Node<OrganizationalUnit>(o2o3, nodeo2.getPath() + S + o2o3.getLabel(), nodeo2);
			rootNode.setChildren(Arrays.asList(nodeo2));
			nodeo2.setChildren(Arrays.asList(nodeo2o1, nodeo2o2, nodeo2o3));
			
		} else {
			
			Node<OrganizationalUnit> nodeo1 = new Node<OrganizationalUnit>(o1, rootNode.getPath() + S + o1.getLabel(), rootNode);
			Node<OrganizationalUnit> nodeo2 = new Node<OrganizationalUnit>(o2, rootNode.getPath() + S + o2.getLabel(), rootNode);
			Node<OrganizationalUnit> nodeo3 = new Node<OrganizationalUnit>(o3, rootNode.getPath() + S + o3.getLabel(), rootNode);
			
			Node<OrganizationalUnit> nodeo1o1 = new Node<OrganizationalUnit>(o1o1, nodeo1.getPath() + S + o1o1.getLabel(), nodeo1);
			Node<OrganizationalUnit> nodeo1o2 = new Node<OrganizationalUnit>(o1o2, nodeo1.getPath() + S + o1o2.getLabel(), nodeo1);
			Node<OrganizationalUnit> nodeo1o3 = new Node<OrganizationalUnit>(o1o3, nodeo1.getPath() + S + o1o3.getLabel(), nodeo1);
			nodeo1.setChildren(Arrays.asList(nodeo1o1, nodeo1o2, nodeo1o3));
			
			Node<OrganizationalUnit> nodeo2o1 = new Node<OrganizationalUnit>(o2o1, nodeo2.getPath() + S + o2o1.getLabel(), nodeo2);
			Node<OrganizationalUnit> nodeo2o2 = new Node<OrganizationalUnit>(o2o2, nodeo2.getPath() + S + o2o2.getLabel(), nodeo2);
			Node<OrganizationalUnit> nodeo2o3 = new Node<OrganizationalUnit>(o2o3, nodeo2.getPath() + S + o2o3.getLabel(), nodeo2);
			nodeo2.setChildren(Arrays.asList(nodeo2o1, nodeo2o2, nodeo2o3));
			
			Node<OrganizationalUnit> nodeo3o1 = new Node<OrganizationalUnit>(o3o1, nodeo3.getPath() + S + o3o1.getLabel(), nodeo3);
			Node<OrganizationalUnit> nodeo3o2 = new Node<OrganizationalUnit>(o3o2, nodeo3.getPath() + S + o3o2.getLabel(), nodeo3);
			Node<OrganizationalUnit> nodeo3o3 = new Node<OrganizationalUnit>(o3o3, nodeo3.getPath() + S + o3o3.getLabel(), nodeo3);
			nodeo3.setChildren(Arrays.asList(nodeo3o1, nodeo3o2, nodeo3o3));
			
			Node<OrganizationalUnit> nodeo3o1o1 = new Node<OrganizationalUnit>(o3o1o1, nodeo3o1.getPath() + S + o3o1o1.getLabel(), nodeo3o1);
			Node<OrganizationalUnit> nodeo3o1o2 = new Node<OrganizationalUnit>(o3o1o2, nodeo3o1.getPath() + S + o3o1o2.getLabel(), nodeo3o1);
			Node<OrganizationalUnit> nodeo3o1o3 = new Node<OrganizationalUnit>(o3o1o3, nodeo3o1.getPath() + S + o3o1o3.getLabel(), nodeo3o1);
			nodeo3o1.setChildren(Arrays.asList(nodeo3o1o1, nodeo3o1o2, nodeo3o1o3));
			
			rootNode.setChildren(Arrays.asList(nodeo1, nodeo2, nodeo3));
		}
		
		toReturn.add(tree);
		
		return toReturn;
	}

	@Override
	public List<OrganizationalUnit> getOrganizationalUnits() {
		List<OrganizationalUnit> toReturn = new ArrayList<OrganizationalUnit>();
		toReturn.add(root);
		toReturn.add(o1);
		toReturn.add(o2);
		toReturn.add(o3);
		toReturn.add(o1o1);
		toReturn.add(o1o2);
		toReturn.add(o1o3);
		toReturn.add(o2o1);
		toReturn.add(o2o2);
		toReturn.add(o2o3);
		toReturn.add(o3o1);
		toReturn.add(o3o2);
		toReturn.add(o3o3);
		toReturn.add(o3o1o1);
		toReturn.add(o3o1o2);
		toReturn.add(o3o1o3);
		return toReturn;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

}
