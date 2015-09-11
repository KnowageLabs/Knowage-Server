var app = angular.module('AIDA_GLOSSARY_HELP_ON_LINE',
		[ 'ngMaterial',  'angular_rest', 'angular_list','bread_crumb' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

app.controller('Controller', [ "translate","$filter" ,"restServices", "$q", "$scope",
		"$timeout", funzione ]);

function funzione(translate,$filter, restServices, $q, $scope, $timeout) {
	s=$scope;
	s.translate=translate;
	s.selectedWord;
	s.selectedIndex=0;
	s.storyItem=[];
//	s.data=[{title:'ciao',itemList:[{WORD:'pippo',WORD_ID:"5"},{WORD:'pippo2',WORD_ID:"11"},{WORD:'pippo3',WORD_ID:"10"},{WORD:'pippo4',WORD_ID:"10"},{WORD:'pippo5',WORD_ID:"10"},{WORD:'pippo6',WORD_ID:"10"},{WORD:'pippo7',WORD_ID:"10"},{WORD:'pippo8',WORD_ID:"10"},{WORD:'pippo9',WORD_ID:"10"}]},{title:'ciao2',itemList:[]},{title:'ciao3',itemList:[]}]
	s.data=[{}];
	s.breadControl={};
	s.type=type;
	

	s.loadWord=function(item,story){
		restServices.get("1.0/glossary", "getWord", item)
		.success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.error(translate.load("sbi.glossary.load.error"));
					} else if (data.Status == "NON OK") {
						console.error(translate.load(data.Message));
						s.selectedWord={noDataFound:translate.load(data.Message)}
					}else {
						if(story==undefined){
							s.breadControl.resetBreadCrumb();
						}
						s.breadControl.insertBread(data);
					}
				}).error(function(data, status, headers, config) {
					console.error(translate.load("sbi.glossary.load.error"));
				})
	}

	s.loadDocument=function(iddoc){

		restServices.get(
				"1.0/glossary","getDocumentInfo","DOCUMENT_ID=" + iddoc+"&DATASETWORD=true" )
				.success(
						function(data, status, headers, config) {
							console.log("loadDocumentInfo ottnuti")
							console.log(data)
							if (data.hasOwnProperty("errors")) {
								console.error("dati n on ottenuti "+data.errors);
							} else {
								
								s.data=[{type:'DOCUMENT',title:label,itemList:data.word}];
								if(datasetId!=null && datasetId!="null"){
									s.loadDataset("DATASET_ID="+datasetId,true)
								}
//								if(data.hasOwnProperty("dataset_word")){
//									s.data.push({type:'DATASET',title:data.dataset_label,itemList:data.dataset_word});
//								}
							}
						}).error(function(data, status, headers, config) {
							console.error("dati n on ottenuti "+data.errors);
						})

	}
	
	s.loadDataset=function(param,isPush){
		restServices.get(
				"1.0/glossary","getDataSetInfo",param )
				.success(
						function(data, status, headers, config) {
							console.log("loadDatasetInfo ottnuti")
							console.log(data)
							if (data.hasOwnProperty("errors")) {
								console.error("dati non ottenuti "+data.errors);
							} else {
								console.log("dataset",data)
								if(isPush!=true){
									s.data=[];
								}
								var tmp={type:'DATASET',title:data.DataSet.label,itemList:data.Word,datasetColumn:[]};
								
								
								if(data.SbiGlDataSetWlist!=undefined){
									for(var i=0;i<data.SbiGlDataSetWlist.length;i++){
										if(data.SbiGlDataSetWlist[i].word.length!=0){
											tmp.datasetColumn.push({type:'DATASET_COL',alias:data.SbiGlDataSetWlist[i].alias,word:data.SbiGlDataSetWlist[i].word});
											}
									}
								}
								s.data.push(tmp);
								
							}
						}).error(function(data, status, headers, config) {
							console.error("dati non ottenuti "+data.errors);
						})

	}
	
	
	
	if(type=="DOCUMENT"){
			console.log("loadDocumentInfo");
			s.loadDocument(value);
			console.log("datasetId",datasetId)
	}else if(type=="DATASET"){
		console.log("loadDatasetInfo");
		if(value=="null"){
			//alter the context path because this services is called also from a services with context path = athenacockpit...
		restServices.alterContextPath('athena');
		s.loadDataset("DATASET_LABEL="+label);
		}else{
		s.loadDataset("DATASET_ID="+value);
		}
	}else if(type=='WORD'){
		restServices.alterContextPath('athena');
		s.loadWord("WORD_NAME="+label);
	}
	
	s.showInfoWORD=function(item,story){
		s.loadWord("WORD_ID=" + item.WORD_ID,story);
	}
	
	


}