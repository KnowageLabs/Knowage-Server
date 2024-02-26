function respond(data) {
  process.send(data);
}

function applyRegex(inStr, dataStruct){
	return inStr.replace(/\$V\{([a-zA-Z0-9\-\_]{1,255})(?:.([a-zA-Z0-9\-\_]{1,255}))?\}/g
	             ,function(match,p1,p2){ return p2 ? dataStruct.VARIABLES[p1][p2] : dataStruct.VARIABLES[p1];} );
}

function handleMessage(data) {
  var ret = applyRegex(data.inStr, data.dataStruct); 
  respond(ret);
}

process.on('message', handleMessage);



