// HEJ--
var Utils = {};
	
Utils.hideme = function( id ) {
	$( "#" + id ).hide( "slow" );
}

Utils.startupload = function( nodeid, append ) {
	$.getJSON('/upload?nodeid=' + nodeid, function(data) {
		if( data.ratio < 1.0 ) {
			$("#status"+append).width( Math.floor( data.ratio * 100 ) + "%" );
			setTimeout( function() { Utils.startupload( nodeid, append ); }, 1000 );
		} else {
			$("#status"+append).width( "100%" );
			$("#nodeid"+append).val( nodeid );
		}
	});
}

Utils.getProgress = function( resourceId, element, p ) {
    $('#' + element).hide();
    $('#' + p).show();
    Utils.updateProgress( resourceId, p );
}

Utils.updateProgress = function( resourceId, element ) {
    $.get( '/resource/' + resourceId + '/progress', function( data ) {
            $('#' + element).width( ( data * 100 ) + '%' );

            if( data < 1.0 ) {
                setTimeout( function() {
                    Utils.updateProgress( resourceId, element );
                }, 500)
            } else if( data == 1.0 ) {
                $('#' + element).html( "Upload done" );
                $('#' + element).css( 'background-color', '#ffffff' );
            }
        }
    )


}

Utils.popupselect = function( myurl, height ) {
		var w1 = screen.width * 0.9;
		var w2 = screen.width * 0.05;
		var h1 = screen.height * 0.45;
		
		a = window.open( myurl, '', "width=" + w1+ ",height=" + height + ",status=no,toolbar=no,menubar=no,dependent=yes,directories=no,hotkeys=no,scrollbars=yes,location=no,resizable=no,screenX=" + w2 + ",screenY=" + h1 );
}

Utils.addJsonElement = function( form ) {
	
	var j = {};
	var jsonData  = Utils.getJsonFromForm( form, j );
	alert( "JSON: " + JSON.stringify( jsonData ) );
	var element   = document.createElement( 'input' );
	element.name  = "json";
	element.type  = "hidden";
	element.value = JSON.stringify( jsonData );
	form.appendChild( element );
}


Utils.getJsonFromForm = function( e, jsonData ) {
    var childs = e.childNodes;

    for( var i = 0 ; i < childs.length ; i++ ) {
        //alert( "Child: " + childs[i] + ", TAG:" + childs[i].tagName + ", nodename:" + childs[i].name );
        var name = childs[i].name != undefined ? childs[i].name : childs[i].title;

        if( childs[i].tagName != undefined && name.length > 0 ) {
            //alert( "NAME=" + name + ", " + childs[i].tagName );
            switch( childs[i].tagName ) {
                case "INPUT":
                    //alert("INPUT: " + childs[i].name + "=" + childs[i].value + ", type: " + childs[i].type);
                    if( childs[i].type == "radio" ) { // Special radio button case
                        if( childs[i].checked ) {
                            jsonData[childs[i].name] = childs[i].value;
                        }
                    } else if( childs[i].type == "checkbox" ) {
                        if( childs[i].checked ) {
                            jsonData[childs[i].name] = childs[i].value;
                        } else {
                            jsonData[childs[i].name] = null;
                        }
                    } else {
                        jsonData[childs[i].name] = childs[i].value;
                    }
                    break;
                case "SELECT":
                    //alert( "SELECT: " + childs[i].name + "=" + childs[i].value + ", type: " + childs[i].type );
                    if( childs[i].type == "select-one" ) {
                        jsonData[childs[i].name] = childs[i].value;
                    } else {
                        //alert( "Debug: " + debug( childs[i] ) );
                        jsonData[childs[i].name] = {};
                        for( var j = 0 ; j < childs[i].length ; j++ ) {
                            //alert(j+ "= "+ childs[i][j].value );
                            var t = jsonData[childs[i].name];
                            if( childs[i][j].selected ) {
                                t[j] = childs[i][j].value;
                            }
                        }
                    }

                    break;
                case "DIV":
                    //alert("DIV: " + name + "=" + $(childs[i]).html() );
                    if( $(childs[i]).is( ':visible' ) ) {
                        if( jsonData[name] == undefined ) {
                            jsonData[name] = [];
                        }
                        var j = jsonData[name].push( {} );
                        Utils.getJsonFromForm( childs[i], jsonData[name][j-1] );
                        //alert( JSON.stringify( jsonData[name][j-1] ) );
                    }
                    break;
            }
        }

            //alert( "Testing= " + childs[i].tagName );
        if( childs[i].tagName == 'TR' || childs[i].tagName == 'TD' || childs[i].tagName == 'TABLE' || childs[i].tagName == 'TBODY' ) {
            //alert( "IN HERE= " + $(childs[i]).is( ':visible' ) );
            Utils.getJsonFromForm( childs[i], jsonData );

        }
    }

    return jsonData;
}

Utils.toggleFormElement = function( id ) {

    var element2 = $( '#' + Utils.jqEscape( id ) );

    if( element2.is( ':visible') ) { // Disable
        element2.hide( 500 );
        element2.attr( 'data-enabled', 0 );
    } else { // Enable
        element2.show( 500 );
        element2.attr( 'data-enabled', 1 );
    }
}


Utils.jqEscape = function( str ) {
    return str.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
}