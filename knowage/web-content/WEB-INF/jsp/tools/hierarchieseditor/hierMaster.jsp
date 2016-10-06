<div ng-controller="hierMasterController" id="hierMasterController">
	<md-content layout="row" layout-wrap>
		<!-- Table - Left side of master tab -->		
		<div class="div-container" flex style="margin-right: 20px;" >
			<md-toolbar class="miniheadhiersmall" style="border-bottom: 2px solid grey;">
				<div class="md-toolbar-tools">
					<i class="fa fa-list"></i>
					<h2 class="md-flex" style="padding-left: 14px">{{translate.load("sbi.hierarchies.dimensions");}}</h2>
					<span flex=""></span>					
				</div>
			</md-toolbar>
			<md-content id='hierMasterDim' layout-padding layout='column' layout-align='space-around stretch'>
		        <md-content id='dimensionsSelection' layout='row' layout-wrap layout-align='space-around center' layout-sm='column' layout-align-sm='space-around stretch' style='width:96%'>
		        	<md-datepicker ng-model="dateDim" ng-change='getDimensionsTable(seeHideLeafDim,false,false,true)' md-placeholder="{{translate.load('sbi.hierarchies.date');}}" required></md-datepicker>
					<md-input-container flex flex-sm='90'>
						<label>{{translate.load("sbi.hierarchies.dimensions");}}</label>
			        	<md-select ng-model='dim' md-on-close='getDimensionsTable(seeHideLeafDim,true,true,false);getHierarchies();getFilters()'>
			        		<md-option ng-value="dim" ng-repeat="dim in dimensions">{{ dim.DIMENSION_NM }}</md-option>
			        	</md-select>
					</md-input-container>
				</md-content>
		      	<md-content layout='row' layout-align='start start'>
					<md-icon ng-if="!seeFilterDim" class="fa fa-filter" ng-click="toggleSeeFilter('dim')"></md-icon>
					<md-icon ng-if="seeFilterDim" class="fa fa-times" ng-click="toggleSeeFilter('dim')"></md-icon>
				</md-content>
				<md-content id='dimFilters' ng-show='seeFilterDim' class="force-100width">
					<fieldset class="standard" >
				        <legend>Filters</legend>
				        <div layout="column" layout-wrap flex layout-align="space-around stretch">
			        		<md-checkbox ng-model="seeHideLeafDim" style='height: 33px;' ng-disabled="!dim || !hierMaster">{{translate.load("sbi.hierarchies.show.misselement");}}</md-checkbox>
				        	<md-content ng-if="dimFilters && dimFilters.length > 0" layout="column" layout-align="start start" class="force-100width">
				        		<md-content ng-repeat="filter in dimFilters" class="force-100width">
				        			<md-content ng-if="filter.TYPE.toUpperCase() == 'DATE' " ng-init="filter.VALUE = filter.DEFAULT" class="force-100width">
				        				{{filter.NAME}}: <md-datepicker ng-model="filter.VALUE"></md-datepicker>
				        			</md-content>
				        			<md-content ng-if="filter.TYPE.toUpperCase() == 'NUMBER' " ng-init="filter.VALUE = filter.DEFAULT" class="force-100width">
				        				<md-input-container flex flex-sm='90'>
											<label>{{filter.NAME}}</label>
											<input type='number' ng-model="filter.VALUE"></input>
										</md-input-container>
				        			</md-content>
				        			<md-content ng-if="filter.TYPE.toUpperCase() == 'STRING' " ng-init="filter.VALUE = filter.DEFAULT" class="force-100width">
				        				<md-input-container flex flex-sm='90'>
											<label> {{filter.NAME}}</label>
											<input type='text'  ng-model="filter.VALUE"></input>
										</md-input-container>
				        			</md-content>
				        		</md-content>
				        	</md-content>
				        	<md-content layout='row' class="force-100width" layout-align='space-between center'>
				        		 <md-content layout="row">
					        		 <md-icon class="fa fa-check" ng-click="getDimensionsTable(seeHideLeafDim,false,false,true)"></md-icon>
							         <md-icon class="fa fa-trash" ng-click="getDimensionsTable(undedined,true,false,false)"></md-icon>
						         </md-content>
			        		</md-content>
			        	</div>
			        </fieldset>
				</md-content>
				<md-content class='force-100width' ng-show='showLoadingMaster' layout='row' layout-align ='space-around center'>
					<md-progress-circular md-mode="indeterminate"></md-progress-circular>
					<h4>{{translate.load("sbi.generic.wait");}}</h4>
				</md-content>
		      	<md-content style="min-height:32rem;max-height:32rem;" ng-if="dimensionsTable && dimensionsTable.length > 0 && !showLoadingMaster" >
		      		<angular-table flex
				        id='dimTable'
				        ng-model="dimensionsTable"
					    columns="columnsTable"
					    columns-search="columnSearchTable"
					    show-search-bar = "true"
					    drag-enabled = "true"
					    enable-clone = "true"
					    no-drop-enabled = "true"
					    no-pagination = "false"
					    show-empty-placeholder = "false"
					    highlights-selected-item="true"
					    speed-menu-option="dimSpeedMenu"
					    drag-drop-options="tableOptions"
					    class="table-object"
					>
					</angular-table>
				</md-content>
				<md-content class='' ng-show='dimensionsTable.length==0 && dim && dateDim' layout='row' layout-padding layout-align ='space-around center'>
					<h4>{{translate.load("sbi.hierarchies.nodata");}}</h4>
				</md-content> 	  	
				<md-content layout="row" layout-align="start start">	
					<md-button ng-click="createMasterHier()" ng-disabled="!dim || (!dateDim && !checkDateFilterDim(dimFilters))" class="md-raised md-ExtraMini" style="min-width:15rem;">{{translate.load("sbi.hierarchies.new.create.master");}}</md-button>
					<md-button ng-click="synchronizeMaster()" ng-disabled="dimensionsTable.length==0 ||  hierType.toUpperCase()!='MASTER' || !hierTree || hierTree.length == 0 || (!dateDim && !checkDateFilterDim(dimFilters))" class="md-raised md-ExtraMini" style="min-width:15rem;">{{translate.load("sbi.hierarchies.synchronize");}}</md-button>
				</md-content>
			</md-content>
		</div>
		
		<!-- Tree - Right side of master tab -->													    
		<div  class="div-container" flex>
			<md-toolbar class="miniheadhiersmall" style="border-bottom: 2px solid grey;" >
				<div class="md-toolbar-tools">
					<i class="fa fa-list"></i>
					<h2 class="md-flex" style="padding-left: 14px">{{translate.load("sbi.hierarchies.hierarchies");}}</h2>
					<span flex=""></span>					
				</div>
			</md-toolbar>
			<md-content id='hierMasterTree' layout-padding layout='column' layout-align='space-around stretch'>
				<md-content layout='row' layout-xs='column' layout-padding layout-align='space-around center'>
					<md-datepicker ng-model="dateTree" ng-change="getTree()"></md-datepicker>
					<md-button ng-click='saveTree()' ng-disabled='!treeDirty' class='md-raised md-ExtraMini'>{{translate.load("sbi.generic.update");}}</md-button>
					<md-checkbox ng-model="doBackup" ng-disabled='!treeDirty'>{{translate.load("sbi.hierarchies.backup");}}</md-checkbox>
				</md-content>
				<md-content id='hierarchiesSelection' layout='row' layout-wrap layout-align='space-around center' layout-sm='column' layout-align-sm='space-around stretch' style='width:96%'>
					<md-input-container flex flex-sm='90'>
				        	<label>{{translate.load("sbi.hierarchies.hierarchies");}} {{translate.load("sbi.generic.type");}}</label>
				        	<md-select type='text' ng-model='hierType' ng-disabled='!dateTree || !dim' md-on-close='getHierarchies()'>
				        		<md-option ng-value="hierType" ng-repeat="hierType in hierarchiesType">{{ hierType}}</md-option>
				        	</md-select>
						</md-input-container>
					<md-input-container flex flex-sm='90'>
			        	<label>{{translate.load("sbi.hierarchies.hierarchies");}}</label>
			        	<label ng-if=''>{{translate.load("sbi.hierarchies.hierarchies");}}</label>
			        	<md-select type='text' ng-model='hierMaster'  md-on-close='getTree()' ng-disabled='!dim || !hierType || hierarchiesMaster == undefined || hierarchiesMaster.length==0'>
			        		<md-option ng-value="hierMaster" ng-repeat="hierMaster in hierarchiesMaster">{{ hierMaster.HIER_NM	}}</md-option>
			        	</md-select>
					</md-input-container>
				</md-content>
				<md-content layout='row' layout-align='start start'>
					<md-icon ng-if="!seeFilterTree" class="fa fa-filter" ng-click="toggleSeeFilter('tree')"></md-icon>
					<md-icon ng-if="seeFilterTree" class="fa fa-times" ng-click="toggleSeeFilter('tree')"></md-icon>
				</md-content>
				<md-content id='treeFilters' ng-show='seeFilterTree' class="force-100width">
					<fieldset class="standard" >
				        <legend>Filters</legend>
				        <div layout="column" layout-wrap flex layout-align="space-around stretch">
					        <md-content layout='row' class="force-100width" layout-align='start center'>
				        		<md-checkbox ng-model="seeHideLeafTree" style='height: 33px;' ng-disabled="!hierMaster">{{translate.load("sbi.hierarchies.show.misselement");}}</md-checkbox>
					        </md-content>
					        <md-content layout='row' class="force-100width" layout-align='start center'>
				        		After Date: <md-datepicker ng-model="dateFilterTree" ng-disabled="!hierMaster"></md-datepicker>
				        	</md-content>
				        	<md-content layout='row' class="force-100width" layout-align='space-between center'>
						        <md-input-container flex='40'>
						        	<label>{{translate.load("sbi.ds.filterLabel");}}</label>
						        	<input type='text' ng-model='filterByTree' ng-disabled="!hierMaster"></input>
					      		</md-input-container>
						        <md-input-container flex='40'>
						        	<label>{{translate.load("sbi.ds.orderComboLabel");}}</label>
						        	<md-select type='text' ng-model='orderByTree' ng-disabled="!hierMaster">
						        		<md-option ng-value="orderByTree" ng-repeat="orderByTree in orderByFields">{{ orderByTree }}</md-option>
						        	</md-select>
						        </md-input-container>
				        		 <div>
					        		 <md-icon class="fa fa-check" ng-click="applyFilter('tree')"></md-icon>
							         <md-icon class="fa fa-trash" ng-click="removeFilter('tree')"></md-icon>
						         </div>
			        		</md-content>
			        	</div>
			        </fieldset>
				</md-content>
				<md-content ng-hide="showLoading || hierTree.length == 0" layout-padding>
					<document-tree id="master" 
						ng-model="hierTree"
						create-tree="false" 
						multi-select='false' 
						keys='keys' 
						text-search='filterByTreeTrigger' 
						fields-search='["name"]' 
						order-by='orderByTreeTrigger' 
						menu-option='menuOptionTree' 
						enable-drag='true' 
						no-drop-enabled='false' 
						enable-clone='true' 
						show-empty-placeholder='true'
						options-drag-drop = "treeOptions"
						>
					</document-tree>
				</md-content>
				<md-content class='force-100width' ng-show='showLoading == true' layout='row' layout-align ='space-around center'>
					<md-progress-circular md-mode="indeterminate"></md-progress-circular>
					<h4>{{translate.load("sbi.generic.wait");}}</h4>
				</md-content>
				<md-content class='' ng-show='hierTree.length==0 && !showLoading && dateTree && hierType && hierMaster' layout='row' layout-padding layout-align ='space-around center'>
					<h4>{{translate.load("sbi.hierarchies.nodata");}}</h4>
				</md-content>
			</md-content>
		</div>
	</md-content>
</div>