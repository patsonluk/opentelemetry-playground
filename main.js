function ajaxCall(url) {
  var uid = "test-user-1"
  FS.identify(uid);
  var tracestate = 'fs='+ uid + ":" + FS.getCurrentSessionURL();
	$.ajax({
		type: 'GET',
		url: url,
                headers: { "tracestate" : tracestate},
	    success: function(data, status) {
      alert("Data: " + data + "\nStatus: " + status);
	}
});

}
