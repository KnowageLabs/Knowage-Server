<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@page import="org.apache.log4j.Logger"%>
<%@page import="it.eng.spagobi.engines.documentcomposition.SpagoBIDocumentCompositionInternalEngine"%>
<%@page import="java.util.HashMap,java.util.List,java.util.ArrayList"%>
<% 
	logger.debug("IN");
    String urlIframe = "";
	logger.debug("urlIframe: " + urlIframe);
	String compositeJSDocumentUrl=urlBuilder.getResourceLink(request, "/js/documentcomposition/documentcomposition.js");
%> 
<!-- LIBS AJAX-->
	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/ux/miframe/miframe-min.js")%>'></script>
    <script type="text/javascript" src="<%=compositeJSDocumentUrl%>"></script>
    <script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/execution/ExecutionWizard.js")%>'></script>
<!-- ENDLIBS -->

<!-- ***************************************************************** -->
<!-- ***************************************************************** -->
<!-- **************** START BLOCK DIV ******************************** -->
<!-- ***************************************************************** -->
<!-- ***************************************************************** -->
<%
List labelDocs = new ArrayList();
for (int i=0; i<lstUrl.size(); i++){
	String styleDoc = (String)lstStyle.get("STYLE_DOC__"+i);
	String totalSbiDocLabel = (String)lstUrlParams.get("SBI_DOC_LABEL__"+(i));
	String labelDoc = "";
	if (totalSbiDocLabel != null && !totalSbiDocLabel.equals("")){
		labelDoc = totalSbiDocLabel.substring(totalSbiDocLabel.indexOf("|")+1);
		labelDocs.add(labelDoc);
	}
%>
 
 <div id="divIframe_<%=labelDoc%>" style="<%=styleDoc%>" > 
</div> 

<%} %>
<br> 
<style>
	.x-panel-header{	
		padding: 0px;
		height: 0px;
		border: none;
	}
</style>
<script>
	/* Create associative arrays with information for refresh (dependencies, ...)   ACTHUNG: Every array is indipendent!!!! */
	var arUrl = new Object();
	var arTestUrl = new Object();
	var arTitleDocs = new Object();
	var arTypeDocs = new Object();
	var arExportDocs = new Object();
	var arLinkedDocs  = new Object();
	var arLinkedFields  = new Object();
	var arLinkedCross  = new Object();
	var arStylePanels  = new Object();
	var arExportTypes = new Array();
 	<% //loop on documents
 	for (int i = 0; i < lstUrl.size(); i++){
 		String mainLabel = (String)lstDocLinked.get("MAIN_DOC_LABEL__"+(i));
 		String totalSbiDocLabel = (String)lstUrlParams.get("SBI_DOC_LABEL__"+(i));
 		String totalSbiDocType = (String)lstDocTypes.get("SBI_DOC_TYPE__"+(i));
 		if (totalSbiDocLabel != null && !totalSbiDocLabel.equals("")){
	 		String labelDoc = totalSbiDocLabel.substring(totalSbiDocLabel.indexOf("|")+1);
	 	%> 
	 		arUrl['<%=totalSbiDocLabel%>'] = ['<%=(String)lstUrl.get("URL_DOC__"+(i))%>'];
	 		arTypeDocs['<%=labelDoc%>'] = ['<%=totalSbiDocType%>'];
	 		arStylePanels['<%=labelDoc%>'] = ['<%=(String)lstStylePanel.get("STYLE__"+labelDoc)%>'];
	 		arTitleDocs['<%=labelDoc%>'] = ['<%=(String)lstTitles.get("TITLE_DOC__"+labelDoc)%>'];
	 		arExportDocs['<%=labelDoc%>'] = ['<%=(String)lstExport.get("EXPORT_DOC__"+labelDoc)%>'];
	 		var docExporters = new Array();
	 		<%
	 		List exportersForDoc = (List)lstExporterTypes.get(labelDoc);
	 		for(int j=0; j < exportersForDoc.size(); j++){
	 			String exp = (String)exportersForDoc.get(j);
	 		%>
	 			
	 			var exportType = '<%=exp%>';
	 			docExporters.push(exportType);
	 			
	 		<%
		 	}
	 		%>
	 		arExportTypes['<%=labelDoc%>'] = docExporters;
	 	<%	//loop on document linked  
			for (int j=0; j<lstFieldLinked.size(); j++){
	 			if (mainLabel != null && mainLabel.equalsIgnoreCase(labelDoc)){ 
			 		String fieldMaster =  (String)lstFieldLinked.get("SBI_LABEL_PAR_MASTER__"+i+"__"+(j));
					if (fieldMaster != null && !fieldMaster.equals("")){	
		%>
						arLinkedFields['<%="SBI_LABEL_PAR_MASTER__"+i+"__"+(j)%>'] = ['<%=fieldMaster%>'];
		<%    		}
					Integer numDocLinked = (Integer)lstFieldLinked.get("NUM_DOC_FIELD_LINKED__"+(i)+"__"+(j));
					if (numDocLinked != null){
						for (int k=0; k < numDocLinked.intValue(); k++){
			 				String field =  (String)lstFieldLinked.get("DOC_FIELD_LINKED__"+(i)+"__"+(j)+"__"+k); 
			 				if (field != null && !field.equals("")){
				 				String labelDocLinked = field.substring(0,field.indexOf("__"));
				 				field = field.substring(field.indexOf("__")+2);
				 				String label = "";
				 				String crossType = "";
				 				for(int x=0; x<lstDocLinked.size(); x++){
				 					//label = (String)lstDocLinked.get("DOC_LABEL_LINKED__"+(i)+"__"+x);
				 					label = (String)lstDocLinked.get("DOC_LABEL_LINKED__"+(i)+"__"+(j)+"__"+k);
				 					crossType = (String)lstCrossLinked.get("DOC_CROSS_LINKED__"+(i)+"__"+(j)+"__"+k);
				 					if (label != null && label.substring(label.indexOf("|")+1).equalsIgnoreCase(labelDocLinked)) break;
				 				}
				 				if (field != null && !field.equals("") ){	
					%>
									arLinkedDocs['<%=labelDoc +"__"+ i+"__"+ j+"__"+k%>'] = ['<%=label%>'];
				 					arLinkedFields['<%=label+"__"+ i + "__"+ j+"__"+k%>'] = ['<%=field%>'];
				 					arLinkedCross['<%=label+"__"+ i + "__"+ j+"__"+k%>'] = ['<%=crossType%>'];
					 <%							 					
				 		     		//adds the url of the linked doc if itsn't already presents into arUrl 
	 								//(tipical case of EXTERNAL cross on a document that doesn't present into the composite)
	 								boolean isPresents = false;
	 								for(int z=0, len=labelDocs.size(); z<len; z++){
	 									if (((String)labelDocs.get(z)).equalsIgnoreCase(label)){
	 										isPresents = true;
	 										break;
	 									}
	 								}
	 								if (!isPresents) {	 									
	 									String extUrl = DocumentCompositionUtils.getExecutionUrl(label, aSessionContainer, aRequestContainer.getServiceRequest());
	 									extUrl = extUrl.substring(extUrl.indexOf("|")+1);
	 									labelDocs.add(label);
	 								%>	 
	 									arUrl['EXT__<%=label%>'] = ['<%=extUrl%>'];
	 								<%
	 								}
				 				}
			 				}
						}
	 				}
	 			} 
	 		}
 		}
 	}
 	
 	for (int i = 0; i < lstTestUrl.size(); i++){
 		String testUrl = (String)lstTestUrl.get(i); 	
 	%>
 			arTestUrl['<%=testUrl%>'] = false;
 	<%	
	} 	
	%>  
	setDocs(arUrl, arTestUrl, arTitleDocs, arExportDocs, arExportTypes, arTypeDocs);
	setLinkedDocs(arLinkedDocs);
	setLinkedFields(arLinkedFields);
	setLinkedCross(arLinkedCross);
	setStylePanels(arStylePanels);
</script> 


<%
Integer refreshSeconds=obj.getRefreshSeconds();
if(refreshSeconds!=null && refreshSeconds.intValue()>0){
Integer refreshConvert=new Integer(refreshSeconds.intValue()*1000);
%>

<script  type="text/javascript">

function refreshpage(){
if(document.getElementById('refreshimage<%= uuid %>')){
	document.getElementById('refreshimage<%= uuid %>').click();
	}
}
</script>

 <script type="text/javascript">

    //setTimeout('window.location.reload()', <%=refreshConvert%>);
   setTimeout('javascript:refreshpage()', <%=refreshConvert%>);
   
</script>
 <%} %>






<%	logger.debug("OUT"); %> 

<!-- ***************************************************************** -->
<!-- ***************************************************************** -->
<!-- **************** END BLOCK DIV  ********************************* -->
<!-- ***************************************************************** -->
<!-- ***************************************************************** -->
