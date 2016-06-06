describe('drill butons', function(){
	var oldCnt;
	afterEach(function(){
		browser.sleep(2345);
	});

	it('should open olap page and drill down', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
  		element.all(by.css('[class="x-pivot-cell x-pivot-header-column"]')).then(function(result){
  			oldCnt = result.length;
  		});
  	});

  	it('should drill down food dimension',function(){
  		element(by.css('[src="../img/plus.gif"]')).click();
  		element.all(by.css('[class="x-pivot-cell x-pivot-header-column"]')).then(function(result){
  			expect(result.length).toBeGreaterThan(oldCnt);
  			oldCnt = result.length;
  		});
  	});

  	it('should drill down food dimension',function(){
  		element(by.css('[src="../img/minus.gif"]')).click();
  		element.all(by.css('[class="x-pivot-cell x-pivot-header-column"]')).then(function(result){
  			expect(result.length).toBeLessThan(oldCnt);
  		});
  	});
});