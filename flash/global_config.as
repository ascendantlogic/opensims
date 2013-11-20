//------------------------------------------------------------------//
// @LICENSE@
//
// CONFIG
// authors: Lindsey Simon <lsimon@symbiot.com>, 
//          Paco Nathan <paco@symbiot.com>
//
//------------------------------------------------------------------//


// parse xml to create the links here
var configXML:XML = new XML();
configXML.ignoreWhite = true;
configXML.onLoad = function(success) {

  if (success) {
	 var CONFIG = configXML.firstChild;
	 _global.SESSION = CONFIG.attributes.session;

	 var GLOBAL_FIELD = CONFIG.firstChild;
	 while (GLOBAL_FIELD != null) {

		// set the true or false explicit
		if (GLOBAL_FIELD.attributes.type == "Boolean") {
		  if (GLOBAL_FIELD.attributes.value == "true") {
			 _global[GLOBAL_FIELD.nodeName] = true;
		  }
		  else if (GLOBAL_FIELD.attributes.value == "false") {
			 _global[GLOBAL_FIELD.nodeName] = false;
		  } 
		}

		// Numbers
		else if (GLOBAL_FIELD.attributes.type == "Number") {
		  _global[GLOBAL_FIELD.nodeName] = Number(GLOBAL_FIELD.attributes.value);
		}

		// Array
		else if (GLOBAL_FIELD.attributes.type == "Array") {
		  if (!_global[GLOBAL_FIELD.nodeName]) {
			 _global[GLOBAL_FIELD.nodeName] = new Array();
		  }
		  _global[GLOBAL_FIELD.nodeName].push(GLOBAL_FIELD.attributes.value);
		}

		// Strings
		else {
		  _global[GLOBAL_FIELD.nodeName] = GLOBAL_FIELD.attributes.value;
		}
		
		
		//_global.log("SETTING "+GLOBAL_FIELD.nodeName+" to "+GLOBAL_FIELD.attributes.value);
		GLOBAL_FIELD = GLOBAL_FIELD.nextSibling;
	 }
	 
	 
	 // only draw this stuff and setup .log if DEBUG and DEBUG_X defined
	 if (_global.DEBUG && _global.DEBUG_X) {

		var DBTN  = _root.attachMovie("Button", "BUTTON_mc", 1);
		DBTN._alpha = 100;
		DBTN._x = DEBUG_X;
		DBTN._y = DEBUG_Y-20;
		DBTN.setSize(50,20);
		DBTN.stylize();
		DBTN.label = "Analyze";
		DBTN.onPress = function() {
		  _global.killAll();
		  _global.MovieClipLoader.unloadClip(_root.UI_CONTENT);
		}

		_root.attachMovie("TextArea", "DEBUGGING", 2);
		DEBUGGING._alpha = 100;


		DEBUGGING._x = _global.DEBUG_X;
		DEBUGGING._y = _global.DEBUG_Y;
		DEBUGGING.setSize(_global.DEBUG_WIDTH, _global.DEBUG_HEIGHT);

		DEBUGGING.wordWrap = true;
		DEBUGGING.text = "Initialized Debgging\n_global.DEBUG="+_global.DEBUG+"\n_global.DEBUG_VERBOSE="+_global.DEBUG_VERBOSE;


		// logging function
		_global.log = function(message, wipe) {
		  if (wipe) {
			 DEBUGGING.text = message;
		  }
		  else {
			 DEBUGGING.text = message
			 + "\n"
			 + DEBUGGING.text;
		  }
		}
		// SET LOGGING FUNCTION
		_global.log("Global logging setup successfully.");
		_global.log("Preparing to load _global.CONFIG_FILE:"+_global.CONFIG_FILE);
	 }
	 else {
		_global.log = undefined;
		_global.DEBUG = false;
	 }

  }

  // NOT success
  else {
	 _global.errorPage("XML Load Error", "Error loading and reading "+_global.CONFIG_FILE);
  }
}

// Load and parse the server config file
// the actual name of the config file is passed in
var xmlCacheBust:Number = Math.random();
configXML.load(_global.CONFIG_FILE+"&xmlCacheBust="+xmlCacheBust);
