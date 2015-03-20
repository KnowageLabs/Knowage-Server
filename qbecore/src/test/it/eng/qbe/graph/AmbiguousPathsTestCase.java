/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.graph;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.PathInspector;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.GraphPath;

public class AmbiguousPathsTestCase  extends AbstractGraphTestCase {
	

	
	
//	public void testAmbiguousPaths() {
//		
//		PathInspector pathInspector = new PathInspector(graph);
//		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> map = pathInspector.getAmbiguousEntitiesPathsMap();
//		assertEquals(map.keySet().size(), 3);
//		Iterator<IModelEntity> iter = map.keySet().iterator();
//		while(iter.hasNext()){
//			IModelEntity myMe = iter.next();
//			assertTrue(mappaths.containsKey(myMe));
//			Set<GraphPath<IModelEntity, Relationship>> paths = map.get(myMe);
//			Set<GraphPath<IModelEntity, Relationship>> paths2 = mappaths.get(myMe);
//			assertEquals(paths.size(), paths2.size());
//
//		}
//		
//		
//	}
	
	
	
//	public void testAmbiguousPaths() {
//		
//		PathInspector pathInspector = new PathInspector(graph, entities);
//		String s;
//		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> map = pathInspector.getAmbiguousEntitiesAllPathsMap();
//		//assertEquals(map.keySet().size(), 3);
//		Iterator<IModelEntity> iter = map.keySet().iterator();
//		while(iter.hasNext()){
//			IModelEntity myMe = iter.next();
//			assertTrue(mappaths.containsKey(myMe));
//			Set<GraphPath<IModelEntity, Relationship>> paths = map.get(myMe);
//			Iterator<GraphPath<IModelEntity, Relationship>> iter2 = paths.iterator();
//			int y=0;
//			while (iter2.hasNext()) {
//				GraphPath<IModelEntity, Relationship> path3 =  iter2.next();
//				List<Relationship> de = path3.getEdgeList();
//				ModelFieldPaths mp = new ModelFieldPaths(myMe.getAllFields().get(0), paths);
//				y++;
//			}
////			Set<GraphPath<IModelEntity, Relationship>> paths2 = mappaths.get(myMe);
//			
//
//		}
//	}
	
//	
//	public void testdeserializatrion() {
//		
//		PathInspector pathInspector = new PathInspector(graph, entities);
//		String s;
//		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> map = pathInspector.getAmbiguousEntitiesAllPathsMap();
//		//assertEquals(map.keySet().size(), 3);
//		Iterator<IModelEntity> iter = map.keySet().iterator();
//		while(iter.hasNext()){
//			IModelEntity myMe = iter.next();
//			assertTrue(mappaths.containsKey(myMe));
//			Set<GraphPath<IModelEntity, Relationship>> paths = map.get(myMe);
//			Iterator<GraphPath<IModelEntity, Relationship>> iter2 = paths.iterator();
//			int y=0;
//			while (iter2.hasNext()) {
//				GraphPath<IModelEntity, Relationship> path3 =  iter2.next();
//				List<Relationship> de = path3.getEdgeList();
//				ModelFieldPaths mp = new ModelFieldPaths(myMe.getAllFields().get(0), paths);
//				ModelFieldPaths deserialized = null;
//				try {
//					s = mp.getModelFieldPatsAsString();
//					deserialized = ModelFieldPaths.deserialize(s, relationShips, graph, modelStructure);
//					
//				} catch (SerializationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				y++;
//			}
////			Set<GraphPath<IModelEntity, Relationship>> paths2 = mappaths.get(myMe);
//			
//
//		}
//	}

}
