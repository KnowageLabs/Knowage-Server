
Sbi.sdk.namespace('Sbi.sdk.services');


Sbi.sdk.apply(Sbi.sdk.services, {

    services: null
    
    , baseUrl:  {
		protocol: 'http'     
		, host: 'localhost'
	    , port: '8080'
	    , contextPath: 'SpagoBI'
	    , controllerPath: 'servlet/AdapterHTTP'    
	}
    
    , initServices: function() {
        this.services = {};
        this.services.authenticate = {
            type: 'ACTION', 
            name: 'LOGIN_ACTION_WEB', 
            baseParams: {NEW_SESSION: 'TRUE'}
        };
        
     // no more called
        this.services.execute = {
            type: 'PAGE', 
            name: 'ExecuteBIObjectPage', 
            baseParams: {NEW_SESSION: 'TRUE', MODALITY: 'SINGLE_OBJECT_EXECUTION_MODALITY', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
        
        this.services.executewithext = {
            type: 'ACTION', 
            name: 'EXECUTE_DOCUMENT_ACTION', 
            baseParams: {NEW_SESSION: 'TRUE', IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS: 'true'}
        };
        
        this.services.executedataset = {
            type: 'ACTION', 
            name: 'EXECUTE_DATASET_ACTION', 
            baseParams: {NEW_SESSION: 'TRUE'}
        };
    }
    
    , setBaseUrl: function(u) {
        Sbi.sdk.apply(this.baseUrl, u || {});
    }
    
    , getServiceUrl: function(serviceName, p) {
        var urlStr = null;
        
        if(this.services === null) {
            this.initServices();
        }
        
        if(this.services[serviceName] === undefined) {
            alert('ERROR: Service [' + + '] does not exist');
        } else {
            urlStr = '';
            urlStr = this.baseUrl.protocol + '://' + this.baseUrl.host + ":" + this.baseUrl.port + '/' + this.baseUrl.contextPath + '/' + this.baseUrl.controllerPath;
            var params;
            if(this.services[serviceName].type === 'PAGE'){
            	params = {PAGE: this.services[serviceName].name};
            } else {
            	params = {ACTION_NAME: this.services[serviceName].name};            	
            }
            
            Sbi.sdk.apply(params, p || {}, this.services[serviceName].baseParams || {});
            var paramsStr = Sbi.sdk.urlEncode(params);
            urlStr += '?' + paramsStr;
        }
        
        return urlStr;
    }
});

