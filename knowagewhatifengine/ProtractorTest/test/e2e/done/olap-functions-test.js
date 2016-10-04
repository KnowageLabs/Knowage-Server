describe('mdx btns test', function(){
	afterEach(function(){
		browser.sleep(2345);
	});

	it('should open olap page', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');  		
  	});

  	it('should open sidenav', function(){
      element(by.css('[ng-click="toggleRight()"]')).click();
    });

    it('should click on show mdx button',function(){
    	element(by.css('[aria-label="Show MDX"]')).click();
    });

    it('should close show mdx dialog', function(){
       element(by.css('[ng-click="closeDialog(e)"]')).click();
    });

    it('should click on show mdx button',function(){
    	element(by.css('[aria-label="Send MDX"]')).click();
    });

    it('should close send mdx dialog', function(){
       element(by.css('[ng-click="closeDialog(e)"]')).click();
    });

    it('should click on show mdx button',function(){
    	element(by.css('[aria-label="Send MDX"]')).click();
    });

    it('should close send mdx dialog', function(){
    	
        element(by.model('mdxQuery')).sendKeys(
        	'SELECT Subset({[Measures].[Store Cost]},0.0,10.0) ON COLUMNS,Subset({[Product].[Food]}, 0.0,10.0)ON ROWS FROM [Sales]');
        browser.sleep(1234);
       	element(by.css('[ng-click="sendMdxQuery(mdxQuery)"]')).click();
    });
});