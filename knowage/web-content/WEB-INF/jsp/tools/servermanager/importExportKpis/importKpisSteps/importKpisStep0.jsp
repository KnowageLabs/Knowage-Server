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


<div  layout="column" layout-wrap>
						<div layout="row" layout-wrap>
			
							<div flex >
								<file-upload flex id="AssociationFileUploadImport" ng-model="IEDConf.fileImport"></file-upload>
							</div>
							
							<md-input-container class="small counter"> 
								<md-button ng-click="upload($event)" aria-label="upload KPIs"
									class="md-fab md-mini"  > <md-icon
									md-font-icon="fa fa-upload fa-2x"  >
								</md-icon> </md-button>
							</md-input-container>
							
							
								
						</div>
						 
					
					</div>
