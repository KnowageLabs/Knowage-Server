<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<script type="text/javascript">
	function toggleHideChild(obj, tab_name){

	var objName = obj;
	var nameSuffix = objName.split("_");
	var objList = document.getElementById(tab_name);
	if(objList!=undefined && objList!=null ){
	 for(var j=0;j<objList.rows.length;j++){
			 //firefox and chrome
	 if (navigator.userAgent.indexOf("Firefox")!=-1 || navigator.userAgent.indexOf("Mozilla")!=-1){
	   var nome = objList.rows[j].id;
	  } else {
	   var nome = objList.rows[j].getAttribute('id');
	  }
	 
	  if(nome.indexOf(objName)>=0 && nome!=objName)
	   if(objList.rows[j].style.display=='none') {
	    if (navigator.userAgent.indexOf("Firefox")!=-1){
	     var actualNome = objList.rows[j].id;
	    } else {
	     var actualNome = objList.rows[j].getAttribute('id');
	    }
	    var actualSuffix = actualNome.split("_");
	    if(actualSuffix.length>nameSuffix.length && actualSuffix.length<=(nameSuffix.length+1)){
	      if (navigator.userAgent.indexOf("Firefox")!=-1){
	       objList.rows[j].style.display='table-row';
	      } else {
	       objList.rows[j].style.display='inline';
	      }
	     }
	    } else {
	     objList.rows[j].style.display='none';
	    }
	  }
	}
	}	 	
	
	function hideAllTr(tab_name){
	   var objList = document.getElementById(tab_name);
	   if(objList!=undefined && objList!=null ){
		   for(var j=0;j<objList.rows.length;j++){
			  if (navigator.userAgent.indexOf("Firefox")!=-1){
			   		var nome = objList.rows[j].id;
			  } else {
			    	var nome = objList.rows[j].getAttribute('id');
			  }
			  if(nome.indexOf("child")>0){
			    	objList.rows[j].style.display='none';
			  }
		   }
	   }
	}

</script>
<% 
//START constructing scripts to view and hide different resources
	String resDiv = "";
	String scriptDiv = "";
	String scriptViewAll = "";
	String scriptHideAll = "";
	String scriptHideOnLoad = "";
	if(resources!=null && !resources.isEmpty() && resources.size()>1){
		scriptViewAll = "<script>";
		scriptViewAll += "function viewAll(){";
		scriptHideAll = "<script>";
		scriptHideAll += "function hideAll(){";
		scriptHideOnLoad = "<script>";
		scriptDiv += "<script>";
		resDiv += "<div class='slider_header' ><ul>";
		resDiv += "<li class='arrow' id='ss'>";
		resDiv += "<a  href='javascript:viewAll();' style='margin: 0px 0px 5px 10px;' id='ViewAll_click' name='ViewAll_click' > ";
		String viewAll = msgBuilder.getMessage("sbi.kpi.viewAll", request);
		resDiv += viewAll;
		resDiv += "</a> ";
		resDiv += "</li>";
		resDiv += "<li class='arrow' id='ss'>";
		resDiv += "<a  href='javascript:hideAll();' style='margin: 0px 0px 5px 10px;' id='HideAll_click' name='HideAll_click' > ";
		String hideAll = msgBuilder.getMessage("sbi.kpi.hideAll", request);
		resDiv += hideAll;
		resDiv += "</a> ";
		resDiv += "</li>";	
		int col = 2;
		Iterator resIt = resources.iterator();
		while(resIt.hasNext()){
			col++;
			Resource r = (Resource)resIt.next();
			String resName = r.getName();
			String resDescr = r.getDescr();
			if(col>7){
				resDiv += "</ul><ul>";
			}
			resDiv += "<li class='arrow' id='ss'>";
			resDiv += "<a  href='javascript:void(0);' style='margin: 0px 0px 5px 10px;' id='"+resName+"_click' name='"+resName+"_click' > ";
			resDiv += " "+resDescr;
			resDiv += "</a> ";
			resDiv += "</li>";
			scriptDiv += "toggle('"+resName+"', '"+resName+"_click', true );\n";	
			scriptViewAll += "setVisible('"+resName+"', '"+resName+"_click', true );\n";	
			scriptHideAll += "setVisible('"+resName+"', '"+resName+"_click', false );\n";	
			scriptHideOnLoad += "hideAllTr('KPI_TABLE"+r.getId()+"');\n";
			if(col>7){
				col = 0;
			}
		}	
		 resDiv += "</ul>";
		 resDiv += "</div>";
		 scriptDiv += "</script>";
		 scriptHideAll += "}";
		 scriptHideAll += "</script>";
		 scriptViewAll += "}";
		 scriptViewAll += "</script>";
		 scriptHideOnLoad += "</script>";
	}else if(resources!=null && !resources.isEmpty() && resources.size()==1){
		 Resource r = (Resource)resources.get(0);
		 scriptHideOnLoad = "<script>";
		 scriptHideOnLoad +="hideAllTr('KPI_TABLE"+r.getId()+"');\n";
		 scriptHideOnLoad += "</script>";
	}	
	else{
		 scriptHideOnLoad = "<script>";
		 scriptHideOnLoad +="hideAllTr('KPI_TABLE');\n";
		 scriptHideOnLoad += "</script>";
	}	
//END constructing scripts to view and hide different resources
%>
<br>
 <%=scriptDiv%>
 <%=scriptViewAll%>
 <%=scriptHideAll%>
		
<%  if (options.getClosed_tree().booleanValue()){ %>
	 <%=scriptHideOnLoad%>
<%  } %>
		
 <%=resDiv%>