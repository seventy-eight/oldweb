
function testCometd() {
	var cometd = $.cometd;

	cometd.configure({
		url: 'http://localhost:8080/cometd'
	});

	cometd.init();

	cometd.publish('/foo', { foo: 'bar' });
}