describe("customWidgetAPI", function() {
	var customChartDatastore;
	var mockData =  {"metaData":{"totalProperty":"results","root":"rows","id":"id","fields":["recNo",{"name":"column_1","header":"QUARTER","dataIndex":"column_1","type":"string","multiValue":false},{"name":"column_2","header":"THE_DATE","dataIndex":"column_2","type":"timestamp","dateFormat":"d/m/Y H:i:s.uuu","dateFormatJava":"dd/MM/yyyy HH:mm:ss.SSS","multiValue":false},{"name":"column_3","header":"STORE_ID","dataIndex":"column_3","type":"float","multiValue":false},{"name":"column_4","header":"PRODUCT_FAMILY","dataIndex":"column_4","type":"string","multiValue":false},{"name":"column_5","header":"UNIT_SALES","dataIndex":"column_5","type":"float","multiValue":false},{"name":"column_6","header":"STORE_COST","dataIndex":"column_6","type":"float","multiValue":false}],"cacheDate":"2020-03-05 10:33:36.213"},"results":3928,"rows":[{"id":1,"column_1":"Q1","column_2":"28/01/1998 00:00:00.000","column_3":"1","column_4":"Drink","column_5":"48","column_6":"38.0323"},{"id":2,"column_1":"Q2","column_2":"19/04/1998 00:00:00.000","column_3":"1","column_4":"Drink","column_5":"44","column_6":"36.6181"},{"id":3,"column_1":"Q1","column_2":"27/03/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"75","column_6":"70.7084"},{"id":4,"column_1":"Q2","column_2":"18/04/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"112","column_6":"86.7598"},{"id":5,"column_1":"Q4","column_2":"24/11/1998 00:00:00.000","column_3":"1","column_4":"Food","column_5":"303","column_6":"274.2339"},{"id":6,"column_1":"Q4","column_2":"14/11/1998 00:00:00.000","column_3":"1","column_4":"Non-Consumable","column_5":"80","column_6":"65.998"},{"id":7,"column_1":"Q3","column_2":"25/08/1998 00:00:00.000","column_3":"2","column_4":"Food","column_5":"36","column_6":"31.5318"},{"id":8,"column_1":"Q1","column_2":"02/03/1998 00:00:00.000","column_3":"3","column_4":"Drink","column_5":"49","column_6":"37.6628"},{"id":9,"column_1":"Q2","column_2":"05/06/1998 00:00:00.000","column_3":"3","column_4":"Drink","column_5":"31","column_6":"27.965"},{"id":10,"column_1":"Q3","column_2":"13/07/1998 00:00:00.000","column_3":"3","column_4":"Food","column_5":"220","column_6":"189.1403"}]};
	beforeEach(function(){
	    module(function($provide){
	        $provide.service('datastoreService', function(){});
	      });
	  module('customWidgetAPI');
	});

	beforeEach(inject(function(datastore){
		customChartDatastore = datastore
		customChartDatastore.setData(mockData)
	}));

	it('should set data',function (){
	    expect(customChartDatastore.data.results).toBeDefined()
	});

	describe("getDataArray method", function() {

		it('should contain same number of datastore items',function (){
		    expect(customChartDatastore.getDataArray(function(record){
		        return {
		            y: record.UNIT_SALES,
		            name: record.QUARTER
		        }
		    }).length).toBe(mockData.rows.length)
		 });
	});

	it('should contain columns "QUARTER" list',function (){
	    expect(customChartDatastore.sort('QUARTER').getColumn('QUARTER')).toEqual(["Q1","Q2","Q3","Q4"])
	});

	describe("filter method", function() {
		it('should leave selected datastore objects with a single filter',function (){
			var temp = customChartDatastore.filter({'QUARTER':'Q1'}).getDataArray(function(record){
		        return record.QUARTER
			})
			expect(temp.indexOf('Q1')).not.toEqual(-1)
		    expect(temp.indexOf('Q2')).toEqual(-1)
		});

		it('should leave selected datastore objects with multiple filters',function (){
			var temp = customChartDatastore.filter({'QUARTER':'Q1','STORE_ID':'1'}).getDataArray(function(record){
		        return {
		        	quarter: record.QUARTER,
		        	id: record.STORE_ID
		        }
			})
			expect(temp.length).toEqual(2)
		    expect(temp[0].id).toEqual('1')
		});
	})

	describe("sort method", function() {
		it('should sort datastore array of object correctly',function (){
			var temp = customChartDatastore.sort('STORE_ID').getDataArray(function(record){
		        return {
		            store: record.STORE_ID,
		            quarter: record.QUARTER,
		            sales: record.UNIT_SALES
		        }
		    })
		    expect(temp[0].store).toEqual('1')
		});

		it('should sort with object datastore array of object correctly',function (){
			var temp = customChartDatastore.sort({'STORE_ID':'desc'}).getDataArray(function(record){
		        return {
		            store: record.STORE_ID,
		            quarter: record.QUARTER,
		            sales: record.UNIT_SALES
		        }
		    })
		    expect(temp[0].store).toEqual('3');
		});

		it('should sort datastore array of array correctly',function (){
			var temp = customChartDatastore.sort('STORE_ID').getDataArray(function(record){
		        return [record.STORE_ID, record.QUARTER]
		    })
		    expect(temp[0][0]).toEqual('1')
		});

		it('should sort datastore array correctly',function (){
			var temp = customChartDatastore.sort('STORE_ID').getDataArray(function(record){
		        return record.STORE_ID
		    })
		    expect(temp[0]).toEqual('1')
		});
	})

	describe("hierarchy method", function() {
		it('should return a data tree when not defining measures',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY']})
		    expect(temp.tree).toBeDefined()
		})

		it('should return a data tree when defining measures',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}})
		    expect(temp).toBeDefined()
		})

		it('should not return a two levels tree when just one is requested',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER'],'measures':{'UNIT_SALES':'SUM'}})
		    expect(temp.tree[0].children[0]).not.toBeDefined()
		})

		it('should return children aggregated measures',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}})
		    expect(temp.tree[0].children[0].UNIT_SALES + temp.tree[0].children[1].UNIT_SALES).toEqual(temp.tree[0].UNIT_SALES)
		})

		it('should return a specific hierarchy\'s child(node)',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0)
			expect(temp).toBeDefined()
		    expect(temp.name).toEqual('Q1')
		})

		it('should return a measure value for a specific hierarchy\'s child(node)',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0).getValue('UNIT_SALES')
			expect(temp).toBeDefined()
		    expect(temp).toEqual(123)
		})

		it('should return a specific node\'s child',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0).getChild(0)
			expect(temp).toBeDefined()
		    expect(temp.name).toEqual('Drink')
		    expect(temp.children.length).toEqual(0)
		})

		it('should return an array of node\'s children',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0).getChildren()
			expect(temp).toBeDefined()
		    expect(temp.length).toBeGreaterThan(0)
		    expect(temp[0].name).toEqual('Drink')
		})

		it('should return an array of nodes of a specific level',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getLevel(0)
		    expect(temp).toBeDefined()
		    expect(temp.length).toBeGreaterThan(0)
		    expect(temp[0].children[0].name).toEqual('Drink')
		})

		it('should return an array of nodes of a different specific level',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getLevel(1)
		    expect(temp).toBeDefined()
		    expect(temp.length).toBeGreaterThan(0)
		    expect(temp[0].name).toEqual('Drink')
		})

		it('should return a node parent of specific child',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0).getChild(0).getParent();
		    expect(temp.name).toEqual('Q1')
		})

		it('should return an array of node siblings to a specific child',function (){
			var temp = customChartDatastore.hierarchy({'levels':['QUARTER','PRODUCT_FAMILY'],'measures':{'UNIT_SALES':'SUM'}}).getChild(0).getChild(0).getSiblings();
		    expect(temp.length).toEqual(2)
		})

	})
});


