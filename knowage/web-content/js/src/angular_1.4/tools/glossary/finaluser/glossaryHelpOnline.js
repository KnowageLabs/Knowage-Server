var app = angular.module('glossaryHelpOnLine',
		[ 'ngMaterial',  'sbiModule', 'angular_list','bread_crumb' ]);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

 
app.controller('Controller', 
		[ "sbiModule_translate","$filter" ,"sbiModule_restServices", "$q", "$scope", "$timeout", funzione ]);

function funzione(sbiModule_translate,$filter, sbiModule_restServices, $q, $scope, $timeout) {
	s=$scope;
	s.translate=sbiModule_translate;
	s.selectedWord;
	s.selectedIndex=0;
	s.storyItem=[];
//	s.data=[{title:'ciao',itemList:[{WORD:'pippo',WORD_ID:"5"},{WORD:'pippo2',WORD_ID:"11"},{WORD:'pippo3',WORD_ID:"10"},{WORD:'pippo4',WORD_ID:"10"},{WORD:'pippo5',WORD_ID:"10"},{WORD:'pippo6',WORD_ID:"10"},{WORD:'pippo7',WORD_ID:"10"},{WORD:'pippo8',WORD_ID:"10"},{WORD:'pippo9',WORD_ID:"10"}]},{title:'ciao2',itemList:[]},{title:'ciao3',itemList:[]}]
	s.data=[{}];
	s.breadControl={};
	s.type=type;

	s.loadWord=function(item,story){
		sbiModule_restServices.get("1.0/glossary", "getWord", item)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				console.error(sbiModule_translate.load("sbi.glossary.load.error"));
			} else if (data.Status == "NON OK") {
				console.error(sbiModule_translate.load(data.Message));
				s.selectedWord={noDataFound:sbiModule_translate.load(data.Message)}
			}else {
				if(story==undefined){
					s.breadControl.resetBreadCrumb();
				}
				s.breadControl.insertBread(data);
			}
		}).error(function(data, status, headers, config) {
			console.error("dati n on ottenuti "+data);
		});
	}

	s.loadDocument=function(iddoc){
		sbiModule_restServices.get("1.0/glossary", "getDocumentInfo", "DOCUMENT_ID=" + iddoc + "&DATASETWORD=true" )
		.success(function(data, status, headers, config) {
			console.log("loadDocumentInfo ottnuti")
			console.log(data)
			if (data.hasOwnProperty("errors")) {
				console.error("dati n on ottenuti "+data.errors);
			} else {
				s.data=[{type:'DOCUMENT',title:label,itemList:data.word}];
				if(parameter1!=null && parameter1!="null"){
					s.loadDataset("DATASET_ID="+parameter1,true)
				}
//				if(data.hasOwnProperty("dataset_word")){
//					s.data.push({type:'DATASET',title:data.dataset_label,itemList:data.dataset_word});
//				}
			}
		}).error(function(data, status, headers, config) {
			console.error("dati n on ottenuti "+data);
		});
	};

	s.loadDataset=function(param,isPush){
		sbiModule_restServices.get(
				"1.0/glossary","getDataSetInfo",param )
		.success(function(data, status, headers, config) {
			console.log("loadDatasetInfo ottnuti");
			console.log(data);
			if (data.hasOwnProperty("errors")) {
				console.error("dati non ottenuti " + data.errors);
			} else {
				console.log("dataset",data)
				if(isPush!=true){
					s.data=[];
				}
				var tmp={
						type:'DATASET',
						title: data.DataSet.name,
						itemList:data.Word,
						subItemList:[]
				};

				if(data.SbiGlDataSetWlist!=undefined){
					for(var i=0;i<data.SbiGlDataSetWlist.length;i++){
						if(data.SbiGlDataSetWlist[i].word.length!=0){
							tmp.subItemList.push({
								type: 'DATASET_COL',
								alias: data.SbiGlDataSetWlist[i].alias,
								word: data.SbiGlDataSetWlist[i].word
							});
						}
					}
				}
				s.data.push(tmp);
			}
		}).error(function(data, status, headers, config) {
			console.error("dati non ottenuti "+data);
		});
	};

	s.loadDatamart=function(param){
		sbiModule_restServices.get("1.0/glossary","getDatamartInfo",param )
		.success(function(data, status, headers, config) {
			console.log("DatamartInfo ottnuti")
			console.log(data)
			if (data.hasOwnProperty("errors")) {
				console.error("dati non ottenuti "+data.errors);
			} else {
				console.log("datamart",data)
				s.data=[{type:'BUSINESS_CLASS',title:label,itemList:data.selfItem,subItemList:[]}];

				if(data.columnItem!=undefined){
					for(var i=0;i<data.columnItem.length;i++){
						if(data.columnItem[i].word.length!=0){
							s.data[0].subItemList.push({type:'BUSINESS_CLASS_COL',alias:data.columnItem[i].name,word:data.columnItem[i].word});
						}
					}
				}
			}
		}).error(function(data, status, headers, config) {
			console.error("dati non ottenuti "+data);
		});
	};

	if(type=="DOCUMENT"){
		console.log("loadDocumentInfo");
		s.loadDocument(value);
		console.log("datasetId",parameter1);
	}else if(type=="DATASET"){
		console.log("loadDatasetInfo");
		if(value=="null"){
			//alter the context path because this services is called also from a services with context path = knowagecockpit...
			sbiModule_restServices.alterContextPath('knowage');
			s.loadDataset("DATASET_LABEL="+label);
		}else{
			s.loadDataset("DATASET_ID="+value);
		}
	}else if(type=='BUSINESS_CLASS'){
		var ite = "BUSINESS_CLASS=" + label;
		ite += "&DATAMART=" + parameter1 ;
		s.loadDatamart(ite);
	}else if(type=='WORD'){
		sbiModule_restServices.alterContextPath('knowage');
		s.loadWord("WORD_NAME="+label);
	}
	s.showInfoWORD=function(item,story){
		s.loadWord("WORD_ID=" + item.WORD_ID,story);
	};
};