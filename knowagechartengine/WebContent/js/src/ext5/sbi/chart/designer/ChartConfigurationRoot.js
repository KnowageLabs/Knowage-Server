/**
 * This is the root panel class that will be extended by all panels on the Step 2
 * (Configuration) tab panel of the Designer. We will specify some common properties
 * that those panels should have (contain).
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	'Sbi.chart.designer.ChartConfigurationRoot', 
	
	{
		extend : 'Ext.panel.Panel',
		
		/**
		 * The padding on the RIGHT is 10 since the scrollbar appears when
		 * there are too many panels (when the tab content cannot fit the 
		 * height of the page) comes very close with its left edge to the
		 * right edge of panels on the Configuration tab (former Step 2).
		 */
		// (top, right, bottom, left)
		margin: "5 10 5 0"
	}
);