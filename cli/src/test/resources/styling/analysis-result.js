var analysisResult = {};

function drillToDetails(elementId) {
	var wWidth = $(window).width();
	var dWidth = wWidth * 0.85;
	var wHeight = $(window).height();
	var dHeight = wHeight * 0.8;
	$('#' + elementId).dialog({
		modal : true,
		width : dWidth,
		height : dHeight
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