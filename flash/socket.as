//------------------------------------------------------------------//
// @LICENSE@
//
// XMLSocket Function Library
// authors: Lindsey Simon <lsimon@symbiot.com>, 
//          Paco Nathan <paco@symbiot.com>
//------------------------------------------------------------------//

// SET SOME STAGE VARS
Stage.scaleMode = "noScale";
Stage.align = "LT";

// get prototypes
#include "prototype_lib.as"

// Load DynTween Libs, thanks to Tatsuo Kato for the tweening prototypes.
// http://www.tatsuokato.com
#include "DynTween.as"

// _root._alpha = 0;
// _root.dynTween({duration:10, _alpha:100});

// DEFINE LISTENERS BASED ON INSTANCE
_global.socketnavListener = "snLC_"+_root.instance;
_global.navListener = "nLC_"+_root.instance;
_global.contentListener = "cLC_"+_root.instance;

_global.log("ins:"+_root.instance);


// DO NOT GET THE GLOBAL_LIB!

//  Create new XMLSocket object
_global.XML_SOCKET = new XMLSocket();


// ERROR clear screen and throw up an error message
_global.XML_SOCKET.error = function(errorCode) {

  _global.log("ERROR: "+errorCode);

  // disconnect
  this.disConnect();

  // clear the ping lag
  clearInterval(this.pingLagInterval);

  // clear the reconnect interval
  clearInterval(this.rcInterval);

  // make status bar red
  _global.SOCKET.SOCKET_ANIME.gotoAndPlay(45);

  // error page if error code
  if (errorCode) {
	 getURL("javascript:newWin=window.open('error.swf?code="+errorCode+"','newWin','width=300,height=300,menubar=no,toolbar=no,location=no,statusbar=no,scrollbars=no'); newWin.moveTo(50,50); newWin.focus(); void(0);");
  }

  // set text
  _global.SOCKET.socket_status.text = "Socket server error.";
  _global.SOCKET.ping_status.text = "";
  _global.SOCKET.PACKET_ANIME.gotoAndStop(1);
  _global.SOCKET.PING_ANIME.gotoAndStop(1);

  // change button state to allow for re-connect
  _global.SOCKET.packet_status.text = "Click button above to connect.";
  _global.SOCKET.agent_status.text = "Error Code: "+errorCode;
  //_global.XML_SOCKET.reConnect();

  

  _global.SOCKET.socketControl.icon = "reconnectIcon";
  _global.SOCKET.socketControl.state = "error";
}

 
// ON CONNECT
_global.XML_SOCKET.onConnect = function(success) {
  if (success) {

	 if (_global.DEBUG) {
		_global.log("Socket server connection successful.");
	 }

	 // start the blue bar
	 _global.SOCKET.SOCKET_ANIME.gotoAndPlay(16);
	 _global.SOCKET.PING_ANIME.gotoAndPlay(1);

	 // kill reconnect interval in case
	 if (this.rcInterval) {
		clearInterval(this.rcInterval);

		// note in ping & socket movies
		_global.SOCKET.ping_verify.text = "";
		_global.SOCKET.ping_status.text = _global.WAITING_FOR_PING;
		_global.SOCKET.packet_status.text = "Successfully reconnected.";
		getURL("reconnected.html", "content");
	 }
	 else {
		// note in ping & socket movies
		_global.SOCKET.ping_verify.text = "";
		_global.SOCKET.ping_status.text = _global.WAITING_FOR_PING;
		_global.SOCKET.packet_status.text = "Not subscribed to VIZ.";
	 }

	 
	 var CONSTRING:String = "<CONNECT session='"+_global.SESSION+"' lang='"+System.capabilities.language+"' />";

	 // SEND CONNECT STRING TO SOCKET SERVER
	 this.send(CONSTRING);

	 if (_global.DEBUG) {
		_global.log(CONSTRING);
	 }

	 // subscribe to init
	 this.subscribe({stream:"init", refresh:0});

	 // run this to make sure we eventually get a PING
	 this.pingLagInterval = setInterval(this, "checkPingLag", _global.SOCKET_LAG_TIMEOUT);

  }
  // ERROR CONNECTING TO STREAM
  else {

	 _global.log("Error connecting to stream");
	 // if the reconnect interval is set, just cycle through
	 if (this.rcInterval) {
	 }
	 // otherwise, error out 
	 else {
		this.error(0);
	 }
  }
}


// CHECK THE PING LAG
// run on interval every so often to make sure we're not in limbo
_global.XML_SOCKET.checkPingLag = function() {
  

  // if no communication or lag too great
  if (_global.LAG == undefined) {
	 _global.SOCKET.ping_status.text = "PING never received."; 
	 //reconnect
	 _global.XML_SOCKET.reConnect();
  }
  // if lag gt lag timeout
  else if (_global.LAG == true) {
	 _global.SOCKET.ping_verify.text = "Too much PING lag.";
	 //reconnect
	 _global.XML_SOCKET.reConnect();
  }
  // else ping lag ok

  // set lag to true
  // if we get back and it hasn't been reset by a PING, something's wrong
  _global.LAG = true;

}

// DISCONNECT FROM A STREAM
_global.XML_SOCKET.disConnect = function() {


  if (_global.DEBUG) {
	 _global.log("killing pingLagInterval");
	 _global.log("XML SOCKET disConnect <DISCONNECT />");
  }
  // clear and disconnect
  clearInterval(this.pingLagInterval);
  this.send("<DISCONNECT />");
  
  // make status bar red
  _global.SOCKET.SOCKET_ANIME.gotoAndPlay(45);

  _global.XML_SOCKET.close();
}

// IN CASE SOCKET DIES FOR SOME UNKNOWN REASON
_global.XML_SOCKET.onClose = function() {
  this.error(5);
}

// RECONNECT TO A STREAM
_global.XML_SOCKET.rebooting = function() {
  // decrement
  this.rbCount--;

  // at 0 start trying again via reConnect()
  if (this.rbCount == 0) {
	 // clear reboot interval
	 clearInterval(_global.XML_SOCKET.rebootInterval);
	 _global.SOCKET.Spinner.removeMovieClip();
	 _global.SOCKET.socketControl._visible = true;

	 // reConnect now
	 _global.XML_SOCKET.reConnect();
  }
  else {
	 _global.SOCKET.socket_status.text = "Trying in "+_global.XML_SOCKET.rbCount+" sec.";
  }
}

// RECONNECT TO A STREAM
_global.XML_SOCKET.reConnect = function() {

  // make sure there's no pingLagInterval set
  clearInterval(this.pingLagInterval);
  
  _global.SOCKET.socket_status.text = "Trying to reconnect.";
  _global.SOCKET.packet_status.text = "Waiting for connection.";
  _global.SOCKET.ping_status.text = "";
  _global.SOCKET.agent_status.text = "";
  _global.SOCKET.PACKET_ANIME.gotoAndStop(1);
  _global.SOCKET.SOCKET_ANIME.gotoAndPlay(1);

  // set counter
  _global.XML_SOCKET.rcCounter = 1;
  
  this.rcInterval = setInterval(this, "loopConnect", 5000, _global.SOCKET_HOST, _global.SOCKET_PORT);

}

_global.XML_SOCKET.loopConnect = function(HOST:String, PORT:Number) {
  _global.XML_SOCKET.rcCounter++;

  // :REDO put rcCounter in socket_config.xml
  if (_global.XML_SOCKET.rcCounter == 20) {
	 _global.XML_SOCKET.error();
  }
  else {
	 _global.XML_SOCKET.connect(HOST, PORT);
  }
}


_global.parseStreamData = function(DataString) {

  // _global.log("--------------- ON DATA ------------------");
  // _global.log(DataString);
  

  // Split up the datastring
  var streamXML:Array = DataString.split("|");
  if (_global.DEBUG_VERBOSE) {
	 _global.log("streamXML: "+DataString);
  }
  // USE STREAMINFO at 0 key, and ignore 0 key when parsing
  var STREAMSTUFF = streamXML[0];
  var STREAMINFO = STREAMSTUFF.split("&");
  var STREAMNAME = STREAMINFO[0];


  // CHECK IF PING
  if (STREAMNAME == "PING") {

	 // send der PONG
	 _global.XML_SOCKET.send("<PONG />");

	 // set some status
	 _global.SOCKET.ping_status.text = STREAMNAME+" "+_global.PACKET_RECEIVED;
	 _global.SOCKET.PING_ANIME.gotoAndPlay(7);

	 // set LAG to false, since we did in fact get a ping
	 _global.LAG = false;
	 
	 _global.log("Status:"+streamXML[1]);

	 // STATUS NODE
	 var STATUS = streamXML[1].split("&");
	 
	 // reboot sequence
	 var reboot:Number = STATUS[1];
	 if (reboot == "1") {
		// load reboot.html URL
		getURL("reboot.html", "content");
		
		// unsubscribe all streams and disconnect
		_global.XML_SOCKET.unSubscribe();
		_global.XML_SOCKET.disConnect();

		// change the look
		_global.SOCKET.socket_status.text = "Trying in "+_global.XML_SOCKET.rbCount+" sec.";
		_global.SOCKET.last_packet_status.text = _global.SOCKET.packet_status.text = _global.SOCKET.ping_status.text = "";
		_global.SOCKET.SOCKET_ANIME.gotoAndStop(1);
		_global.SOCKET.PACKET_ANIME.gotoAndStop(1);
		_global.SOCKET.PING_ANIME.gotoAndStop(1);

		// attach Spinner
		_global.SOCKET.attachMovie("Spinner", "Spinner", _global.SOCKET.getNextHighestDepth());
		_global.SOCKET.Spinner._x = _global.SOCKET.socketControl._x+(_global.SOCKET.Spinner._width/2);
		_global.SOCKET.Spinner._y = _global.SOCKET.socketControl._y+_global.SOCKET.Spinner._height;
		
		// make button invisible
		_global.SOCKET.socketControl._visible = false;

		// set number of seconds between reboot signal and reconnect
		_global.XML_SOCKET.rbCount = 120;

		//reconnect after rbCount seconds
		_global.XML_SOCKET.rebootInterval = setInterval(_global.XML_SOCKET, "rebooting", 1000);

	 }

	 // modal error
	 var modal:Number = STATUS[2];

	 // some other error
	 var error:Number = STATUS[3];
	 if (error != 0) {
		_global.XML_SOCKET.error(error);
	 }

	 // Agent Status string
	 var message:String = STATUS[4];
	 _global.SOCKET.agent_status.text = message;

	 var url:String = STATUS[5];	 

	 // reboot sequence

	 return;
  }

  // hold init and wait
  else if (STREAMNAME == "INIT") {
	 _global.initString = DataString;
  }


  // send onto the LocalConnection
  else {

	 // set tickstamp
	 var TICKSTAMP = new Date(STREAMINFO[1]);

	 // set last_status
	 _global.SOCKET.last_packet_status.text = _global.LAST_PACKET_AT+" "+TICKSTAMP.getTimeStamp()+" ("+STREAMNAME+")";
	 
	 _global.SOCKET.packet_status.text = STREAMNAME+" "+_global.PACKET_RECEIVED;
	 _global.SOCKET.PACKET_ANIME.gotoAndPlay(7);

	 // send on
	 _global.socketLC.send(_global.contentListener, "parseStreamData", DataString);
  }

}


// SUBSCRIBE
_global.XML_SOCKET.subscribedTo = new Array();
_global.XML_SOCKET.subscribe = function(STREAM_OBJ) {
  
  //_global.log("XML_SOCKET subscribe to stream:"+STREAM_OBJ.stream+", refresh:"+STREAM_OBJ.refresh);


  // keep a record of what we're subscribed to in an array
  if (STREAM_OBJ.stream != "init") {
	 _global.XML_SOCKET.subscribedTo.push(STREAM_OBJ);
	 // set text
	 _global.SOCKET.packet_status.text = _global.SUBSCRIBING_TO+" "+STREAM_OBJ.stream.toUpperCase();
	 _global.SOCKET.PACKET_ANIME.gotoAndPlay(1);
  }

  
  // not enough args to subscribe
  if (STREAM_OBJ.stream == undefined || STREAM_OBJ.refresh == undefined) {
	 this.error(6);
	 return;
  }

  // INITIALIZE lastPING and LAG
  if (this.LAG == undefined) {
	 this.LAG = 0;
	 this.lastPING = 0;
  }

  // SEND SUBSCRIBE STREAM TO SOCKET SERVER
  var SUBSCRIBE_STRING:String = "<SUBSCRIBE";
  for (var attr in STREAM_OBJ) {
	 var VALUE = STREAM_OBJ[attr];
	 SUBSCRIBE_STRING += " "+attr+"='"+VALUE+"'";
  }
  SUBSCRIBE_STRING += " />";

  if (_global.DEBUG) {
	 _global.log("XML SOCKET subscribe:"+SUBSCRIBE_STRING);
  }

  // Send the string over the socket
  this.send(SUBSCRIBE_STRING);
  
}

  
// UNSUBSCRIBE TO A STREAM
_global.XML_SOCKET.unSubscribe = function(STREAM_OBJ) {
  _global.socketLC.send(_global.contentListener, "log", "unSubscribing:"+STREAM_OBJ.stream);

  // clean out array if not pausing
  if (!STREAM_OBJ.pausing) {
	 if (STREAM_OBJ.stream == undefined) {
		_global.XML_SOCKET.subscribedTo = new Array();
		
		_global.SOCKET.packet_status.text = "Unsubscribed.";
		_global.SOCKET.PACKET_ANIME.gotoAndStop(1);
	 }
	 // specific stream
	 else {
		var i:Number = _global.XML_SOCKET.subscribedTo.length+1;
		while (--i > -1) {
		  if (STREAM_OBJ.stream == _global.XML_SOCKET.subscribedTo[i].stream) {
			 _global.log("purging "+STREAM_OBJ.stream+" from subscribedTo");
			 
			 _global.XML_SOCKET.subscribedTo.splice(i, 1);
		  }
		  else {
			 _global.log("Leaving "+_global.XML_SOCKET.subscribedTo[i].stream+" alone.");
		  }
		}
	 }
  }

  var UNSUBSCRIBE_STRING:String = "<UNSUBSCRIBE";

  for (var attr in STREAM_OBJ) {
	 var VALUE = STREAM_OBJ[attr];
	 UNSUBSCRIBE_STRING += " "+attr+"='"+VALUE+"'";
  }
  UNSUBSCRIBE_STRING += " />";

  if (_global.DEBUG) {
	 _global.log("XML SOCKET UNsubscribe: :"+UNSUBSCRIBE_STRING);
  }

  // send the String to the socket listener
  this.send(UNSUBSCRIBE_STRING);

}



// set default parser
_global.XML_SOCKET.onData = _global.parseStreamData;




// Load the Socket tester mc
_global.styles.AccordionHeader.setStyle("fontSize", "14");

// make Accordion
_root.attachMovie("Accordion", "socketAccordion", 1);
_root.socketAccordion._x = _root.socketAccordion._y = 0;
_root.socketAccordion.setSize(150, 210);

_root.socketAccordion.headerHeight = 25;
_root.socketAccordion.setStyle("themeColor", "0x999999");

// create the pane in the accordion for this menu
var menuPane = _root.socketAccordion.createChild("View", "STATUS", { label: _global.STATUS_TITLE });

// attach Socket clip to Accordion
_global.SOCKET = menuPane.attachMovie("Socket", "Socket", 1);

// unselectable fields
_global.SOCKET.socket_status.selectable = false;
_global.SOCKET.socket_status.text = _global.CONNECTION_INITIALIZING;

_global.SOCKET.packet_status.selectable = false;
_global.SOCKET.last_status.selectable = false;
_global.SOCKET.ping_status.selectable = false;
_global.SOCKET.ping_verify.selectable = false;
_global.SOCKET.agent_status.selectable = false;


// start em off dead
_global.SOCKET.PACKET_ANIME.gotoAndStop(1);
_global.SOCKET.PING_ANIME.gotoAndStop(1);

_global.SOCKET.attachMovie("Button", "socketControl", 3);
_global.SOCKET.socketControl.setSize(30,20);
_global.SOCKET.socketControl.useHandCursor = true;
_global.SOCKET.socketControl._x = 8;
_global.SOCKET.socketControl._y = 8;
_global.SOCKET.socketControl.icon = "pauseIcon";


// socketControl pause/play/error
_global.SOCKET.socketControl.state = "playing";
_global.SOCKET.socketControl.onRelease = function() {

  // if it's playing now, they're wanting to pause
  if (this.state == "playing") {
	 //_root._root.socketAccordion.STATUS.label.text = _global.STATUS_TITLE+" "+_global.PAUSED;
	 _global.SOCKET.packet_status.text = _global.PAUSED;
	 _global.SOCKET.socketControl.state = "paused";
	 _global.SOCKET.socketControl.icon = "playIcon";

	 // unsubscribing is the same as pausing
	 _global.XML_SOCKET.unSubscribe({pausing:true});
  }

  // error
  else if (this.state == "error") {
	 _global.SOCKET.packet_status.text = "";
	 _global.SOCKET.socketControl.state = "playing";
	 _global.SOCKET.socketControl.icon = "playIcon";
	 _global.SOCKET.socket_status.text = "Trying to reconnect.";
	 _global.XML_SOCKET.connect(_global.SOCKET_HOST, _global.SOCKET_PORT);
  }

  // they want to play again so we resubscribe them to their streams
  else {
	 //_root._root.socketAccordion.STATUS.label.text = _global.STATUS_TITLE+" "+_global.PLAYING;
	 _global.SOCKET.packet_status.text = _global.WAITING_FOR_PACKETS;
	 _global.SOCKET.socketControl.state = "playing";
	 _global.SOCKET.socketControl.icon = "pauseIcon";
	 var length:Number = _global.XML_SOCKET.subscribedTo.length;
	 for (var i=0; i<length; i++) {
		_global.XML_SOCKET.subscribe(_global.XML_SOCKET.subscribedTo[i]);
	 }
  }
}


// OPEN CONNECT TO XML SOCKET
_global.XML_SOCKET.connect(_global.SOCKET_HOST, _global.SOCKET_PORT);



// SOCKET LOCAL CONNECTION 
// 
// The ever useful LocalConnection is how we pass stuff to and from
// our XMLSocket stream
// create LocalConnection and its methods
_global.socketLC = new LocalConnection();
_global.socketLC.connect(_global.socketnavListener);

// when we get a call to subscribe
_global.socketLC.subscribe = function(subscribeObj) {
  _global.socketLC.send(_global.contentListener, "log", "subscribe !!");
  _global.XML_SOCKET.subscribe(subscribeObj);
}

// when we get a call to unsubscribe
_global.socketLC.unSubscribe = function(unSubscribeObj) {
  _global.XML_SOCKET.unSubscribe(unSubscribeObj);
}

// when we get a call for initString
_global.socketLC.sendInitString = function() {
  _global.socketLC.send(_global.contentListener, "log", "Sending INIT stream back");
  _global.socketLC.send(_global.contentListener, "parseStreamData", _global.initString);
}

// when we get a call to subscribe
_global.socketLC.sendSocketString = function(sendString) {
  _global.XML_SOCKET.send(sendString);
}

// setAgentStatus
_global.socketLC.setAgentStatus = function(status) {
}

// setAgentStatus
_global.socketLC.log = function(string) {
  _global.log(string);
}


// nav will send this request to loadContent onRelease
_global.socketLC.loadContent = function(URL:String) {

  // first we unsubscribe to be safe
  _global.XML_SOCKET.unSubscribe();

  // load URL
  getURL(URL, "content");
  

}

