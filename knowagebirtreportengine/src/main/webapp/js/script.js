$(document).ready(function() {
	var win = $(window);
	var pageCounter = 2;
	// Each time the user scrolls
	win.scroll(function() {

		clearTimeout($.data(this, 'scrollTimer'));

		$.data(this, 'scrollTimer', setTimeout(function() {

			// End of the document reached?
			//if ($(document).height() - win.height() == win.scrollTop()) {
			//alert(($(document).height() - win.height())/win.scrollTop());
			if (($(document).height() - win.height())/win.scrollTop() <= 1.06 ) {
				//alert('posttest');


				//$('#loading').show();

				$.ajax({
					url: 'getPage?page=' + pageCounter + '&report_execution_id=' + Sbi.birtengine.reportExecutionId,
					dataType: 'html',
					success: function(html) {
						var bodyIndex = html.search("<body");
						var startIndex = html.indexOf(">", bodyIndex);
						var endIndex = html.search("</body>");
						html = html.substr(startIndex + 1, endIndex);
						$(document.body).append(html);
						//$('#loading').hide();
						pageCounter++;
					}
				});
			}

		}, 250));

	});
});
