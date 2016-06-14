
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	
	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%

%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	
	<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<%@include file="/WEB-INF/jsp/commons/angular/svgViewerImport.jsp"%>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/svgviewer/svgViewerController.js"></script>
	
	<script type="text/javascript">
      

var svgdoc = null;
var svgwin = null;


function testSVG(columnId,description)
{
  // FIRST WE TRY TO OBTAIN A REFERENCE TO THE SVG DOCUMENT.
  //
  // Mozilla with native SVG support and ASV running in IE both support the
  // GetSVGDocument interface for the <embed> object.
  //
  //   http://w3.org/TR/SVG11/struct.html#InterfaceGetSVGDocument
  //
  // Unfortunately ASV in IE doesn't allow us to check if the method
  // getSVGDocument() exists without calling it. Since calling a function that
  // doesn't exist will cause an error we must be prepared to catch an
  // exception.
  debugger;
  var embed = document.getElementById('svgContainer');
  try {
    svgdoc = embed.getSVGDocument();
  }
  catch(exception) {
    alert('The GetSVGDocument interface is not supported');
  }

  // If the GetSVGDocument interface is supported then the global variable
  // svgdoc will now contain a reference to the SVG document. Otherwise it will
  // be null.
  //
  // NOW WE TRY TO OBTAIN A REFERENCE TO THE SVG DOCUMENT'S "window" OBJECT.
  //
  // The W3C does not provide a direct way to obtain the "window" object for
  // the SVG document from the <embed> element (obviously, since it's not a W3C
  // tag). The W3C way to access the SVG document's "window" object is via the
  // SVG document itself, using the defaultView attribute of the DocumentView
  // interface.
  //
  //   http://www.w3.org/TR/DOM-Level-2-Views/views.html#Views-DocumentView
  //
  // ASV in IE doesn't implement the W3C's DocumentView interface (yet), so we
  // also try to access the SVG document's "window" object using the non-W3C
  // standard properties 'window' and non-W3C standard function getWindow() on
  // the <embed> object. Again ASV doesn't allow us to check whether getWindow()
  // is supported, so we need to be ready to catch an exception if calling it
  // causes an error.

  if (svgdoc && svgdoc.defaultView){  // try the W3C standard way first
    svgwin = svgdoc.defaultView;
    svgwin.setKpi('radioButtons',columnId,description);
  }
  else if (embed.window)
    svgwin = embed.window;
  else try {
    svgwin = embed.getWindow();
  }
  catch(exception) {
    alert('The DocumentView interface is not supported\r\n' +
          'Non-W3C methods of obtaining "window" also failed');
  }

  // If we failed to get a reference to the SVG document's "window" then svgwin
  // will still be null.
}

     
    </script>
	
	<title>SVG Viewer</title>
	
	</head>
	
	<body>        
        
			<div ng-app="svgViewerApp">
			    <div layout="column" ng-controller="SvgViewerController">
			        <md-sidenav md-disable-backdrop md-component-id="svgSideNav" md-is-open="isSidenavOpen" class="md-sidenav-<%= propertiesPanelPosition %>"  >
			            Side Nav opened!
			            Position: <%= propertiesPanelPosition %>
			            <input id="Unit Sales" type="button" value="clickme" onclick="testSVG('unit_sales', 'Unit Sales');" />
			            
			            <p ng-repeat="(key,prop) in measures track by $index">
			            	{{key}} {{prop.columnId}} {{prop.description}}
			            	<input id="{{key}}" type="button" value="clickme" ng-click="testSVG({{prop.columnId}},{{prop.description}})" />
			            </p>
			        </md-sidenav>
			         <md-content>
			            <md-button ng-click="openSideNav()">
			              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
			            </md-button>
			            <div id="container">
							<object ng-cloak id="svgContainer" width="100%" height="100%"
								data="${pageContext.request.contextPath}/api/1.0/svgviewer/drawMap"
								type="image/svg+xml"> Your browser does not support SVG 
							</object>
						</div>
			          </md-content>
			    </div>
			</div>        
        	
		 
		 <!-- 
		<iframe id="iframe_1"
		        name="iframe_1"
		        src='http://localhost:8080/knowagesvgviewerengine/api/1.0/svgviewer/drawMap'
		        width="100%"
		        height="100%"
		        frameborder="0"
		        style="background-color:white;">
		</iframe>
		 -->
		
		


</body>

</html>
	
	
	
	
	
    