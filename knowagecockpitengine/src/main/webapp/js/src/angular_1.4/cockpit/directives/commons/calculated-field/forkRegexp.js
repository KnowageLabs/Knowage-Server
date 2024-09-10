function respond(data) {
  process.send(data);
}

function handleMessage(data) {
  f = new Function(data.cback);
  var appo = data.inStr.replace(data.regEx, f);
  respond(appo);
}

process.on('message', handleMessage);
