var app = angular.module('timespanManager', [ 'ngMaterial','angular-list-detail','sbiModule', 'angular_table', 'angular_time_picker' ]);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);





app.controller('Controller', [ "sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", behavior ]);

function behavior(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	ctrl = this; 
	ctrl.isFunction=function(item){
		return angular.isFunction(item)
	}
	$scope.translate = sbiModule_translate;
	
	var emptyTs = {
			name : "",
			type : "",
			category : "",
			definition : [],
			isnew : true
	};
	ctrl.selectedItem={};
	angular.copy(emptyTs,ctrl.selectedItem);
	
	ctrl.tsList;
	ctrl.tsCategory;
	ctrl.objCat={};
	listTimespan();
	loadCategories();
	
	ctrl.TSTableColumns=[{label:sbiModule_translate.load("sbi.generic.name") , name:"name"},{label:sbiModule_translate.load("sbi.generic.category") , name:"category",
		transformer:function(row){
			return (row==undefined || angular.equals("",row) )? "" : ctrl.objCat[row].VALUE_NM;
		},
		comparatorFunction:function(row,col){
			debugger
		}
		}]
	
	ctrl.TSTableColumnsSearch=['name',{name:"category",transformer:function(row){
		return (row.category==undefined || angular.equals("",row.category) )? "" : ctrl.objCat[row.category].VALUE_NM;
	}}]
	
	ctrl.menuTs = [
		{
			label : sbiModule_translate.load('sbi.generic.delete'),
			icon:'fa fa-trash' ,
			backgroundColor:'transparent', 
			action : function(item) {
				ctrl.deleteTimespan(item);
			}
		}, {
		label : sbiModule_translate.load('sbi.generic.clone'),
		icon:'fa fa-clone' ,
		backgroundColor:'transparent',
		visible:function(row,col){ 
			return row.type=="temporal";},
		action : function(item) {
			ctrl.nexPeriodTimespan(item);
		}
	}
	];
	
	ctrl.tsType = [{
		"label": sbiModule_translate.load('sbi.timespan.type.time'),
		"value": "time"
	},{
		"label": sbiModule_translate.load('sbi.timespan.type.temporal'),
		"value": "temporal"
	}];
	
	
	ctrl.loadTimespan = function(item) {
		sbiModule_restServices.get("1.0/timespan", "loadTimespan", "ID="+item.id+"").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("error loading timespan");
				} else {
					angular.copy(data,ctrl.selectedItem);
//					ctrl.selectedItem = data;
					ctrl.selectedItem.isnew = false;
					ctrl.resetFields();
				}
			}).error(function(data, status, headers, config) {
				console.log("Load error " + status);
		})

	}
	
	ctrl.addInterval = function(from, to) {
		if(from && to){
			if(ctrl.selectedItem.type=='temporal'){
				 var f_date = new Date(from);
				var t_date = new Date(to);
				
				if(f_date > t_date){
					var alert = $mdDialog.alert()
						.title(sbiModule_translate.load("sbi.generic.warning"))
						.content(sbiModule_translate.load("sbi.timespan.temporal.viceversa.alert"))
						.ok(sbiModule_translate.load("sbi.general.ok"));
					$mdDialog.show( alert );
					return false;
				}
				
				for(var i in ctrl.selectedItem.definition){
					var start = new Date(ctrl.selectedItem.definition[i].from.replace( /(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3') );
					var end = new Date(ctrl.selectedItem.definition[i].to.replace( /(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3') );
					
					if( f_date <= end && t_date >= start ){
						var alert = $mdDialog.alert()
							.title(sbiModule_translate.load("sbi.generic.warning"))
							.content(sbiModule_translate.load("sbi.timespan.temporal.overlap.alert"))
							.ok(sbiModule_translate.load("sbi.general.ok"));
						$mdDialog.show( alert );
						return false;
					}
				}
				
				
				var f = ('0' + f_date.getDate()).slice(-2) + '/' + ('0' + (f_date.getMonth()+1)).slice(-2) + '/' + f_date.getFullYear(); 
				var t = ('0' + t_date.getDate()).slice(-2) + '/' + ('0' + (t_date.getMonth()+1)).slice(-2) + '/' + t_date.getFullYear(); 
				var interval = { from: f, to: t };
				
				var millsDay=86400000;
				ctrl.newtimespanTableFunction.from=t_date;
				ctrl.newtimespanTableFunction.to=new Date();
				ctrl.newtimespanTableFunction.from.setTime(t_date.getTime()+millsDay);
				ctrl.newtimespanTableFunction.to.setTime(ctrl.newtimespanTableFunction.from.getTime() + t_date.getTime()-f_date.getTime()-millsDay);
				
			} else {
				
				var f_time = Date.parse('01/01/2011 '+from);
				var t_time = Date.parse('01/01/2011 '+to);
				
				if(f_time > t_time){
					var alert = $mdDialog.alert()
						.title(sbiModule_translate.load("sbi.generic.warning"))
						.content(sbiModule_translate.load("sbi.timespan.time.viceversa.alert"))
						.ok(sbiModule_translate.load("sbi.general.ok"));
					$mdDialog.show( alert );
					return false;
				}
				
				for(var i in ctrl.selectedItem.definition){
					var start = Date.parse('01/01/2011 '+ctrl.selectedItem.definition[i].from);
					var end = Date.parse('01/01/2011 '+ctrl.selectedItem.definition[i].to);
					
					if( f_time <= end && t_time >= start ){
						var alert = $mdDialog.alert()
							.title(sbiModule_translate.load("sbi.generic.warning"))
							.content(sbiModule_translate.load("sbi.timespan.time.overlap.alert"))
							.ok(sbiModule_translate.load("sbi.general.ok"));
						$mdDialog.show( alert );
						return false;
					}
				}
				
				var interval = { from: from, to: to };
				var millsHour=60*1000; 
				var st=new Date(t_time+millsHour)
				ctrl.newtimespanTableFunction.from=st.getHours()+":"+st.getMinutes();
				
				var diffTime=t_time-f_time;
				var et=new Date(t_time+millsHour+diffTime) 
				
				ctrl.newtimespanTableFunction.to=et.getHours()+":"+et.getMinutes();
				   
			}
			ctrl.selectedItem.definition.push(interval);
		}
	}

	
	ctrl.removeInterval = function(interval) {
		ctrl.selectedItem.definition.splice(ctrl.selectedItem.definition
				.indexOf(interval), 1)
	}
	
	
	ctrl.cancel = function() {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.timespan"))
		.content(sbiModule_translate.load("sbi.timespan.cancel.message"))
		.ariaLabel('Lucky day')
		.ok(sbiModule_translate.load("sbi.general.ok"))
		.cancel( sbiModule_translate.load("sbi.ds.wizard.cancel")).targetEvent(event);
		
		$mdDialog
		.show( confirm )
		.then(function() {
			angular.copy(emptyTs,ctrl.selectedItem);
			ctrl.resetFields();
		});
	}
	
	
	ctrl.newTs = function() {
		angular.copy(emptyTs,ctrl.selectedItem);
		ctrl.resetFields();
	}
	
	
	ctrl.saveTimespan = function(clonedTS) {
		if(ctrl.selectedItem.definition.length==0 && clonedTS==undefined){
			var alert = $mdDialog.alert()
				.title(sbiModule_translate.load("sbi.generic.warning"))
				.content(sbiModule_translate.load("sbi.timespan.nointerval.alert"))
				.ok(sbiModule_translate.load("sbi.general.ok"));
			$mdDialog.show( alert );
			
		} else {	
			var itemToSave=clonedTS==undefined ? ctrl.selectedItem : clonedTS;
			
		sbiModule_restServices
		.promisePost("1.0/timespan", "saveTimespan", itemToSave).then(
			function(response, status, headers, config){
				
					if(ctrl.selectedItem.isnew){
						ctrl.selectedItem.id = response.data.id;
						ctrl.selectedItem.isnew = false; 
					}  
					
					showToast(sbiModule_translate.load("sbi.timespan.save.success"));
					listTimespan();
				
			},function(response, status, headers, config) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.timespan.save.error"));  
			})
		}
	}
	
	
	ctrl.deleteTimespan = function(item) {
		var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load("sbi.timespan.delete"))
			.content(sbiModule_translate.load("sbi.timespan.delete.message"))
			.ariaLabel('delete Timespan')
			.ok(sbiModule_translate.load("sbi.generic.delete"))
			.cancel( sbiModule_translate.load("sbi.ds.wizard.cancel"));

		$mdDialog
			.show( confirm )
			.then(function() {
				sbiModule_restServices
				.remove("1.0/timespan",
						"deleteTimespan",
						"ID=" + item.id)
				.success(
						function( data, status, headers, config ) {
							if (data.hasOwnProperty( "errors" )) { 
								console.log( "delete error" );
								showToast(sbiModule_translate.load("sbi.timespan.delete.error"));
								
							} else {
								var index = ctrl.tsList.indexOf(item);
								ctrl.tsList.splice(index, 1);
								showToast(sbiModule_translate.load("sbi.timespan.delete.success"));
							}
						})
				.error(function(data, status, headers, config) {
					console.log("delete error "+status);
					showToast(sbiModule_translate.load("sbi.timespan.delete.success"));
				})
			});
	}
	
	ctrl.changeType = function() {
		ctrl.selectedItem.definition = [];
		ctrl.resetFields();
		
	}
	
	
	ctrl.resetFields = function() { 
		if(ctrl.selectedItem.type=="temporal"){
			ctrl.newtimespanTableFunction.from = new Date();
			ctrl.newtimespanTableFunction.to = new Date();
			ctrl.newtimespanTableFunction.to.setTime(ctrl.newtimespanTableFunction.to.getTime()+(24*60*60*1000));
			
		}else{  
			var st=new Date()
			ctrl.newtimespanTableFunction.from=st.getHours()+":"+st.getMinutes(); 
			st.setTime(st.getTime()+60000); 
			ctrl.newtimespanTableFunction.to=st.getHours()+":"+st.getMinutes(); 
		}
	}
	
	
	function listTimespan() {
		sbiModule_restServices.get("1.0/timespan", "listDynTimespan").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("list error");
				} else {
					console.log("list success");
					ctrl.tsList = data;
				}
			}).error(function(data, status, headers, config) {
				console.log("list error "+status);
			})
	}
	
	function loadCategories() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType",
		"DOMAIN_TYPE=TIMESPAN_CATEGORY").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("error loading category");
				} else {
					ctrl.tsCategory = data;
					for(var i=0;i<data.length;i++){
						ctrl.objCat[data[i].VALUE_ID]=data[i];
					}
				}
			}).error(function(data, status, headers, config) {
				console.log("error loading category "+status);
			})
	}
	
	function showToast(msg) {
		$mdToast.show(
				$mdToast.simple()
				.content(msg)
				.position('top')
				.action('OK')
				.highlightAction(false).hideDelay(3000));
	}
	
	
	ctrl.timespanIntervalAction=[{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',
		action : function(item) {
			ctrl.removeInterval(item);
		}
	}];
	 
	ctrl.newtimespanTableFunction={
			addInterval:function(from,to){
				ctrl.addInterval(from,to);
			},
			from : "",
			to : "",
			selectedItem:ctrl.selectedItem,
			translate:sbiModule_translate
			
	}
	
	
	ctrl.nexPeriodTimespan=function(item){
		var extendedTS={};
		angular.copy(item,extendedTS);
		extendedTS.id=undefined;
		extendedTS.isnew=true; 
		var patt = new RegExp(/.*#.*\d/ig);
		if(patt.test(extendedTS.name)){
			extendedTS.name=extendedTS.name.substring(0,extendedTS.name.length-1)+""+(parseInt(extendedTS.name[extendedTS.name.length-1])+1);
		}else{
			extendedTS.name=extendedTS.name+" #2";
		}
		
		//check if name already exist
		for(var i=0;i<ctrl.tsList.length;i++){
			if(angular.equals(ctrl.tsList[i].name,extendedTS.name)){
				var alert = $mdDialog.alert()
				.title(sbiModule_translate.load("sbi.generic.warning"))
				.content(sbiModule_translate.load("sbi.timespan.clone.already.defined"))
				.ok(sbiModule_translate.load("sbi.general.ok"));
			$mdDialog.show( alert );
			return
			}
		}
		
		
		var FirstTemp=extendedTS.definition[0];
		var f_date = new Date(FirstTemp.from);
		var t_date = new Date(FirstTemp.to);
		
		
		var millsDay=86400000;
		var tmpFT={};
		tmpFT.from=t_date;
		tmpFT.to=new Date();
		tmpFT.from.setTime(t_date.getTime()+millsDay);
		tmpFT.to.setTime(tmpFT.from.getTime() + t_date.getTime()-f_date.getTime()-millsDay);
		
		FirstTemp.from=tmpFT.from.getDate()+"/"+(tmpFT.from.getMonth()+1)+"/"+tmpFT.from.getFullYear();
		FirstTemp.to=tmpFT.to.getDate()+"/"+(tmpFT.to.getMonth()+1)+"/"+tmpFT.to.getFullYear();
		
		 extendedTS.definition=[FirstTemp];
		 ctrl.saveTimespan(extendedTS);
		
	}
}
