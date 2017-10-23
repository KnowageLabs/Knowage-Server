<%@page import="it.eng.knowage.tools.servermanager.utils.LicenseManager"%>
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
               <h2 class="md-title" layout-padding layout-margin>            
                    <div>{{title}}</div>
               </h2>
     <md-tabs md-selected="selectedIndex" md-autoselect md-dynamic-height ng-if="hosts.length>0"> 
          <md-tab ng-repeat="host in hosts" label="{{host.hostName}}">
            
            <div layout="row" layout-align="space-between center">
                 <h2 class="md-title" layout-padding layout-margin>               
                    <div></div>
                </h2>
                    <div>
                        <label ng-disabled='ngDisabled' id="upload_license" class="md-button md-knowage-theme md-fab md-mini md-primary" md-ink-ripple for="upload_license_input">
                             <i class="fa fa-plus" aria-hidden="true" style="line-height: 4rem;"></i>
                        </label>
                        <input  ng-disabled='ngDisabled' id="upload_license_input" type="file" class="ng-hide" onchange='angular.element(this).scope().setFile(this)'>
                        <md-button ng-if="file" ng-click="uploadFile(host.hostName)" aria-label="menu" class="md-knowage-theme md-fab md-mini md-primary">
                            <i class="fa fa-upload" aria-hidden="true"></i>
                         </md-button>
                    </div>
            </div>
            <div layout-align="space-between center" layout-padding layout-margin><b>Hardware Id: </b>{{host.hardwareId}}</div>
            <br>
            <md-list class="md-dense">
                <md-list-item ng-repeat="license in licenseData[host.hostName]" class="license-item md-1-line" layout="column" layout-align="start stretch">
                    <md-divider></md-divider>
                    <div layout="row" layout-align="space-around center">
                        <div class="md-list-item-text" layout="column" flex="40">
                            <h3> <b>{{license.product}}</b></h3>
                            <h4 ng-if="license.status.contains('INVALID')"><font color="red">{{license.status_ext}}</font> </h4>
                            <h4 ng-if="!license.status.contains('INVALID')"><font color="green">{{license.status_ext}}</font> </h4>
                            <p><i>{{license.expiration_date}}</i></p>
                        </div>
                        <div flex="40">
                            <span ng-if="license.other_info.length > 0 && license.other_info_type=='OK'"><font color="green">{{license.other_info}}</font></span>
                            <span ng-if="license.other_info.length > 0 && license.other_info_type=='KO'"><font color="red">{{license.other_info}}</font></span>
                            <span ng-if="license.other_info.length == 0">-</span>
                            </div>
                        <div flex="5">
                            <i ng-click="dowloadFile(license, host.hostName)" class="fa fa-download fa-lg" aria-hidden="true"></i>
                        </div>
                        <!--
                        <div flex="5"> 
                            <i ng-click="deleteFile(license, host.hostName)" class="fa fa-trash fa-lg" aria-hidden="true"></i>
                        </div>                      
                        -->
                    </div>
                </md-list-item>
            </md-list>
         
          </md-tab>
         </md-tabs> 
            
        </md-dialog-content>
        <div class="md-actions">
            <md-button ng-click="closeDialog()" >
                {{okMessage}}
            </md-button>
        </div>
    </form>
</md-dialog>