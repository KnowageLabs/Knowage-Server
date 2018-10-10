<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

 <%



 	String userName="";
 	String tenantName="";
	IEngUserProfile userProfile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	

	if (userProfile!=null){
		userName=(String)((UserProfile)userProfile).getUserName();
		tenantName=(String)((UserProfile)userProfile).getOrganization();
	}

	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	%>
	

<md-dialog aria-label={{title}} ng-cloack flex=50 id="licenseDialog">

	<form>
		<md-dialog-content>		
			
			<div layout="row" layout-align="space-between center">
			    <h2 class="md-title" layout-padding layout-margin> 		      
					<div>{{title}}</div>
				</h2>
					<div>
						<label ng-disabled='ngDisabled' id="upload_license" class="md-button md-knowage-theme md-fab md-mini md-primary" md-ink-ripple for="upload_license_input">
							 <i class="fa fa-plus" aria-hidden="true" style="line-height: 4rem;"></i>
						</label>
						<input  ng-disabled='ngDisabled' id="upload_license_input" type="file" class="ng-hide" onchange='angular.element(this).scope().setFile(this)'>
						<md-button ng-if="file" ng-click="uploadFile()" aria-label="menu" class="md-knowage-theme md-fab md-mini md-primary">
				            <i class="fa fa-upload" aria-hidden="true"></i>
				         </md-button>
					</div>
			</div>
				
			<md-list class="md-dense">
				<md-list-item ng-repeat="license in licenseData" class="license-item md-1-line" layout="column" layout-align="start stretch">
					<md-divider></md-divider>
					<div layout="row" layout-align="space-around center">
						<div class="md-list-item-text" layout="column" flex="40">
				            <h3> <b>{{license.product}}</b></h3>
				            <h4 ng-if="license.status.contains('INVALID')"><font color="red">{{license.status}}</font> </h4>
				            <h4 ng-if="!license.status.contains('INVALID')"><font color="green">{{license.status}}</font> </h4>
				            <p><i>{{license.expiration_date}}</i></p>
				        </div>
					    <div flex="40"> 
					    	<span ng-if="license.other_info.length > 0">{{license.other_info}}</span>
					    	<span ng-if="license.other_info.length == 0">-</span>
					    	</div>
					    <div flex="5"> 
					    	<i ng-click="dowloadFile(license)" class="fa fa-download fa-lg" aria-hidden="true"></i>
						</div>
					</div>
				</md-list-item>
			</md-list>
			
		</md-dialog-content>
		<div class="md-actions">
			<md-button ng-click="closeDialog()" >
				{{okMessage}}
        	</md-button>
     	</div>
  	</form>
</md-dialog>