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
	

<md-dialog aria-label={{title}} ng-cloack>

	<form>
		<md-dialog-content>		
			
		    <h2 class="md-title" layout-padding layout-margin> 		      
			       {{title}}
			</h2>
			    			
				
			<md-list>
				<md-list-item ng-repeat="license in licenseData" class="noright" flex>
				    <div flex=10%> Product:</div>
				    <div flex=10%> {{license.product}}</div>
				    <div flex=10%> &nbsp;Status: </div>
				    <div flex=10%> {{license.status}}</div>
				    <div flex=10%> Expiration date:</div>
				    <div flex=10%> {{license.expiration-date}}</div>
				    <div flex=10%> Other info: </div>
				    <div flex=10%> {{license.other-info}}</div>  				 	
				</<md-list-item>
			</md-list>
			
			<!-- 
			<div>
				<angular-table 
					id="licenseTable"
					flex
					columns="['expiration-date','other-info','product','status']"					
					ng-show=true
					ng-model="licenseData" 
					highlights-selected-item=true				
				>						
				</angular-table>		
			</div>   -->	
			    	
		</md-dialog-content>
		<div class="md-actions">
			<md-button ng-click="closeDialog()" >
				{{okMessage}}
        	</md-button>
     	</div>
  	</form>
</md-dialog>	
	
	
	