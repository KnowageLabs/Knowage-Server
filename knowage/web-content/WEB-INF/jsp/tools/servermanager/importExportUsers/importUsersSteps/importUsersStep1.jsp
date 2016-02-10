<md-content  layout="column" layout-wrap>	
	
	<div layout="row" layout-wrap  >
		<div flex >
			<md-radio-group layout="row" ng-model="IEDConf.typeSaveUser">
		      <md-radio-button value="Override" >{{translate.load("sbi.importusers.override");}}</md-radio-button>
		      <md-radio-button value="Missing">{{translate.load("sbi.importusers.addmissing");}} </md-radio-button>
		    </md-radio-group>
		</div>
		<md-checkbox flex   style="line-height: 61px;"  ng-model="IEDConf.importPersonalFolder" aria-label="Checkbox 1">{{translate.load("sbi.impexpusers.importPersonalFolder")}}</md-checkbox>
					
		<md-input-container class="small counter"> 
			<md-button ng-click="save($event)" aria-label="upload Users" >{{translate.load("sbi.importusers.startimport");}}</md-button>
		</md-input-container>
		
	</div>
	
	<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
		<p flex="45">{{translate.load('sbi.hierarchies.source');}}</p>
		<span flex></span>
		<p flex="45">{{translate.load('sbi.modelinstances.target');}}</p>
	</md-whiteframe >
	 
	<div layout="row" flex>
			<div flex style="position: relative;" >
				 <angular-table ng-show="IEDConf.roles.exportedUser.length!=0" id='layerlist' 
					ng-model=IEDConf.roles.exportedUser
					columns='[{"label":"","name":"userId"}]'
					columnsSearch='["userId"]' 
					show-search-bar=true
					highlights-selected-item=true 
					hide-table-head=true
					menu-option=menuLayer 
					multi-select=true
					selected-item=IEDConf.roles.selectedUser
					no-pagination=true
					scope-functions=tableFunction>
					</angular-table> 
					
			</div>
			<div layout="column" layout-wrap  >
			<div flex></div>
				 	<md-button  class="md-fab md-mini"  ng-click="addUser()"><md-icon
						md-font-icon="fa fa-angle-right fa-2x"  >
					</md-icon></md-button>
				 	<md-button  class="md-fab md-mini"  ng-click="removeUser()" ><md-icon
						md-font-icon="fa fa-angle-left fa-2x" >
					</md-icon></md-button>
				<div flex></div>
				 	<md-button  class="md-fab md-mini"  ng-click="addAllUser()" ><md-icon
						md-font-icon="fa fa-angle-double-right fa-2x"  >
					</md-icon></md-button>
				 	<md-button class="md-fab md-mini"  ng-click="removeAllUser()"><md-icon
						md-font-icon="fa fa-angle-double-left fa-2x"  >
					</md-icon></md-button>
				<div flex></div>
			 </div>
			<div flex style="position: relative;" >
	
				<angular-table ng-show="IEDConf.roles.exportingUser.length!=0" id='layerlist2' 
				ng-model=IEDConf.roles.exportingUser
				columns='[{"label":"","name":"userId"}]'
				columnsSearch='["userId"]' 
				show-search-bar=true
				highlights-selected-item=true 
				menu-option=menuLayer 
				multi-select=true
				selected-item=IEDConf.roles.selectedUser
				no-pagination=true
				scope-functions=tableFunction
				hide-table-head=true>
			
				
			</div>
	</div>

</md-content>