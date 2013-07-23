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

	
	// Hide extra screens
	$('#dataScreen').hide();
	$('#loadingScreen').hide();
	
	// Load the current resource, in case there is one
	// History.go(1);
	loadResource();

	// DEBUG load Frank's data
	// changeState("http://vua.example.org/individual/Person_PRS1242925");
	// changeState("http://hal.archives-ouvertes.fr/autlabid/Magali_Hersant_65");
	// J. Leeuwen (ruu)
	// changeState("http://ruu.example.org/individual/Person_PRS1234619");
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

	// Hide previous information
	$('#dataScreen').hide();
	$('#loadingScreen').show();
	
	// Make some room and show the data loading sign
	$('#triplesBody').empty();
	$('#relationsBody').empty();
	$('#interestsBody').empty();

	// Send the query and fill with data upon completion
	$.ajax({
		url : '/api/person',
		data : {
			resource : resource
		},
		success : function(json) {
			// Fill in the name
			$('#search-name').val(json.name);
			
			// Fill in the centres of interest
			$.each(json.interests, function(index, value) {
				var badge = $("<span/>").attr('class', 'label label-info')
						.text(value.label);
				badge.appendTo($('#interestsBody'));
			});

			// Fill in the description
			$.each(json.triples, function(index, value) {
				var cell1 = $("<td/>").text(index);
				var cell2 = $("<td/>").text(value.p);
				var cell3 = $("<td/>").text(value.o);
				var row = $("<tr/>");
				cell1.appendTo(row);
				cell2.appendTo(row);
				cell3.appendTo(row);
				row.appendTo($('#triplesBody'));
			});

			// Fill in the relations
			$.each(json.relation, function(index, value) {
				var cell1 = $("<td/>").text(index);
				var cell2 = $("<td/>").text(value.source);
				var link = $("<a/>").attr("href", "#").text(value.name);
				link.click(function() {
					changeState(value.person);
				});
				var cell3 = $("<td/>");
				link.appendTo(cell3);
				var row = $("<tr/>");
				cell1.appendTo(row);
				cell2.appendTo(row);
				cell3.appendTo(row);
				row.appendTo($('#relationsBody'));
			});
			
			// Show
			$('#dataScreen').show();
			$('#loadingScreen').hide();

		},
	});
}