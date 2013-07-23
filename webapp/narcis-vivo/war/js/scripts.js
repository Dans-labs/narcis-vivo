// When everything is ready, call init
$().ready(init);

/**
 * Function called when all the javascript is ready to rock
 */
function init() {
	// Configure AJAX queries
	$.ajaxSetup({
		datatype : "json"
	});

	// Bind to StateChange Event of the history
	History.Adapter.bind(window, 'statechange', function() {
		loadResource();
	});	
	
	// Setup the autocomplete
	var opts = {
		serviceUrl : '/api/person',
		deferRequestBy : 250,
		minChars : 3,
		noCache : true,
		onSelect : function(suggestion) {
			changeState(suggestion.data);
		}
	};
	$('#search-name').autocomplete(opts);
	
	// Load the current resource, in case there is one
	History.go(0);
	
	// DEBUG load Frank's data
	//changeState("http://vua.example.org/individual/Person_PRS1242925");
}

/**
 * @param resource
 */
function changeState(resource) {
	var url = "?resource=" + resource;
	var title = "Showing " + resource;
	History.pushState({
		resource : resource
	}, title, url);
}

/**
 * @param resource
 */
function loadResource() {
	var State = History.getState();
	var resource = State.data['resource'];
	if (resource == null)
		return;

	// Make some room and show the data loading sign
	$('#triplesBody').empty();
	
	// Send the query and fill with data upon completion
	$.ajax({
		url : '/api/person',
		data : {
			resource : resource
		},
		success : function(json) {
			$.each(json.triples, function(index, value) {
				var cell1 = $("<td/>").text(index);
				var cell3 = $("<td/>").text(value.p);
				var cell4 = $("<td/>").text(value.o);
				var row = $("<tr/>");
				cell1.appendTo(row);
				cell3.appendTo(row);
				cell4.appendTo(row);
				row.appendTo($('#triplesBody'));
			});
		},
	});
}