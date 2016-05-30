<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>

 <%
	IEngUserProfile profile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);;
		
	%>	
 <md-content layout="column">	
 				 	<angular-table flex  ng-hide="showGridView==true || ngModel.length==0 "
							id='documentListTable' ng-model=ngModel
							columns='[{"label":"Type","name":"typeCode"},{"label":"Name","name":"name"},{"label":"Author","name":"creationUser"},{"label":"Label","name":"label"}]'
							columnsSearch='["name"]' 
							show-search-bar=false
							no-pagination=true
							speed-menu-option=tableSpeedMenuOption
							highlights-selected-item="true"
							selected-item=selectedDocument
							click-function="clickDocument(item);">
						</angular-table>
					 
					
					
					<!-- Document Grid View -->
					<div layout="row"  layout-wrap ng-hide="showGridView!=true " >
					<md-card class="documentCard" ng-repeat="document in ngModel| orderBy:orderingDocumentCards" ng-class="{'md-whiteframe-15dp' : selectedDocument==document }">
			        <md-card-title>
				          <md-card-title-text>
				            <p class=" ellipsis">{{document.name}}</p>
				             <md-tooltip md-delay="1500">
				              {{document.name}}
				            </md-tooltip>
				          </md-card-title-text>
				    </md-card-title>
			        
			       
			        <div class="md-card-image document_browser_image_{{document.typeCode}}"   ng-click="clickDocument(document);" 
			         ng-if="document.previewFile==null"></div>
			        
			        <img align="center" class="preview-icon" ng-if="document.previewFile!=null"
					ng-src="{{sbiModule_config.contextName}}/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&amp;SBI_ENVIRONMENT=DOCBROWSER&amp;
					LIGHT_NAVIGATOR_DISABLED=TRUE&amp;operation=DOWNLOAD&amp;fileName={{document.previewFile}}">
			        
			        
			        
			        <md-card-actions layout="row" layout-align="space-around end">
			        <% 
						if(UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0]) ||
								UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ROLE_TYPE_DEV, new String[0])
						){
					%>
			          <md-button title="{{translate.load('sbi.documentbrowser.edit')}}" class="md-icon-button" aria-label="edit" ng-click="editDocumentAction({doc:document});">
			            <md-icon md-font-icon="fa  fa-pencil fa-2x"></md-icon>
			          </md-button>
			        <%
			        	}
			        %>	 
			          <md-button title="{{translate.load('sbi.documentbrowser.execute')}}" class="md-icon-button" aria-label="Favorite" ng-click="executeDocumentAction({doc:document});">
			            <md-icon md-font-icon="fa fa-play-circle fa-2x"></md-icon>
			          </md-button>
			        <% 
							if(UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0]) ||
								UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ROLE_TYPE_DEV, new String[0])
							){
					%>
			          <md-button title="{{translate.load('sbi.documentbrowser.clone')}}" class="md-icon-button" aria-label="Settings" ng-click="cloneDocumentAction({doc:document});">
			            <md-icon md-font-icon="fa fa-clone fa-2x"></md-icon>
			          </md-button>
			          
			          <md-button title="{{translate.load('sbi.documentbrowser.delete')}}" class="md-icon-button" aria-label="Share" ng-click="deleteDocumentAction({doc:document});">
			            <md-icon md-font-icon="fa fa-trash fa-2x"></md-icon>
			          </md-button>
			          <%
			        	}
			       	 %>	
			        </md-card-actions>
			      </md-card>
	 
					</div> 
</md-content>