function showLoading(div) {
	div.html('<div class="loader">Loading...</div>');
}

var loopingAction;

/** Loads URL in the main section of the page */
function loadPageInMain(url) {
	if (loopingAction){
		window.clearInterval(loopingAction);
		loopingAction = null;
	}
	
	loadPage($('#page_content'), url, true, function(response, status, xhr){
		if ( status == "error" ) {
			$('#page_content').html('Error');
			$('#page_content').html(response);
		}
	});
}

function doUntilLoadInMain(thingToDo, interval){
	loopingAction = setInterval(thingToDo, interval);
}

function loadPage(div, url, showLoader, onComplete) {
	div.css('border-style', 'none');
	if (showLoader) {
		showLoading(div);
	}
	div.load(url, onComplete);
}

function submitForm(form, messageDiv) {
    $.ajax({
        url     : $(form).attr('action'),
        type    : $(form).attr('method'),
        data    : $(form).serialize(),
        dataType: 'json',
        success : function( data ) {
             messageDiv.html('Great success!');
        },
        error   : function( xhr, err ) {
        		messageDiv.html(xhr.responseJSON.message != null ?xhr.responseJSON.message :  xhr.responseJSON.exception);    
        }
    });    
}


function submitFormAndGoto(form, messageDiv, gotoUrl) {
    $.ajax({
        url     : $(form).attr('action'),
        type    : $(form).attr('method'),
        data    : $(form).serialize(),
        dataType: 'json',
        success : function( data ) {
             loadPageInMain(gotoUrl);
        },
        error   : function( xhr, err ) {
        		var type = xhr.getResponseHeader("Content-Type");
        		if (type.startsWith("application/json")){
        			messageDiv.html(xhr.responseJSON.message != null ?xhr.responseJSON.message :  xhr.responseJSON.exception);
        		} else {
        			$('#page_content').html(xhr.responseText);
        		}
        }
    });    
}


function deleteAndGoto(uriToDelete, messageDiv, gotoUrl) {
    $.ajax({
        url     : uriToDelete,
        type    : 'DELETE',
        dataType: 'json',
        success : function( data ) {
             loadPageInMain(gotoUrl);
        },
        error   : function( xhr, err ) {
        		messageDiv.html(xhr.responseJSON.message != null ?xhr.responseJSON.message :  xhr.responseJSON.exception);
        }
    });  
}

