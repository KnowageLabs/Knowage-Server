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
			  <md-icon md-menu-origin  md-font-icon="fa fa-plus" class="md-primary"></md-icon>
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
	
		<md-tabs flex>
			<md-tab id="propertiestab" label="{{translate.load('sbi.udp.udpList')}}">
				<md-content layout="column" ng-controller="businessModelPropertyController">
				
					<expander-box layout="column" layout-margin expanded="true" label="'Misc'" background-color="transparent" color="black" >
						<md-input-container ng-repeat="prop in businessModelMiscInfo "  >
							<label>{{prop.label}}</label>
							 <input ng-model="selectedBusinessModel[prop.name]" >
						</md-input-container>
					
					</expander-box>	
				
					<expander-box layout-margin layout="column" expanded="true" label="catProp" background-color="transparent" color="black" ng-repeat="catProp in currentBusinessModelParameterCategories">
						<md-input-container ng-repeat="prop in selectedBusinessModel.properties | filterByCategory:catProp"
						ng-init="prop.value.value= (prop.value.value==undefined || prop.value.value==null) ? prop.value.propertyType.defaultValue : prop.value.value">
							<label>{{prop.value.propertyType.name}}</label>
							<md-select ng-model="prop.value.value" ng-if="prop.value.propertyType.admissibleValues.length!=0">
								<md-option ng-repeat="admissibleValue in prop.value.propertyType.admissibleValues" value="{{admissibleValue}}" >
									{{admissibleValue}}
								</md-option>
							</md-select>
							
							<input ng-model="prop.value.value" ng-if="prop.value.propertyType.admissibleValues.length==0">
							
						</md-input-container>
					</expander-box>
				</md-content>
			</md-tab>
			
			<md-tab id="attributesTab" label="{{translate.load('sbi.generic.attributes')}}" ng-if="selectedBusinessModel.columns!=undefined">
				<md-content layout  layout-fill ng-controller="businessModelAttributeController">
					<angular-table id="bmAttr" ng-model="meta.physicalModels[selectedBusinessModel.physicalTable.physicalTableIndex].columns"
					 columns="selectedBusinessModelAttributes" scope-functions="selectedBusinessModelAttributesScopeFunctions" no-pagination=true flex>
					 	</angular-table>
<!-- 					<angular-table id="bmAttr" ng-model="selectedBusinessModel.simpleBusinessColumns" -->
<!-- 					 columns="selectedBusinessModelAttributes" scope-functions="selectedBusinessModelAttributesScopeFunctions" no-pagination=true flex> -->
<!-- 					 	</angular-table> -->
				</md-content>
				
			</md-tab>
			
			<md-tab id="calculatedColumnsTab" label="{{translate.load('sbi.meta.business.calculatedField')}}" ng-if="selectedBusinessModel.calculatedBusinessColumns!=undefined">
				<md-content layout  layout-fill ng-controller="calculatedBusinessColumnsController">
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
			
			<md-tab id="inboundTab" label="{{translate.load('sbi.meta.model.business.inbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
				<md-content layout  layout-fill ng-controller="businessModelInboundController">
					<angular-table id="inbountTable"
							    ng-model="selectedBusinessModel.relationships" 
								show-search-bar=true
								no-pagination="true"
								columns="inboundColumns"
								scope-functions="inboundFunctions"
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
			
			<md-tab id="outboundTab" label="{{translate.load('sbi.meta.model.business.outbound')}}"  ng-if="selectedBusinessModel.columns!=undefined">
				<md-content layout  layout-fill ng-controller="businessModelOutboundController">
					<angular-table id="outbountTable"
					 	ng-model="selectedBusinessModel.relationships" 
					 	columns="outboundColumns"
					 	show-search-bar=true
					 	no-pagination="true"
					 	scope-functions="outboundFunctions"
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
		
			<md-tab id="joinRelationshipTab" label="{{translate.load('sbi.meta.joinRelationships')}}" ng-if="selectedBusinessModel.joinRelationships!=undefined">
				<md-content layout="column"  layout-fill >
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
		</md-tabs>
		
	</detail>
	<extra-button ng-if="selectedBusinessModel.name!=undefined">
		<md-button ng-if="selectedBusinessModel.columns!=undefined" ng-click="deleteCurrentBusiness()">
				<md-icon md-font-icon="fa fa-trash" class=""></md-icon>
				{{translate.load('sbi.generic.delete')}}
		</md-button>
	</extra-button>
	
</angular-list-detail>

