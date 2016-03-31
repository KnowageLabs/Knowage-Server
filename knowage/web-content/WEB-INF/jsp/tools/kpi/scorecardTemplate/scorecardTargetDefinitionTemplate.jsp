		<md-input-container>
			 <label>Nome Obiettivo</label> 
			 <input ng-model="currentTarget.name"> 
		 </md-input-container>

		<md-input-container class="md-block"> 
			<label>Criterio di Valutazione</label> 
				<md-select ng-model="currentPerspective.criterion" ng-model-options="{trackBy: '$value.valueId'}" > 
				<md-option	ng-repeat="crit in criterionTypeList" ng-value="{{crit}}">	{{crit.translatedValueName}} </md-option> 
			</md-select> 
		</md-input-container>
		
		<md-content layout="column" class=" md-whiteframe-3dp" flex>
		<md-toolbar>
			<div class="md-toolbar-tools">
				<h2>
					<span>Elenco KPI</span>
				</h2>
				<span flex></span>
				<md-button class="md-raised" ng-click="saveTarget()" aria-label="Salva Target">
					Salva
				</md-button>
			</div>
		</md-toolbar>
		
<!-- 		 	<angular-table id='targetList' ng-model=kpisList -->
<!-- 				columns='[  {"label":"Name","name":"Nome"}, {"label":"category","name":"Categoria"}]' -->
<!-- 			 	 show-search-bar=true -->
<!-- 			 	 speed-menu-option=measureMenuOption -->
<!-- 				 click-function="measureClickFunction(item);" >  -->
<!-- 			</angular-table> -->




		</md-content>