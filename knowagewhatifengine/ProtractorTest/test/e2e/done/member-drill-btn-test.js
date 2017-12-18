describe('drill butons', function(){
	afterEach(function(){
		browser.sleep(2345);
	});

	it('should open olap page and drill down', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
  		//there is no hdr elements yet
  		expect(element(by.css('[class="pv-col-hdr"]')).isPresent()).toBe(false);
  		expect(element(by.css('[class="pv-row-hdr"]')).isPresent()).toBe(false);
  	});

	it('should open sidenav', function(){
      element(by.css('[ng-click="toggleRight()"]')).click();
    });

    it('should click on replace button', function(){
    	element(by.id('drill-replace-btn')).click();
    	expect(element(by.css('[class="pv-col-hdr"]')).isPresent()).toBe(true);
  		expect(element(by.css('[class="pv-row-hdr"]')).isPresent()).toBe(true);
    });
});