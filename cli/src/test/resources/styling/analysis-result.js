var analysisResult = {};

function drillToDetails(elementId) {
	$('#' + elementId).dialog({
		modal : true,
		width : 700
	});
}

/**
 * Init tabs for each descriptor
 */
$(function() {
	$(".analysisResultContainer").tabs({
		tabTemplate : "<li><a href=\"#{href}\">#{label}</a></li>"
	});
});