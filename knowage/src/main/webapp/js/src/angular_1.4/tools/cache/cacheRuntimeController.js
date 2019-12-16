var app = angular.module('cacheManager', ['ngMaterial','ngMessages','angular_table','sbiModule','chart.js']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

app.controller('cacheRuntimeCtrl', ['sbiModule_restServices','sbiModule_translate',"$scope","$log","$mdDialog",cacheRuntimeManagerFunction]);

app.filter('filterCacheDimension', function () {
    return function (size) {
        if (isNaN(size))
            size = 0;

        if (size < 1024)
            return size.toFixed(2) + ' MB';

        size /= 1024;

        if (size < 1024)
            return '~' + size.toFixed(2) + ' GB';

        size /= 1024;

        return '~' + size.toFixed(2) + ' TB';
    };
});


function cacheRuntimeManagerFunction(sbiModule_restServices,sbiModule_translate, $scope,$log,$mdDialog)
{

//-------------------------Utility variables definition--------------------------
	var self=this;
	self.metadata=[];
	self.itemSelected=[];
	self.translate=sbiModule_translate;
	self.tableColumns=[{label:self.translate.load("cache.manager.name"),name:"name",size:"40%"},{label:self.translate.load("cache.manager.signature"),name:"signature",size:"30%"},{label:self.translate.load("cache.manager.table"),name:"table",size:"20%"},{label:self.translate.load("cache.manager.dimension"),name:"dimension",size:"100px"}];
	//self.noWriteDefaultDataSourceMess="";

//-------------------------Utility functions definition--------------------------


	self.isUndefined = function(thing)
	{
	    return (thing == undefined || thing.length ==0);
	}

	self.formatSizeUnits=function (bytes)
	{
        if      (bytes>=1073741824) {bytes=(bytes/1073741824).toFixed(2)+' GB';}
        else if (bytes>=1048576)    {bytes=(bytes/1048576).toFixed(2)+' MB';}
        else if (bytes>=1024)       {bytes=(bytes/1024).toFixed(2)+' KB';}
        else if (bytes>1)           {bytes=bytes+' bytes';}
        else if (bytes==1)          {bytes=bytes+' byte';}
        else                        {bytes='0 byte';}
        return bytes;
	}

	self.setCacheData=function()
	{
		sbiModule_restServices.get("1.0/cacheee","")
		.success(function(data)
			{
				self.data = data;
				$log.info("Cache information obtained:",data);
				self.labels = ["Available Memory"," Used Memory"];
				self.chartData = [self.data.availableMemoryPercentage,100-self.data.availableMemoryPercentage];
				self.variableEnabled=angular.copy(data.enabled);
				self.enabled=angular.copy(data.enabled);

			});
	}

	self.setCacheMetadata=function()
	{
		sbiModule_restServices.get("1.0/cacheee","meta")
		.success(function(metadata)
			{
				self.metadata = metadata;
				$log.info("Cache metadata information obtained",metadata);

			});
	}

	Array.prototype.contains = function(obj)
	{
	    var i = this.length;
	    while (i--) {
	        if (this[i] === obj) {
	            return true;
	        }
	    }
	    return false;
	}

//------------------------- Alert functions --------------------------------------

  self.showSaveSuccessfulAlert = function()
  	{
	    $mdDialog.show(
	      $mdDialog.alert()
	       .parent(angular.element(document.querySelector('#popupContainer')))
	       .clickOutsideToClose(true)
	       .title('Save operation complete!')
	       .ok('OK')
	   );
	 };

  self.showSaveFailedAlert = function()
  	{
	    $mdDialog.show(
	      $mdDialog.alert()
	       .parent(angular.element(document.querySelector('#popupContainer')))
	       .clickOutsideToClose(true)
	       .title('Save operation failed!')
	       .ok('OK')
	   );
	 };


//------------------------- Controller logic -------------------------------------

	//------- Runtime information card ----
	 self.setCacheMetadata();
	 self.setCacheData();



	//------- General Settings Card --------



	var configName="SPAGOBI.CACHE.NAMEPREFIX"
	sbiModule_restServices.get("2.0/configs","label/"+configName)
	.success(function(result)
		{
			$log.info("Configuration Resource Information obtained",result);
			$log.info("Interesting info:",self.namePrefix);
			self.variableNamePrefix=result.valueCheck;
			self.namePrefix=angular.copy(result.valueCheck);


		});

	configName="SPAGOBI.CACHE.SPACE_AVAILABLE"
	sbiModule_restServices.get("2.0/configs","label/"+configName)
	.success(function(result)
		{
			self.spaceAvailable=parseInt(result.valueCheck);
			$log.info("Configuration Resource Information obtained",result);
			$log.info("Interesting info:",self.spaceAvailable);
			self.variableSpaceAvailable=parseInt(result.valueCheck) / 1048576;


		});

	configName="SPAGOBI.CACHE.LIMIT_FOR_CLEAN"
	sbiModule_restServices.get("2.0/configs","label/"+configName)
	.success(function(result)
		{
			self.limitForClean=parseInt(result.valueCheck);
			$log.info("Configuration Resource Information obtained",result);
			$log.info("Interesting info:",self.limitForClean);
			self.variableLimitForClean=parseInt(result.valueCheck);

		});

	configName="SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN"
	sbiModule_restServices.get("2.0/configs","label/"+configName)
	.success(function(result)
		{
			self.schedulingFullClean=result.valueCheck;
			$log.info("Configuration Resource Information obtained",result);
			$log.info("Interesting info:",self.schedulingFullClean);
			self.schedulingValues=[
				{
					label: self.translate.load("cache.manager.none"),
					id: "NONE"
				},
				{
					label: self.translate.load("cache.manager.every_10_mins"),
					id: "EVERY_10_MINS"
				},
				{
					label: self.translate.load("cache.manager.every_15_mins"),
					id: "EVERY_15_MINS"
				},
				{
					label: self.translate.load("cache.manager.every_20_mins"),
					id: "EVERY_20_MINS"
				},
				{
					label: self.translate.load("cache.manager.every_30_mins"),
					id: "EVERY_30_MINS"
				},
				{
					label: self.translate.load("cache.manager.hourly"),
					id: "HOURLY"
				},
				{
					label: self.translate.load("cache.manager.daily"),
					id: "DAILY"
				},
				{
					label: self.translate.load("cache.manager.weekly"),
					id: "WEEKLY"
				},
				{
					label: self.translate.load("cache.manager.monthly"),
					id: "MONTHLY"
				},
				{
					label: self.translate.load("cache.manager.yearly"),
					id: "YEARLY"
				}];

				self.variableSchedulingFullClean=self.schedulingValues.filter(x => x.id === result.valueCheck)[0];

		});

	configName="SPAGOBI.CACHE.LIMIT_FOR_STORE"
	sbiModule_restServices.get("2.0/configs","label/"+configName)
	.success(function(result)
		{
			self.cacheLimitForStore=parseInt(result.valueCheck);
			$log.info("Configuration Resource Information obtained",result);
			$log.info("Interesting info:",self.cacheLimitForStore);
			self.variableCacheLimitForStore=parseInt(result.valueCheck);
		});

	//--
	configName="SPAGOBI.CACHE.DS_LAST_ACCESS_TTL"
		sbiModule_restServices.get("2.0/configs","label/"+configName)
		.success(function(result)
			{
				self.lastAccessTtl=parseInt(result.valueCheck);
				$log.info("Configuration Resource Information obtained",result);
				$log.info("Interesting info:",self.lastAccessTtl);
				self.variableLastAccessTtl=parseInt(result.valueCheck);

			});

	configName="SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"
		sbiModule_restServices.get("2.0/configs","label/"+configName)
		.success(function(result)
			{
				self.createAndPersistTimeout=parseInt(result.valueCheck);
				$log.info("Configuration Resource Information obtained",result);
				$log.info("Interesting info:",self.createAndPersistTimeout);
				self.variableCreateAndPersistTimeout=parseInt(result.valueCheck);

			});

	configName="SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT"
		sbiModule_restServices.get("2.0/configs","label/"+configName)
		.success(function(result)
			{
				self.sqldbCacheTimeout=parseInt(result.valueCheck);
				$log.info("Configuration Resource Information obtained",result);
				$log.info("Interesting info:",self.sqldbCacheTimeout);
				self.variableSqldbCacheTimeout=parseInt(result.valueCheck);

			});

	configName="SPAGOBI.CACHE.HAZELCAST.TIMEOUT"
		sbiModule_restServices.get("2.0/configs","label/"+configName)
		.success(function(result)
			{
				self.hazelcastTimeout=parseInt(result.valueCheck);
				$log.info("Configuration Resource Information obtained",result);
				$log.info("Interesting info:",self.hazelcastTimeout);
				self.variableHazelcastTimeout=parseInt(result.valueCheck);

			});

	configName="SPAGOBI.CACHE.HAZELCAST.LEASETIME"
		sbiModule_restServices.get("2.0/configs","label/"+configName)
		.success(function(result)
			{
				self.hazelcastLeaseTime=parseInt(result.valueCheck);
				$log.info("Configuration Resource Information obtained",result);
				$log.info("Interesting info:",self.hazelcastLeaseTime);
				self.variableHazelcastLeaseTime=parseInt(result.valueCheck);

			});



	sbiModule_restServices.get("2.0/datasources","", "type=cache")
	.success(function(result)
		{
			$log.infoObtained=result;
			$log.info("Configuration Resource Information obtained",result);


			var datasourceWriteDefaultNumber=0;
			self.filteredDataSources=new Array();
			i=0;
			for(o in result)
			{
				if(result[o].readOnly==false)
				{
					self.filteredDataSources[i]=result[o];
					i++;
					if(result[o].writeDefault)
					{
						self.selectedDataSource=result[o];
						self.variableSelectedDataSource=result[o];
						datasourceWriteDefaultNumber++;
					}

				}


			}
			if(datasourceWriteDefaultNumber==0)
			{
				//self.noWriteDefaultDataSourceMess="No write default datasources selected";
			    $mdDialog.show(
			  	      $mdDialog.alert()
			  	       .parent(angular.element(document.querySelector('#popupContainer')))
			  	       .clickOutsideToClose(true)
			  	       .title(self.translate.load('No default datasource set, cannot display cache runtime information!'))
			  	       .ok('OK')
			  	   );

			}


			$log.info("Filtered Data Sources ",self.filteredDataSources);


		});


	function save()
	{


		//--------- General settings card update ----------

		self.enabled=angular.copy(self.variableEnabled);
		self.namePrefix=angular.copy(self.variableNamePrefix);
		self.spaceAvailable=angular.copy(self.variableSpaceAvailable * 1048576);
		self.limitForClean=angular.copy(self.variableLimitForClean);
		self.cacheLimitForStore=angular.copy(self.variableCacheLimitForStore);
		self.schedulingFullClean=angular.copy(self.variableSchedulingFullClean.id);

		self.lastAccessTtl=angular.copy(self.variableLastAccessTtl);
		self.createAndPersistTimeout=angular.copy(self.variableCreateAndPersistTimeout);
		self.sqldbCacheTimeout=angular.copy(self.variableSqldbCacheTimeout);
		self.hazelcastTimeout=angular.copy(self.variableHazelcastTimeout);
		self.hazelcastLeaseTime=angular.copy(self.variableHazelcastLeaseTime);


		var sendJsonArrayObject={};
		var configurations=[];

		var obj0=new Object();
		obj0.label="SPAGOBI.CACHE.NAMEPREFIX";
		obj0.value=angular.copy(self.namePrefix);
		configurations.push(obj0);

		var obj1=new Object();
		obj1.label="SPAGOBI.CACHE.SPACE_AVAILABLE"
		obj1.value=angular.copy(self.spaceAvailable);
		configurations.push(obj1);

		var obj2=new Object();
		obj2.label="SPAGOBI.CACHE.LIMIT_FOR_CLEAN"
		obj2.value=angular.copy(self.limitForClean);
		configurations.push(obj2);

		var obj3=new Object();
		obj3.label="SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN"
		/*
		 * id is saved into value because is the key
		 * to read/write database
		 */
		obj3.value=angular.copy(self.variableSchedulingFullClean.id);
		obj3.id=angular.copy(self.variableSchedulingFullClean.id);
		configurations.push(obj3);

		var obj4=new Object();
		obj4.label="SPAGOBI.CACHE.LIMIT_FOR_STORE"
		obj4.value=angular.copy(self.cacheLimitForStore);
		configurations.push(obj4);

//--
		var obj5=new Object();
		obj5.label="SPAGOBI.CACHE.DS_LAST_ACCESS_TTL"
		obj5.value=angular.copy(self.lastAccessTtl);
		configurations.push(obj5);

		var obj6=new Object();
		obj6.label="SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"
		obj6.value=angular.copy(self.createAndPersistTimeout);
		configurations.push(obj6);

		var obj7=new Object();
		obj7.label="SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT"
		obj7.value=angular.copy(self.sqldbCacheTimeout);
		configurations.push(obj7);

		var obj8=new Object();
		obj8.label="SPAGOBI.CACHE.HAZELCAST.TIMEOUT"
		obj8.value=angular.copy(self.hazelcastTimeout);
		configurations.push(obj8);

		var obj9=new Object();
		obj9.label="SPAGOBI.CACHE.HAZELCAST.LEASETIME"
		obj9.value=angular.copy(self.hazelcastLeaseTime);
		configurations.push(obj9);

		sendJsonArrayObject.configurations=configurations;


		$log.info("Configuration to send:",sendJsonArrayObject);

		//save Configuration options
		sbiModule_restServices.put("2.0/configs", "conf",sendJsonArrayObject).success(
				function(data, status, headers, config) {
					$log.info("Form data successfully sent with REST service");

					//to update left card data UI, after successful save
					self.setCacheData();
					//to update metadata tab, after successful save (Changing cache size causes metadata deletion. Metadata deletion makes necessary to update metadata table o Manage tab )
					self.setCacheMetadata();
				});



		self.variableSelectedDataSource.writeDefault=true;
		sbiModule_restServices.put("2.0/datasources", "",self.variableSelectedDataSource)
		.success(
			function(data, status, headers, config) {
				$log.info("New target datasource data successfully sent with REST service");
				self.selectedDataSource=angular.copy(self.variableSelectedDataSource);
				self.showSaveSuccessfulAlert();
			})
		.error(
				function(data, status, headers, config)
				{
					self.showSaveFailedAlert();
			});


	}



	self.saveFunction=function ()
	{
		sbiModule_restServices.get("1.0/cacheee","remove")
		.success(function()
			{
				$log.info("Old cache removed");
				save();
			});

	}

	self.discardFunction=function ()
	{
		self.variableEnabled=angular.copy(self.enabled);
		self.variableNamePrefix=angular.copy(self.namePrefix);
		self.variableSpaceAvailable=angular.copy(self.spaceAvailable * 1048576);
		self.variableLimitForClean=angular.copy(self.limitForClean);
		self.variableCacheLimitForStore=angular.copy(self.cacheLimitForStore);
		self.variableSchedulingFullClean=angular.copy(self.schedulingFullClean.id);
		self.variableSelectedDataSource=angular.copy(self.selectedDataSource);

		self.variableLastAccessTtl=angular.copy(self.lastAccessTtl);
		self.variableCreateAndPersistTimeout=angular.copy(self.createAndPersistTimeout);
		self.variableSqldbCacheTimeout=angular.copy(self.sqldbCacheTimeout);
		self.variableHazelcastTimeout=angular.copy(self.hazelcastTimeout);
		self.variableHazelcastLeaseTime=angular.copy(self.hazelcastLeaseTime);

	}


	self.cleanAllFunction=function ()
	{
		sbiModule_restServices.delete("1.0/cacheee","")
		.success(function()
			{
				$log.info("Old cache removed");

				//to update right card metadata
				$log.info("Metadata reloading");
				self.setCacheMetadata()

				$log.info("Cache data in left card reloading");
				//to update left card data, obtained from cacheResourcces.java
				self.setCacheData();

			});

	}


	self.deleteFunction=function (row)
	{
		namesToDelete=[];
		namesToDelete.push(row.signature);
		var body={};
		body.namesArray=namesToDelete;

		sbiModule_restServices.put("1.0/cacheee","deleteItems",body)
		.success(function()
			{
				$log.info("Dataset deleted");

				//Refresh UI
				sbiModule_restServices.get("1.0/cacheee","meta")
				.success(function(metadata)
					{
						self.metadata = metadata;
						$log.info("Cache metadata information obtained",metadata);

						self.setCacheData();

					});


			})
		.error(function(data,status){
			$log.info("error");
		});

	}


}







