<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page contentType="application/json; charset=UTF-8"%>
<%
JSONArray array = new JSONArray();
int j = 1;
for (int k = 0; k < 4; k++) {
	for (int i = 1; i < 5; i++) {
		JSONObject obj = new JSONObject();
		
		obj.put("id", "id campo " + ( k * 10 + i) );
		obj.put("name", "name campo " + ( k * 10 + i) );
		obj.put("entity", "entity campo " + k);
		JSONArray choices = new JSONArray();
		for (; j % 5 != 0; j++) {
			JSONObject aChoice = new JSONObject();
			JSONArray nodes = new JSONArray();
			for (int m = 0; m < 3; m++) {
				JSONObject aNode = new JSONObject();
				aNode.put("sourceId", "id source " + j);
				aNode.put("sourceName", "name source " + j);
				aNode.put("targetId", "id target " + j);
				aNode.put("targetName", "name target " + j);
				aNode.put("relationshipId", "id relationship " + j);
				aNode.put("relationshipName", "name relationship " + j);
				nodes.put(aNode);
			}
			aChoice.put("nodes", nodes);
			if (j % 5 == 1) {
				aChoice.put("active", true);
			}
			aChoice.put("start", "start");
			aChoice.put("end", "end");
			choices.put(aChoice);
		}
		j++;
		obj.put("choices", choices);
		
		array.put(obj);
	}
}
out.print(array);
%>
<%--[]
		[
			      {
			    	  id : 'id campo 1'
			    	  , name : 'campo 1'
			    	  , entity : 'Customer'
				      , choices : [
				            {
				            	nodes : [
					    	         {source: 'source 1', target : 'target 1', relationship : 'relationship 1'}
					    	         , {source: 'source 2', target : 'target 2', relationship : 'relationship 2'}
					    	         , {source: 'source 3', target : 'target 3', relationship : 'relationship 3'} 
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'} 
				            	]
				            	, active : true
					        }
			    	  ]
			      },
			      {
			    	  id : 'id campo 2'
			    	  , name : 'campo 2'
			    	  , entity : 'Customer'
				      , choices : [
				            {
				            	nodes : [
					    	         {source: 'source 7', target : 'target 7', relationship : 'relationship 7'}
					    	         , {source: 'source 8', target : 'target 8', relationship : 'relationship 8'}
					    	         , {source: 'source 9', target : 'target 9', relationship : 'relationship 9'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 10', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 11', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 12', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 13', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 14', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 15', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            	, active : true
				            }, {
				            	nodes : [
					    	         {source: 'source 16', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 17', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 18', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }
					  ]
			      },
			      {
			    	  id : 'id campo 3'
			    	  , name : 'campo 3'
			    	  , entity : 'Product'
				      , choices : [
				            {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            	, active : true
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }
					  ]
			      },
			      {
			    	  id : 'id campo 4'
			    	  , name : 'campo 4'
			    	  , entity : 'Product'
				      , choices : [
				            {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            	, active : true
				            }, {
				            	nodes : [
					    	         {source: 'source 4', target : 'target 4', relationship : 'relationship 4'}
					    	         , {source: 'source 5', target : 'target 5', relationship : 'relationship 5'}
					    	         , {source: 'source 6', target : 'target 6', relationship : 'relationship 6'}
				            	]
				            }
					  ]
			      }
			]
--%>