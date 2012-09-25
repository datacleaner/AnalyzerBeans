function importJS(src, look_for, onload) {
	var s = document.createElement('script');
	s.setAttribute('type', 'text/javascript');
	s.setAttribute('src', src);
	if (onload) {
		wait_for_script_load(look_for, onload);
	}
	var head = document.getElementsByTagName('head')[0];
	if (head) {
		head.appendChild(s);
	} else {
		document.body.appendChild(s);
	}
}

function importCSS(href, look_for, onload) {
	var s = document.createElement('link');
	s.setAttribute('rel', 'stylesheet');
	s.setAttribute('type', 'text/css');
	s.setAttribute('media', 'screen');
	s.setAttribute('href', href);
	if (onload) {
		wait_for_script_load(look_for, onload);
	}
	var head = document.getElementsByTagName('head')[0];
	if (head) {
		head.appendChild(s);
	} else {
		document.body.appendChild(s);
	}
}

function wait_for_script_load(look_for, callback) {
	var interval = setInterval(function() {
		if (eval("typeof " + look_for) != 'undefined') {
			clearInterval(interval);
			callback();
		}
	}, 50);
}

(function() {
	var cdnBaseUrl;
	if (location.protocol == "https:") {
		cdnBaseUrl = "https://ajax.googleapis.com/ajax/libs/";
	} else {
		cdnBaseUrl = "http://ajax.googleapis.com/ajax/libs/";
	}
	
	importJS(cdnBaseUrl + "jquery/1.7.2/jquery.min.js", 'jQuery', function() {
		importCSS(cdnBaseUrl + "jqueryui/1.8.20/themes/base/jquery-ui.css");
		importJS(cdnBaseUrl + "jqueryui/1.8.20/jquery-ui.min.js", 'jQuery.ui', function() {
			loadTabs();
			importCSS("analysis-result.css");
		});
	});
})();

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

function loadTabs() {
	$(function() {
		$(".analysisResultContainer").tabs({
			tabTemplate : "<li><a href=\"#{href}\">#{label}</a></li>"
		});
	});
}