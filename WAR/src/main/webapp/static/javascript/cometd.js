
function testCometd() {
	var cometd = $.cometd;

	cometd.configure({
		url: 'http://localhost:8080/upload'
	});
	
	cometd.init();

	
	cometd.subscribe('/upload/*', function(message) {
		alert("yay");
	});
	
	
	
	//cometd.publish('/foo', { foo: 'bar' });
}