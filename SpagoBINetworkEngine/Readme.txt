FILES TO CHANGE FOR CUSTOMIZE A GENERIC ENGINE WITH SPAGO + EXT
 

- .projects : change tag name <name>
- .settings/org.eclipse.wst.common.component : change the  deploy-name into tag <wb-module deploy-name...
- \WebContent\WEB-INF\web.xml : check eventually servlet name
- \WebContent\index.jsp : customize index page 
- \WebContent\META-INF\MANIFEST.MF : customize engine name
- \WebContent\context.xml : customize engine name
- \src\log4j.properties : customize files name
- \src\it\eng\spagobi\engines\xxx : rename and customize all classes that are presents (engineConfig, ...)

- \WebContent\WEB-INF\jsp\xxx.jsp : rename and customize the startup jsp
- \WebContent\WEB-INF\jsp\commons\includeSbiXXXJS.jspf: rename and customize js include
- \WebContent\css\xxx.css : rename and customize css file when necessary