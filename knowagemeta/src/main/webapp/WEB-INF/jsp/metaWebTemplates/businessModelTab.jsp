<angular-list-detail ng-controller="metaModelCreationBusinessController">
	<list label="translate.load('sbi.meta.businessclass')+'/'+translate.load('sbi.meta.businessview')" layout="column"> 
		 <span ng-if="meta.businessModels.length>0">
			<component-tree id="bcmTree"  style="margin:0px" 
					ng-model="meta.businessModels"
					highlights-selected-item="true"   
					subnode-key="columns" 
					click-function="selectBusinessModel(node)"
					hide-progress=true
					not-hide-on-load = true
					is-folder-fn="businessModel_isFolder(node)"
					folder-icon-fn="businesslModel_getlevelIcon(node)"
					open-folder-icon-fn="businesslModel_getlevelIcon(node)"
					interceptor="businessModelTreeInterceptor"
					static-tree=true
					expand-on-click=false
					tree-root-name="translate.load('sbi.meta.businessclass')"
				></component-tree>
		</span>
	<span ng-if="meta.businessViews.length>0">
		<component-tree id="bvmTree"  style="margin:0px" 
				ng-model="meta.businessViews"
				highlights-selected-item="true"   
				subnode-key="columns" 
				hide-progress=true
				interceptor="businessViewTreeInterceptor"
				static-tree=true
				not-hide-on-load = true
				expand-on-click=false
				click-function="selectBusinessModel(node)"
				is-folder-fn="businessModel_isFolder(node)"
				folder-icon-fn="businesslModel_getlevelIcon(node)"
				open-folder-icon-fn="businesslModel_getlevelIcon(node)"
				tree-root-name="translate.load('sbi.meta.businessview')"
			></component-tree>
		</span>	
	</list>
	
	
	
	<extra-list-button>
		<md-menu>
			<md-button aria-label="Create" class="md-fab" ng-click="$mdOpenMenu($event)">
			  <md-icon md-menu-origin  md-font-icon="fa fa-bars" class="md-primary"></md-icon>
			</md-button>
			<md-menu-content width="4">
			  <md-menu-item>
			    <md-button ng-click="addBusinessModel()">
			      <md-icon md-font-icon="fa fa-plus" md-menu-align-target></md-icon>
			    	 {{translate.load('sbi.meta.new.businessclass')}}
			    </md-button>
			  </md-menu-item>
			  
			<md-menu-item ng-if="meta.businessModels.length>0">
			  <md-button ng-click="addBusinessView()">
			    <md-icon md-font-icon="fa fa-plus" md-menu-align-target></md-icon>
					 {{translate.load('sbi.meta.new.businessview')}}
			  </md-button>
			</md-menu-item>
			
			</md-menu-content>
	</md-menu>
		
	</extra-list-button>
	
	
	<detail label="selectedBusinessModel.name==undefined ? '' : selectedBusinessModel.name "  ng-if="selectedBusinessModel.name!=undefined" >
	
		<md-tabs flex >
			<md-tab id="propertiestab" md-active="tabResource.selectedBusinessTab=='propertiestab'" md-on-select="tabResource.selectedBusinessTab='propertiestab'"  label="{{translate.load('sbi.udp.udpList')}}">
				<md-content ng-if="tabResource.selectedBusinessTab=='propertiestab'"  layout="column" ng-controller="businessModelPropertyController">
				
					<expander-box layout="column" layout-margin expanded="true" label="'Misc'" background-color="transparent" color="black" >
						<md-input-container ng-repeat="prop in businessModelMiscInfo "  >
							<label>{{prop.label}}</label>
							 <input ng-model="selectedBusinessModel[prop.name]" >
						</md-input-container>
						
						<md-input-container ng-if="selectedBusinessModel.physicalTable!=undefined" >
							<label>{{translate.load("sbi.meta.table.physical")}}</label>
							 <input ng-model="meta.physicalModels[selectedBusinessModel.physicalTable.physicalTableIndex].name" disabled>
						</md-input-container>
					</expander-box>	
				
					<expander-box layout-margin layout="column" expanded="true" label="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentBusinessModelParameterCategories | filterByMainCategory">
						<md-input-container ng-repeat="prop in selectedBusinessModel.properties | filterByCategory:catProp"
						ng-class=" {'md-icon-right' : (getPropertyAttributes(prop).value=='temporal dimension' || getPropertyAttributes(prop).value=='time dimension')  }"
						ng-init="getPropertyAttributes(prop).value= (getPropertyAttributes(prop).value==undefined || getPropertyAttributes(prop).value==null) ? getPropertyAttributes(prop).propertyType.defaultValue : getPropertyAttributes(prop).value">
							<label>{{getPropertyAttributes(prop).propertyType.name}}</label>
							<md-select ng-model="getPropertyAttributes(prop).value" ng-if="getPropertyAttributes(prop).propertyType.admissibleValues.length!=0">
								<md-option ng-repeat="admissibleValue in getPropertyAttributes(prop).propertyType.admissibleValues | filterByProductType:prop " value="{{admissibleValue}}" >
									{{admissibleValue}}
								</md-option>
							</md-select>
							
							<input ng-if="getPropertyAttributes(prop).propertyType.admissibleValues.length==0 
							&& getPropertyKey(prop)!='structural.attribute' 
							&& getPropertyKey(prop)!='behavioural.notEnabledRoles'" ng-model="getPropertyAttributes(prop).value">
							
							<!--profile attributes visibility -->
							<md-select ng-model="getPropertyAttributes(prop).value" ng-if="getPropertyKey(prop)=='structural.attribute'" >							
								<md-option  ng-repeat="admissibleValue in sbiModule_config.profileAttributes  " value="{{admissibleValue}}" >
									{{admissibleValue}}
								</md-option>
							</md-select>
							
							<!--profile role visibility -->
							<md-select ng-model="tmpRoleVisibility" ng-if="getPropertyKey(prop)=='behavioural.notEnabledRoles'" multiple 
							 ng-init="tmpRoleVisibility=[];initRoleVisibility(tmpRoleVisibility,getPropertyAttributes(prop).value)"  
							 md-on-close="buildRoleVisibility(tmpRoleVisibility,getPropertyAttributes(prop))">		
							 
								<md-option  ng-repeat="role in sbiModule_config.avaiableRoles track by $index" value="{{role}}" >
									{{role}}
								</md-option>
							</md-select>
							
							<!-- edit temporal hierarchy button -->
							<md-icon ng-if="getPropertyAttributes(prop).value=='temporal dimension'" ng-click="editTemporalHierarchy()" md-font-icon=" fa fa-sitemap" ></md-icon>
							<md-icon ng-if="getPropertyAttributes(prop).value=='time dimension'" ng-click="editTemporalHierarchy()" md-font-icon=" fa fa-sitemap" ></md-icon>
						</md-input-container>
					</expander-box>
				</md-content>
			</md-tab>
			
			<md-tab id="attributesTab" md-active="tabResource.selectedBusinessTab=='attributesTab'" md-on-select="tabResource.selectedBusinessTab='attributesTab'" label="{{translate.load('sbi.generic.attributes')}}" ng-if="selectedBusinessModel.columns!=undefined">
				<md-content  ng-if="tabResource.selectedBusinessTab=='attributesTab'" layout  layout-fill ng-controller="businessModelAttributeController">
					<angular-table  id="bmAttr" ng-model="attributesList" columns="selectedBusinessModelAttributes" scope-functions="selectedBusinessModelAttributesScopeFunctions" no-pagination=true flex>
					 </angular-table>
				</md-content>
			</md-tab>
			
			<md-tab id="calculatedColumnsTab" md-active="tabResource.selectedBusinessTab=='calculatedColumnsTab'" md-on-select="tabResource.selectedBusinessTab='calculatedColumnsTab'" label="{{translate.load('sbi.meta.business.calculatedField')}}" ng-if="selectedBusinessModel.calculatedBusinessColumns!=undefined">
				<md-content ng-if="tabResource.selectedBusinessTab=='calculatedColumnsTab'" layout  layout-fill ng-controller="calculatedBusinessColumnsController">
					<angular-table id="bmAttrCF" ng-model="selectedBusinessModel.calculatedBusinessColumns"
					columns="selectedBusinessModelCalculatedBusinessColumns"
					scope-functions="selectedBusinessModelCalculatedBusinessColumnsScopeFunctions"
					speed-menu-option="calculatedFieldSpeedOption"
					no-pagination=true flex>
					 	<queue-table>
							<div layout="row"> 
								<span flex></span>
								<md-button type="button" class="md-knowage-theme md-raised" id="add-element" ng-click="scopeFunctions.addCalculatedField();">{{scopeFunctions.translate.load("sbi.meta.business.calculatedField.add")}}</md-button>
							</div>
						</queue-table> 
					</angular-table>
				</md-content>
				
			</md-tab>
			
			<md-tab id="inboundTab" md-active="tabResource.selectedBusinessTab=='inboundTab'" md-on-select="tabResource.selectedBusinessTab='inboundTab'" label="{{translate.load('sbi.meta.model.business.inbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
				<md-content ng-if="tabResource.selectedBusinessTab=='inboundTab'" layout  layout-fill ng-controller="businessModelInboundController">
					<angular-table id="inbountTable"
							    ng-model="selectedBusinessModel.relationships" 
								show-search-bar=true
								no-pagination="true"
								columns="inboundColumns"
								scope-functions="inboundFunctions"
								speed-menu-option=inboundActionButton
								visible-row-function="isInbound(item)">
								<queue-table>
									<div layout="row"> 
										<span flex></span>
										<md-button type="button" class="md-knowage-theme md-raised" id="add-element" ng-click="scopeFunctions.addNewInbound();">{{scopeFunctions.translate.load("sbi.general.add")}}</md-button>
									</div>
								</queue-table> 
					 </angular-table>
				</md-content>
			</md-tab>
			
			<md-tab id="outboundTab" md-active="tabResource.selectedBusinessTab=='outboundTab'" md-on-select="tabResource.selectedBusinessTab='outboundTab'" label="{{translate.load('sbi.meta.model.business.outbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
				<md-content ng-if="tabResource.selectedBusinessTab=='outboundTab'" layout  layout-fill ng-controller="businessModelOutboundController">
					<angular-table id="outbountTable"
					 	ng-model="selectedBusinessModel.relationships" 
					 	columns="outboundColumns"
					 	show-search-bar=true
					 	no-pagination="true"
					 	scope-functions="outboundFunctions"
					 	speed-menu-option=outboundActionButton
					 	visible-row-function="isOutbound(item)">
					 	<queue-table>
							<div layout="row"> 
								<span flex></span>
								<md-button type="button" class="md-knowage-theme md-raised" id="add-element" ng-click="scopeFunctions.addNewOutbound();">{{scopeFunctions.translate.load("sbi.general.add")}}</md-button>
							</div>
						</queue-table> 
					 </angular-table>
				</md-content>
			</md-tab>
		
			<md-tab id="joinRelationshipTab" md-active="tabResource.selectedBusinessTab=='joinRelationshipTab'" md-on-select="tabResource.selectedBusinessTab='joinRelationshipTab'" label="{{translate.load('sbi.meta.joinRelationships')}}" ng-if="selectedBusinessModel.joinRelationships!=undefined">
				<md-content  ng-if="tabResource.selectedBusinessTab=='joinRelationshipTab'" layout="column"  layout-fill >
					<div flex layout="column" style="overflow: auto;">
						<md-list class="md-dense noPadding"  ng-repeat="item in selectedBusinessModel.joinRelationships">
	        				<md-list-item ng-repeat="rel in item.sourceColumns"  ng-click="null" layout="row">
		        				<span flex=40>{{item.sourceTable.name}}.{{rel.name}}</span>
		        				<span flex  ><i class="fa fa-link" aria-hidden="true"></i></span>
	        					<span flex=40>{{item.destinationTable.name}}.{{item.destinationColumns[$index].name}}</span>
	        					<md-divider></md-divider>
				    		</md-list-item>
					    </md-list>
					</div>
					<div layout="row">
						<span flex> </span>
						<md-button type="button" class="md-knowage-theme md-raised"  ng-click="addBusinessView(true);">{{translate.load("sbi.meta.business.joinRelationship.edit")}}</md-button>
					</div>
							
					
				</md-content>				
				
			</md-tab>
			
			<md-tab id="physicalTablesTab" md-active="tabResource.selectedBusinessTab=='physicalTablesTab'" md-on-select="tabResource.selectedBusinessTab='physicalTablesTab'" label="{{translate.load('sbi.meta.table.physical')}}" ng-if="selectedBusinessModel.joinRelationships!=undefined">
				<md-content  ng-if="tabResource.selectedBusinessTab=='physicalTablesTab'" layout="column"  layout-fill ng-controller="bvPhisicalTablesController">
					
						<angular-table id="bvPhisicalTablesTab"
					 	ng-model="selectedBusinessModel.physicalTables" 
					 	columns="bvPhisicalTablesTabColumns"
					 	show-search-bar=true
					 	no-pagination="true"
					 	scope-functions="bvPhisicalTablesTabFunctions"
					 	speed-menu-option=bvPhisicalTablesTabActionButton >
					 	<queue-table>
							<div layout="row"> 
								<span flex></span>
								<md-button type="button" class="md-knowage-theme md-raised" id="add-element" ng-click="scopeFunctions.addNewPhysicalTable();">{{scopeFunctions.translate.load("sbi.general.add")}}</md-button>
							</div>
						</queue-table> 
					 </angular-table>	
					
				</md-content>				
				
			</md-tab>
		</md-tabs>
		
	</detail>
	<extra-button ng-if="selectedBusinessModel.name!=undefined">
		<md-button ng-if="selectedBusinessModel.columns!=undefined" ng-click="deleteCurrentBusiness()">
				<md-icon md-font-icon="fa fa-trash" class=""></md-icon>
				{{translate.load('sbi.generic.delete')}}
		</md-button>
	</extra-button>
	
</angular-list-detail>

