<angular-list-detail ng-controller="metaModelCreationBusinessController">
	<list label="translate.load('sbi.meta.businessclass')+'/'+translate.load('sbi.meta.businessview')" layout="column"> 
		 <md-content class="noMargin" >
		 	<!-- BUSINESS CLASSES -->
		 	
			<div class="metaModelBusinessList" ng-if="meta.businessModels.length>0">
				<md-subheader>{{translate.load('sbi.meta.businessclass')}}</md-subheader>
				<div ag-grid="businessClassesGrid" class="ag-theme-balham ag-noBorders ag-theme-knowage noPadding" style="width:100%;"></div>
			</div>
<!-- 				BUSINESS VIEWS -->
			<div class="metaModelBusinessList" ng-if="meta.businessViews.length>0">
				<md-subheader>{{translate.load('sbi.meta.businessview')}}</md-subheader>
				<div ag-grid="businessViewsGrid" class="ag-theme-balham ag-noBorders ag-theme-knowage noPadding" style="width:100%;"></div>
			</div>
				
		   		<!-- div class="md-dense" ng-repeat="bm in meta.businessModels" >
			   		<div class="selectable" ng-click="selectBusinessModel(bm)" layout="row" layout-align="start center" ng-class="{'selected':bm == selectedBusinessModel}" style="padding-right: 8px;">
			   			<span class="businessListName"><md-icon md-font-icon="{{::businesslModel_getlevelIcon(bm)}}"></md-icon> {{bm.name}}</span>
				   		<span flex></span>
				     	<span class="businessListProperties">{{bm.columns.length}} properties</span>
				     	<!-- md-button class="md-icon-button md-icon-button-32" ng-click="openBusinessModel(bm,$event)">
				     		<md-icon md-font-icon="fa fa-chevron-down"></md-icon>
				     	</md-button>
				     	<reorder is-first="$first" is-last="$last" up-func="moveBusinessClass($index,-1)" down-func="moveBusinessClass($index,1)"></reorder>
			   		</div>
			     	<md-divider ng-if="!$last"></md-divider>
					<!-- div ng-if="openedItems.indexOf(bm.uniqueName) !== -1">
						<md-card>
							<md-card-content class="noPadding">
								<ul>
					  				<li ng-repeat-start="col in bm.columns" class="selectable" ng-click="selectBusinessModel(col)" ng-class="{'selected':col == selectedBusinessModel}">
					  					<div layout="row">
						  					<div flex="70">
							  					<md-icon md-font-icon="{{::businesslModel_getlevelIcon(col)}}"></md-icon>
							  					<span>{{col.name}}</span>
						  					</div>
						  					<div flex="30">
									  			<md-button class="md-secondary md-icon-button" ng-if="!$first" ng-click="moveUp($index, bm)" aria-label="Move up property" >
									  				<md-icon md-font-icon="fa fa-arrow-up"></md-icon>
									  			</md-button>
									  			<md-button class="md-secondary md-icon-button" ng-if="!$last" ng-click="moveDown($index, bm)" aria-label="Move down property" >
									  				<md-icon md-font-icon="fa fa-arrow-down"></md-icon>
									  			</md-button>
						  					</div>
					  					</div>
					  				</li>
					  			<md-divider ng-repeat-end ng-if="!$last"></md-divider>
					  			</ul>
							</md-card-content>
						</md-card>
						<md-divider></md-divider>
					</div>
			   	</div>
		   	</div>
	      	
	      	<!--div class="metaModelBusinessList" ng-if="meta.businessViews.length>0">
	      		<md-subheader>{{translate.load('sbi.meta.businessview')}}</md-subheader>
		      <div class="md-dense" ng-repeat="bv in meta.businessViews" >
		      	<div class="selectable" ng-click="selectBusinessModel(bv)" layout="row" layout-align="start center" ng-class="{'selected':bv == selectedBusinessModel}">
		      		<span class="businessListName"><md-icon md-font-icon="{{::businesslModel_getlevelIcon(bv)}}"></md-icon> {{bv.name}}</span>
		      		<span flex></span>
		        	<span class="businessListProperties">{{bv.columns.length}} properties</span>
		        	<md-button class="md-icon-button md-icon-button-32" ng-click="openBusinessModel(bv,$event)">
		        		<md-icon md-font-icon="fa fa-chevron-down"></md-icon>
		        	</md-button>
		      	</div>
		        <md-divider ng-if="!$last"></md-divider>
		        <div ng-if="openedItems.indexOf(bv.uniqueName) !== -1">
		        	<md-card>
		        		<md-card-content class="noPadding">
		        			<ul>
				        		<li ng-repeat-start="col in bv.columns" class="selectable" ng-click="selectBusinessModel(col)" ng-class="{'selected':col == selectedBusinessModel}">
				        			<md-icon md-font-icon="{{::businesslModel_getlevelIcon(col)}}"></md-icon>
				        			<span>{{col.name}}</span>
				        		</li>
				        		<md-divider ng-repeat-end ng-if="!$last"></md-divider>
				        	</ul>
		        		</md-card-content>
		        	</md-card>
		        	<md-divider></md-divider>
		        </div>
		      </div-->	      	
		  </md-content>
	</list>
	
	
	
	<extra-list-button>
		<md-menu>
			<md-button class="md-fab md-mini" style="top:10px;" aria-label="Create" ng-click="$mdOpenMenu($event)">
			  <md-icon md-font-icon="fa fa-plus"></md-icon>
			</md-button>
			<md-menu-content width="4">
			  <md-menu-item>
			    <md-button ng-click="addBusinessModel()">
			    	 {{translate.load('sbi.meta.new.businessclass')}}
			    </md-button>
			  </md-menu-item>
			  
			<md-menu-item ng-if="meta.businessModels.length>0">
			  <md-button ng-click="addBusinessView()">
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
						<div layout="row" ng-repeat="prop in selectedBusinessModel.properties | filterByCategory:catProp | filterByDataType" layout-align="start center">
							<md-input-container class="md-block" flex
							ng-class=" {'md-icon-right' : (getPropertyAttributes(prop).value=='temporal dimension' || getPropertyAttributes(prop).value=='time dimension')  }"
							ng-init="getPropertyAttributes(prop).value= (getPropertyAttributes(prop).value==undefined || getPropertyAttributes(prop).value==null) ? getPropertyAttributes(prop).propertyType.defaultValue : getPropertyAttributes(prop).value"
							ng-if="getPropertyKey(prop)!='structural.sqlFilter'">
								<label>{{getPropertyAttributes(prop).propertyType.name}}</label>
								<md-select ng-model="getPropertyAttributes(prop).value" ng-if="getPropertyAttributes(prop).propertyType.admissibleValues.length!=0">
									<md-option ng-repeat="admissibleValue in getPropertyAttributes(prop).propertyType.admissibleValues | filterByProductType:prop " value="{{admissibleValue}}" >
										{{admissibleValue |format:prop}}
									</md-option>
								</md-select>
								
								
								
								<input ng-if="getPropertyAttributes(prop).propertyType.admissibleValues.length==0 
								&& getPropertyKey(prop)!='structural.attribute'
								&& getPropertyKey(prop)!='structural.sqlFilter'
								&& getPropertyKey(prop)!='behavioural.notEnabledRoles'" ng-model="getPropertyAttributes(prop).value" ng-disabled="getPropertyKey(prop)=='physical.physicaltable'">
								<!--profile attributes visibility -->
								<md-select ng-model="getPropertyAttributes(prop).value" ng-if="getPropertyKey(prop)=='structural.attribute'" >	
									<md-option value=""></md-option>						
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
								<!-- physical column name -->
							</md-input-container>
						<!-- edit temporal hierarchy button -->
							<md-button class="md-icon-button" ng-if="getPropertyAttributes(prop).value=='temporal dimension' || getPropertyAttributes(prop).value=='time dimension'" ng-click="editTemporalHierarchy()">
								<md-tooltip>{{translate.load('sbi.meta.manage.temporal.hierarchy')}}</md-tooltip>
								<md-icon md-font-icon=" fa fa-sitemap" ></md-icon>
							</md-button>

							<md-input-container class="md-block" flex ng-if="(selectedBusinessModel['physicalColumn']!=undefined) && (catProp=='physical')">
								<label >{{translate.load("sbi.meta.column.physical")}}</label>
							 	<input ng-if="(selectedBusinessModel['physicalColumn']!=undefined) && (catProp=='physical')" ng-model="selectedBusinessModel['physicalColumn'].name" disabled>
							</md-input-container>
						</div>
					</expander-box>
				</md-content>
			</md-tab>
			
			<md-tab id="attributesTab" md-active="tabResource.selectedBusinessTab=='attributesTab'" md-on-select="tabResource.selectedBusinessTab='attributesTab'" label="{{translate.load('sbi.generic.attributes')}}" ng-if="selectedBusinessModel.columns!=undefined">
				<md-content  ng-if="tabResource.selectedBusinessTab=='attributesTab'" layout="column"  layout-fill ng-controller="businessModelAttributeController">
					<!--  angular-table  id="bmAttr" ng-model="attributesList" columns="selectedBusinessModelAttributes" scope-functions="selectedBusinessModelAttributesScopeFunctions" no-pagination=true flex>
					 </angular-table-->
					 	<md-button class="md-fab md-mini"  aria-label="Create" ng-click="addUnusedColumns($event)">
			  			<md-icon md-font-icon="fa fa-plus"></md-icon>
					</md-button>
					 <div ag-grid="attributesGrid" class="ag-theme-balham ag-noBorders ag-theme-knowage noPadding" style="width:100%;"></div>
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
			
			<md-tab id="filters-Tab" md-active="tabResource.selectedBusinessTab=='sqlFilterTab'" md-on-select="tabResource.selectedBusinessTab='sqlFilterTab'" label="{{translate.load('sbi.meta.model.business.filter')}}" ng-if="selectedBusinessModel.columns!=undefined">
				<md-content ng-controller="businessModelSqlFilterController" layout layout-fill ng-if="tabResource.selectedBusinessTab=='sqlFilterTab'">					
  					<div flex ng-repeat="bmProperty in selectedBusinessModel.properties" ng-if="bmProperty['structural.sqlFilter']">	  					
						<md-input-container class="md-block">
				          	<md-icon md-menu-origin class="fa fa-question-circle">
								<md-tooltip md-direction="bottom">							
									{{translate.load("sbi.meta.model.business.filter.helpMessage")}}
								</md-tooltip>
							</md-icon>
							<textarea ng-model="bmProperty['structural.sqlFilter'].value" placeholder="SQL expression"></textarea>
						</md-input-container>
  					</div> 
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
		<!-- md-button ng-if="selectedBusinessModel.columns!=undefined" ng-click="addToCurrentBusiness()">
				Add attribute
		</md-button-->
		<md-button ng-if="selectedBusinessModel.columns!=undefined" ng-click="deleteCurrentBusiness()">
				{{translate.load('sbi.generic.delete')}}
		</md-button>
	</extra-button>
	
</angular-list-detail>

