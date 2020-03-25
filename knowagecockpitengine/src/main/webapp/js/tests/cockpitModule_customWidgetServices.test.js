var customChartService;
var mockData =  {"metaData":{"totalProperty":"results","root":"rows","id":"id","fields":["recNo",{"name":"column_1","header":"QUARTER","dataIndex":"column_1","type":"string","multiValue":false},{"name":"column_2","header":"THE_DATE","dataIndex":"column_2","type":"date","subtype":"timestamp","dateFormat":"d/m/Y H:i:s.uuu","dateFormatJava":"dd/MM/yyyy HH:mm:ss.SSS","multiValue":false},{"name":"column_3","header":"STORE_ID","dataIndex":"column_3","type":"float","multiValue":false},{"name":"column_4","header":"PRODUCT_FAMILY","dataIndex":"column_4","type":"string","multiValue":false},{"name":"column_5","header":"UNIT_SALES","dataIndex":"column_5","type":"float","multiValue":false},{"name":"column_6","header":"STORE_COST","dataIndex":"column_6","type":"float","multiValue":false}],"cacheDate":"2020-03-05 10:33:36.213"},"results":3928,"rows":[{"id":1,"column_1":"Q1","column_2":"28/01/1998 00:00:00.000","column_3":"1","column_4":"Drink","column_5":"48","column_6":"38.0323"},{"id":2,"column_1":"Q2","column_2":"19/04/1998 00:00:00.000","column_3":"1","column_4":"Drink","column_5":"44","column_6":"36.6181"},{"id":3,"column_1":"Q1","column_2":"27/03/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"75","column_6":"70.7084"},{"id":4,"column_1":"Q2","column_2":"18/04/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"112","column_6":"86.7598"},{"id":5,"column_1":"Q4","column_2":"24/11/1998 00:00:00.000","column_3":"1","column_4":"Food","column_5":"303","column_6":"274.2339"},{"id":6,"column_1":"Q4","column_2":"14/11/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"80","column_6":"65.998"},{"id":7,"column_1":"Q3","column_2":"25/08/1998 00:00:00.000","column_3":"2","column_4":"Food","column_5":"36","column_6":"31.5318"},{"id":8,"column_1":"Q1","column_2":"02/03/1998 00:00:00.000","column_3":"3","column_4":"Drink","column_5":"49","column_6":"37.6628"},{"id":9,"column_1":"Q2","column_2":"05/06/1998 00:00:00.000","column_3":"3","column_4":"Drink","column_5":"31","column_6":"27.965"},{"id":10,"column_1":"Q3","column_2":"13/07/1998 00:00:00.000","column_3":"3","column_4":"Food","column_5":"220","column_6":"189.1403"}]};
beforeEach(function(){
    module(function($provide){
        $provide.service('datasetService', function(){
          this.getDatasetById= jasmine.createSpy('getDatasetById');
        });
        $provide.service('sbiModule_util', function(){
          this.findInArray = jasmine.createSpy('findInArray');
        });
        $provide.service('datastoreService', function(){
              this.datastore4 = jasmine.createSpy('datastore4');
         });
      });
  module('cockpitModule');
});

beforeEach(inject(function(datastore){
    customChartService = datastore;
}));

it('should contain same number of datastore items',function (){
    expect(customChartService.getDataArray(function(record){
        return {
            y: record.UNIT_SALES,
            name: record.QUARTER
        }
    },mockData).length).toBe(11)
 });