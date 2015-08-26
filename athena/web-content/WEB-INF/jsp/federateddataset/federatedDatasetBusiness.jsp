<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

	<!-- <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.3.0/js/material.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.3.0/css/material.css"> -->

	<script type="text/javascript" 	src="/athena/js/glossary/angular/angular.js"></script>
	<link rel="stylesheet" href="/athena/themes/glossary/css/font-awesome-4.3.0/css/font-awesome.min.css">
	<script type="text/javascript" 	src="/athena/js/glossary/angular/angular-animate.min.js"></script>
	<script type="text/javascript" src="/athena/js/glossary/angular/angular-aria.min.js"></script>
	
	<link rel="stylesheet" href="/athena/js/glossary/angular/material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="/athena/js/glossary/angular/material_0.10.0/angular-material.js"></script>
	
	
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/gestione_glossario.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/generalStyle.css">
	<link rel="stylesheet" type="text/css" href="/athena/themes/glossary/css/datasetsStyle.css">
	
	
	<script src="/athena/js/federateddataset/federatedDataset.js"></script>	
	
		
</head>
<body class="bodyStyle" ng-app="MYAPPNIKOLA">

		<div ng-controller="MyCRTL" layout="column" style="width:100%; height:100%; padding-bottom: 15px; padding-right: 15px; padding-left: 15px; padding-top: 15px;" class="contentdemoBasicUsage">
  			<md-toolbar class="minihead" style="border-left: 2px solid grey; border-top: 2px solid grey; border-right: 2px solid grey;">
    			<div class="md-toolbar-tools">
      				<h2 class="md-flex">Widget</h2>
    			</div>
 			</md-toolbar>

  			<md-content layout-padding="" style="padding: 20px;border:2px solid grey;"">
    			<div ng-show="stanje1"  layout="row" layout-wrap>
					<div flex="49" style="margin-right:20px;border:2px solid grey;"">
     					<md-toolbar class="minihead" style="border-bottom: 2px solid grey;" >
    						<div class="md-toolbar-tools">
      							<h2 class="md-flex">Avaliable datasets</h2>
    						</div>
  						</md-toolbar>
  						<md-content style="height:700px;">
    							<md-list ng-repeat="k in lista" style="border: 1px solid #ddd;" >
									<md-list-item ng-click="prebaciUNovuListu(k)">
										<i class="dragged-icon fa fa-bars fa-2x" style="padding-right:5px"></i>
											{{k.label | uppercase}}
									</md-list-item>
								</md-list>
  						</md-content>
      			</div>
      			      			  
      			<div flex="49" style="border:2px solid grey;">
     				<md-toolbar class="minihead" style="border-bottom: 2px solid grey;">
    					<div class="md-toolbar-tools">
      						<h2 class="md-flex">Selected datasets</h2>
    					</div>
  					</md-toolbar>
  					<md-content style="height:700px;">
   						<md-list ng-repeat="k in listaNova" style="border: 1px solid #ddd;">
							<md-list-item md-ink-ripple>
								<i class="dragged-icon fa fa-bars fa-2x" style="padding-right:5px"></i>
									{{k.label | uppercase}}
									<span flex=""></span>
									<i class="fa fa-times" ng-click="izbaciIzNoveListe(k)" ></i>
						</md-list-item>
						</md-list>
  					</md-content>
      			</div>
    		</div>
   	
   		<div ng-hide="stanje1">
   			<md-toolbar class="minihead" style="border-left: 2px solid grey; border-top: 2px solid grey; border-right: 2px solid grey;">
    				<div class="md-toolbar-tools">
      					<h2 class="md-flex">Associations editor</h2>
    				</div>
 				</md-toolbar>
   			<md-content  style=" padding: 5px; border: 2px solid grey; height:340px">	
   				<div ng-repeat="dataset in listaNova" >
   					<div  style="width:250px; float:left; padding: 5px;">
    					<md-toolbar  class="minihead" style="border-left: 2px solid grey; border-top: 2px solid grey; border-right: 2px solid grey;">
    						<div class="md-toolbar-tools">
      							<h2 class="md-flex">{{dataset.label | uppercase}}</h2>
    						</div>
  						</md-toolbar>
  						<md-content  style="border: 2px solid grey; height:300px;">
    						<div  ng-show="true" >
   									<md-list ng-repeat="field in dataset.metadata.fieldsMeta"   ng-click="selektuj(field,dataset)">
										<md-list-item md-ink-ripple  ng-class="{prova : field.selected }" >
												{{field.name}}
										</md-list-item>
									</md-list>
							</div>
						</md-content>
					</div>
				</div>
			</md-content>	
		</div>
	
		<div ng-hide="stanje1" style="padding-top:5px">	
			<md-toolbar  class="minihead" style="border-left: 2px solid grey; border-top: 2px solid grey; border-right: 2px solid grey;">
    			<div class="md-toolbar-tools">
      				<h2 class="md-flex">Associations List</h2>
      				<span flex=""></span>
        			<i class="fa fa-plus-circle fa-2x" ng-click="createAssociations()"></i>        			
        			
    			</div>
    			
    			
  			</md-toolbar>
  			<md-content  style="border: 2px solid grey; height:300px">
    		
    		<div style="padding: 10px">
    		<md-toolbar class="minihead" style="border-left: 2px solid grey; border-top: 2px solid grey; border-right: 2px solid grey;">
    			<div class="md-toolbar-tools">
      				<h2 class="md-flex">Relations</h2>
    					</div>
    		</md-toolbar>
    		<md-content style=" border: 2px solid grey; height:235px">
    			<div >
    				<md-list>
    					<md-list-item style="list-style: none;" ng-repeat="k in associationArray">
    						{{k}}
    						<span flex=""></span>
							<i class="fa fa-trash-o" ng-click="izbaciIzAssociatonArray(k)" ></i>
						</md-list-item>
    				</md-list>
    			</div>
    			
    		</md-content>
    					
			</md-content>
			</div>
		</div>
		
		<div ng-show="stanje1">
		<md-button class="md-raised" aria-label="Aggiungi_Attributo" style=" margin-top: 20px; float:right;" ng-click="toggle()">NEXT STEP</md-button>
		</div>
		<div ng-hide="stanje1">
		<md-button class="md-raised" aria-label="Aggiungi_Attributo" style=" margin-top: 20px;" ng-click="toggle()">BACK</md-button>
		<md-button class="md-raised" aria-label="Aggiungi_Attributo" style=" margin-top: 20px; float:right;" ng-click="showAdvanced($event)">SAVE federation</md-button>
		 		
		</div>	
  		</md-content>
	</div>

</body>
</html>

<script type="text/ng-template" id="datasetsTemp.jsp">
	<md-dialog aria-label="Fill in the dataset details and click on Save" style="width: 80%; overflow-y: visible;">
  	<form>
  		<div ng-controller="MyCRTL">
		<md-header class="md-sticky-no-effect" style="padding-top:20px; padding-left:20px;">Fill in the dataset details and click on Save</md-header>
  		<md-dialog-content class="sticky-container">
    		<div>
				<md-input-container> 
        			<label>Label</label>
        			<input ng-model='federateddataset.label'></input>
      			</md-input-container>
				
				<md-input-container> 
        			<label>Name</label>
        			<input ng-model='federateddataset.name'></input>
      			</md-input-container>
    
      			<md-input-container> 
        			<label>Description</label>
        			<input ng-model='federateddataset.description'></input>
      			</md-input-container>
      		</div>
			<div>
				
			</div>
		
  		</md-dialog-content>

  		<div class="md-actions" layout="row">
    		<span flex></span>
    		<md-button ng-click="answer('close')" class="md-primary">
     			Close
    		</md-button>
    		<md-button class="md-raised" ng-click="saveFedDataSet()" class="md-primary">
      			save
    		</md-button>
  		</div>
		</div>
  	</form>
	</md-dialog>
</script>