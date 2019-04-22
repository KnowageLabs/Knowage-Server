


var parameters = angular.module('parameters',['sbiModule']);

parameters.service('params_service',function(sbiModule_translate){

    this.getParamTypes = function(){
        return  [ 
    		{
    			name : sbiModule_translate.load("kn.qbe.params.string"),
    			value : "String"
    		},

    		{
    			name : sbiModule_translate.load("kn.qbe.params.number"),
    			value : "Number"
    		},

    		];;
    }

})