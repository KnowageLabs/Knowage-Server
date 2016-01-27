
<!-- <md-button ng-click="nextStep()">nextStep 3</md-button> -->

<md-content layout="column" layout-wrap >
<div layout="row" layout-wrap >
<md-switch flex ng-model="overwriteMetaData" aria-label="Switch 1" ng-init="overwriteMetaData=false">
  {{translate.load('impexp.overwrite','component_impexp_messages');}} : {{ overwriteMetaData }}
  </md-switch>
	<md-button ng-click="saveMetaDataAssociation()" class="md-raised">{{translate.load('SBISet.import','component_impexp_messages');}} </md-button> 
</div>

<md-whiteframe  layout="row" layout-wrap class="sourceTargetToolbar md-whiteframe-1dp" >
<p flex="50">{{translate.load('sbi.hierarchies.source');}}</p>
<p flex="50">{{translate.load('sbi.modelinstances.target');}}</p>
</md-whiteframe >
<md-content flex layout="column" >
 
	<div layout="column" layout-wrap ng-if="IEDConf.summary.SbiLov.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.lovs','component_impexp_messages');}}</md-subheader>
     
      <md-list layout-padding class="centerText ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiLov">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.lovExp.label}}</b>
	              <h4>{{item.lovExp.name}}</h4>
	              <p>
	                {{item.lovExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.lovExist.label}}</b>
	              <h4>{{item.lovExist.name}}</h4>
	              <p>
	                {{item.lovExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list>
    </div>
    
    <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiFunctions !=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.functionalities','component_impexp_messages');}}</md-subheader>
    
       <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiFunctions">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.functExp.code}}</b>
	              <h4>{{item.functExp.name}}</h4>
	              <h4>{{item.functExp.path}}</h4>
	              <p>
	                {{item.functExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.functExist.code}}</b>
	              <h4>{{item.functExist.name}}</h4>
	              <h4>{{item.functExist.path}}</h4>
	              <p>
	                {{item.functExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list>
      </div>
     
      
    <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiEngines.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.engines','component_impexp_messages');}}</md-subheader>
    
      <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiEngines">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.engExp.name}}</b>
	              <h4>{{item.engExp.mainUrl}}</h4>
	              <p>
	                {{item.engExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.engExist.name}}</b>
	              <h4>{{item.engExist.mainUrl}}</h4>
	              <p>
	                {{item.engExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list> 
     </div>
     
     <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiChecks.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.checks','component_impexp_messages');}}</md-subheader>
    
      <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiChecks">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.checkExp.label}}</b>
	              <h4>{{item.checkExp.name}}</h4>
	              <p>
	                {{item.checkExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.checkExist.label}}</b>
	              <h4>{{item.checkExist.name}}</h4>
	              <p>
	                {{item.checkExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list> 
    </div>
    
    <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiParameters.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.parameters','component_impexp_messages');}}</md-subheader>
     
     <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiParameters">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.paramExp.label}}</b>
	              <h4>{{item.paramExp.name}}</h4>
	              <p>
	                {{item.paramExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.paramExist.label}}</b>
	              <h4>{{item.paramExist.name}}</h4>
	              <p>
	                {{item.paramExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list> 
    </div>
    
     <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiParuse.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.paruses','component_impexp_messages');}}</md-subheader>
      
      <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiParuse">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.paruseExp.label}}</b>
	              <h4>{{item.paruseExp.name}}</h4>
	              <p>
	                {{item.paruseExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.paruseExist.label}}</b>
	              <h4>{{item.paruseExist.name}}</h4>
	              <p>
	                {{item.paruseExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list> 
     </div>
     
         <div layout="column" layout-wrap ng-if="IEDConf.summary.SbiObjects.length!=0">
	<md-subheader class="md-primary">{{translate.load('Sbi.objects','component_impexp_messages');}}</md-subheader>
  
     <md-list layout-padding class="centerText  ">
        <md-list-item class="md-3-line" ng-repeat="item in  IEDConf.summary.SbiObjects">
           <div class="md-list-item-text" layout="row" layout-wrap>
              <div flex="50">
	              <b>{{item.biobjExp.label}}</b>
	              <h4>{{item.biobjExp.name}}</h4>
	              <p>
	                {{item.biobjExp.description}}
	              </p>
              </div>
              
              <div flex="50">
	              <b>{{item.biobjExist.label}}</b>
	              <h4>{{item.biobjExist.name}}</h4>
	              <p>
	                {{item.biobjExist.description}}
	              </p>
              </div>
            </div>
            <md-divider></md-divider>
        </md-list-item>
      </md-list> 
    </div>
    </md-content>

</md-content>
