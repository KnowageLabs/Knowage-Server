describe('drag drop and swap test', function(){
	afterEach(function(){
		browser.sleep(2345);
	});

	it('should open olap page', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
  	});

    //from filters on left axis
  	it('should drag filter and drop on left',function(){
  		browser.driver.actions().dragAndDrop(
          browser.driver.findElement(by.id('filter-Region')),
          element(by.css('[ng-drop-success="dropLeft($data,$event)"]'))
          ).perform();
      
        element.all(by.repeater('row in rows')).then(function(count) {
            expect(count.length).toBeGreaterThan(1);
        });
  	});

    //from left axis back to filters
  	it('should drag left axis member and drop on filter',function(){
  		browser.driver.actions().dragAndDrop(
          browser.driver.findElement(by.id('left-Region')),
          element(by.css('[ng-drop-success="dropFilter($data,$event)"]'))
          ).perform();
      
      element.all(by.repeater('filter in filterCardList')).then(function(count) {
            expect(count.length).toBeGreaterThan(1);
        });
  	});

    //from filters on top axis
    it('should drag filter on top',function(){
      browser.driver.actions().dragAndDrop(
          browser.driver.findElement(by.id('filter-Region')),
          element(by.css('[ng-drop-success="dropTop($data,$event)"]'))
          ).perform();
      
        element.all(by.repeater('column in columns')).then(function(count) {
            expect(count.length).toBeGreaterThan(1);
        });
    });

    //from axis to axis
    it('should drag from top drop on left ',function(){
      var countTop;
      var countLeft;

      element.all(by.repeater('row in rows')).then(function(count) {
          countLeft = count.length;
      });

      browser.driver.actions().dragAndDrop(
          browser.driver.findElement(by.id('top-Region')),
          element(by.css('[ng-drop-success="dropLeft($data,$event)"]'))
          ).perform();
      
      element.all(by.repeater('row in rows')).then(function(count) {
            expect(count.length).toBeGreaterThan(countLeft);
      });
    });

    it('should swap axis',function(){
        var countTop;
        var countLeft;

        element.all(by.repeater('column in columns')).then(function(count){
          countTop = count.length;
        });

        element.all(by.repeater('row in rows')).then(function(count){
          countLeft = count.length;
        });

        element(by.css('[ng-click="swapAxis()"]')).click();

        element.all(by.repeater('column in columns')).then(function(count){
          expect(count.length).toBeGreaterThan(countTop);
        });

        element.all(by.repeater('row in rows')).then(function(count){
          expect(count.length).toBeLessThan(countLeft);
        });

    });
});