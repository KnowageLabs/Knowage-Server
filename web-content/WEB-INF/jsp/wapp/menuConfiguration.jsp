<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.menuConf.title" />
		</td>
	</tr>
</table>

<div class="div_background">

<spagobi:treeObjects moduleName="TreeMenuModule"  htmlGeneratorClass="it.eng.spagobi.wapp.presentation.MenuConfigurationHTMLTreeGenerator" />

<br/>
<br/>
<br/>
<br/>
<br/>

</div>








