<%@page import="it.eng.knowage.tools.servermanager.utils.LicenseManager"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.SpagoBIUtilities"%>

 <%
    String userName="";
    String tenantName="";
    IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
    
    if (userProfile!=null){
        userName=(String)((UserProfile)userProfile).getUserName();
        tenantName=(String)((UserProfile)userProfile).getOrganization();
    }

    String contextName = ChannelUtilities.getSpagoBIContextName(request);
    
    String myHostName = SpagoBIUtilities.getCurrentHostName(); 
    
    int cpuNumber = Runtime.getRuntime().availableProcessors();
       
%>
    

<md-dialog aria-label={{title}} ng-cloak flex=40 id="licenseDialog">
	<md-toolbar>
	  <div class="md-toolbar-tools">
	    <h2>{{title}}</h2>
	    <h2>myHostName: <%=myHostName%> </h2>
	    <span flex></span>
	  </div>
	</md-toolbar>
    <md-dialog-content>
 	<md-tabs md-selected="selectedIndex" md-autoselect md-dynamic-height ng-if="hosts.length>0"> 
      <md-tab ng-repeat="host in hosts" label="{{host.hostName}}">
      
        <div layout="row" layout-align="center">
        	<div class="kn-info">
        		<strong>Hardware Id: </strong>{{host.hardwareId}}<br>
        		<strong>CPU Id: <%=cpuNumber%></strong>
        	</div>
        </div>
        
 	    <div class="licenseTopButtons">
            <label ng-disabled='ngDisabled' id="upload_license" class="md-knowage-theme md-button md-fab md-mini md-primary" md-ink-ripple for="upload_license_input">
                 <md-icon md-font-set="fa" md-font-icon="fa fa-plus"></md-icon>
            </label>
            <input  ng-disabled='ngDisabled' id="upload_license_input" type="file" class="ng-hide" onchange='angular.element(this).scope().setFile(this)'>
            <md-button ng-if="file && !isForUpdate" ng-click="uploadFile(host.hostName)" aria-label="menu" class="md-fab md-mini md-primary">
                <md-icon md-font-set="fa" md-font-icon="fa fa-upload"></md-icon>
             </md-button>
        </div>
        
        <md-list class="md-dense" layout="column">
	        <md-list-item flex class="md-2-line" ng-repeat="license in licenseData[host.hostName]">
	        	<img ng-src="/knowage/themes/commons/img/licenseImages/{{license.product}}.png" class="md-avatar" alt="{{license.product}}" />
	        	<div flex class="md-list-item-text">
	          		<h3>{{license.product}}</h3>
	          			
	          		<p ng-class="{'kn-success': license.status == 'LICENSE_VALID' ,'kn-danger' :license.status !== 'LICENSE_VALID'}">
	          
		          				{{license.status_ext}}
		          		
		          		<span ng-if="license.expiration_date"><br />- {{license.expiration_date}}</span>
	          		</p>
	         	</div>
	         	<div flex class="md-list-item-text">
	         		<h3>{{license.licenseId}}</h3>
	         	</div>
<!-- 	         	<md-button class="md-secondary md-icon-button" ng-click="dowloadFile(license, host.hostName)"  > -->
<!-- 		        	<md-icon md-font-set="fa" md-font-icon="fa fa-download"></md-icon> -->
<!-- 		        </md-button> -->
		        <md-menu>
			      <md-button aria-label="Open phone interactions menu" class="md-icon-button">
			    <md-icon md-font-set="fa" md-font-icon="fas fa-ellipsis-v"></md-icon>
			  </md-button>
			  <md-menu-content width="1">
			    <md-menu-item>
			        <md-icon ng-click="dowloadFile(license, host.hostName)" md-font-set="fa" md-font-icon="fa fa-download" md-menu-align-target></md-icon>
			    </md-menu-item>
			   <md-menu-item>
			   			<label ng-disabled='ngDisabled' for="upload_license_update">
                 			<md-icon ng-if="!isForUpdate" md-font-set="fa" md-font-icon="fa fa-edit"></md-icon>
            			</label>
            			<input  ng-disabled='ngDisabled' id="upload_license_update" type="file" ng-hide="true" onchange="angular.element(this).scope().setFile(this, true)">
                 		<md-icon ng-if="isForUpdate" ng-click="uploadFile(host.hostName, license, isForUpdate)"   md-font-set="fa" md-font-icon="fa fa-upload"></md-icon>
			   </md-menu-item>
			    <md-menu-item>
			        <md-icon ng-click="deleteFile(license, host.hostName)" md-font-set="fa" md-font-icon="fa fa-trash"></md-icon>
			    </md-menu-item>
			  </md-menu-content>
			</md-menu>
		        <!--  uncomment this to add the license delete md-button class="md-secondary md-icon-button" ng-click="deleteFile(license, host.hostName)" >
		        	<md-icon md-font-set="fa" md-font-icon="fa fa-trash"></md-icon>
		        </md-button -->
	         	<md-divider></md-divider>
	       </md-list-item>
        </md-list>
     
      </md-tab>
     </md-tabs> 
        
    </md-dialog-content>
    <md-dialog-actions layout="row">
      <md-button class="md-raised md-primary" ng-click="closeDialog()" >
          {{okMessage}}
      </md-button>
    </md-dialog-actions>
</md-dialog>