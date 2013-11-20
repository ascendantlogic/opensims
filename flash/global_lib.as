//------------------------------------------------------------------//
// @LICENSE@
//
// Global scope functions/properties
// author: Lindsey Simon <lsimon@symbiot.com>
//------------------------------------------------------------------//

// SET SOME STAGE VARS
Stage.scaleMode = "noScale";
Stage.align = "LT"; 


// DEFINE LISTENERS BASED ON INSTANCE
_global.socketnavListener = "snLC_"+_global.INSTANCE;
_global.navListener = "nLC_"+_global.INSTANCE;
_global.contentListener = "cLC_"+_global.INSTANCE;

// create LocalConnection
_global.contentLC = new LocalConnection();
_global.contentLC.connect(_global.contentListener);


// so we can log here from anywhere
_global.contentLC.logContent = function(msg) {
  _global.log("contentLC.log:"+msg);
}

// sets the bgcolor of the fields when they get focused
_global.handleFieldFocus = function() {
  this.textColor = "0x000000";
  this.borderColor = "0x000000";
  this.backgroundColor = "0xFFFFFF";
}
_global.handleFieldBlur = function() {
  this.textColor = "0x666666";
  this.borderColor = "0x999999";
  this.backgroundColor = "0xFFFFFF";
}


// RollOverText follows the mouse at all times, at highest depth
_root.createTextField("RT", 99999999999, 0, 0, 10, 10);
var RollText = _root.RT;
RollText.selectable = false;
RollText.setNewTextFormat(_global.SmallTxtFormat);
RollText.autoSize = true;
RollText.border = true;
RollText.background = true;
RollText.backgroundColor = "0xFFFFCC";
RollText._visible = false;


// Functions for setting/unsetting roll over text
_global.setRollText = function(text:String) {
  // move the text here
  _root.RT._x = _xmouse+5;
  _root.RT._y = _ymouse-15;
  _root.RT.text = text;
  _root.RT._visible = true;
  _root.onMouseMove = function() {
	 _root.RT._x = _xmouse+5;
	 _root.RT._y = _ymouse-15;
  }
}
_global.unsetRollText = function() {
  _root.onMouseMove = undefined;
  _root.RT.text = "";
  _root.RT._visible = false;
}

// this is a killall to get rid of any running type functions/intervals
_global.killAll = function() {

  // just in case, unset the cursor
  _global.unsetRollText();

  // stop loading the RSS
  _global.killRSSInterval();

  // why not unsubscribe and disconnect from the socket server just in case
  _global.contentLC.send(_global.socketnavListener, "unSubscribe");

}

// ERROR handling / paging
_global.errorPage = function(errorCode) {

  if (_global.DEBUG) {
	 _global.log("errorPage errorCode:"+errorCode);
  }  

  // killAll intervals, subscriptions
  _global.killAll();

  // error page
  getURL("javascript:newWin=window.open('error.swf?code="+errorCode+"','newWin','width=300,height=300,menubar=no,toolbar=no,location=no,statusbar=no,scrollbars=no'); newWin.moveTo(50,50); newWin.focus(); void(0);");

}


// GET UNIQUE MOVIE DEPTH
// INITIALIZE DEPTH at a value that leaves open some stuff for static depths
_global.DEPTH = 100;
_global.DEPTH_HIGH = 10000;

_global.getNewDepth = function(high) {
	// normal
	if (!high) {
	  _global.DEPTH++;
	  return _global.DEPTH;
	}
	// for higher depths not to be caught up to
	else {
	  _global.DEPTH_HIGH++;
	  return _global.DEPTH_HIGH;
	}
}



// sliderChangeHandler -- see live_METRICS30



// DraggablePane Windows
_global.newDraggableWindow = function(PANE_obj) {
  
//   if (_global.DEBUG) {
// 	 _global.log("newDraggableWindow target:"+PANE_obj.target+", instanceName:"+PANE_obj.instanceName+", width:"+PANE_obj.width+", height:"+PANE_obj.height+", x:"+PANE_obj.x+", y:"+PANE_obj.y);
//   }


  // verify it has enough attributes to draw
  if (PANE_obj.target == undefined || PANE_obj.instanceName == undefined || PANE_obj.width == undefined || PANE_obj.height == undefined || PANE_obj.x  == undefined || PANE_obj.y == undefined) {
	 _global.errorPage(98);
	 _global.log("-- width:"+PANE_obj.width+", height:"+PANE_obj.height+", x:"+PANE_obj.x+", y:"+PANE_obj.y);

	 return;
  }

  
  // create a new CSSStyleDeclaration
  var titleStyle = new mx.styles.CSSStyleDeclaration();
  titleStyle.fontStyle = "bold";
  titleStyle.color = 0x000000;
  titleStyle.fontSize = 15;

  // CREATE THE UBER SCREEN OBJECT FOR HOLDING ALL DATA ABOUT DRAWING etc...
  var SCREEN_HANDLE = PANE_obj.target.attachMovie("DraggableWindow", PANE_obj.instanceName, _global.getNewDepth(), {titleBarStyle:titleStyle, _alpha:0});
  
  
  // set blank content
  SCREEN_HANDLE.contentPath = "blank";

  // // paneList / z-order
//   if (PANE_obj.swappable == false) {
// 	 SCREEN_HANDLE.setSwappable(false);
//   }
  
  // width & height
  SCREEN_HANDLE.setSize(PANE_obj.width, PANE_obj.height);

  // style it
  SCREEN_HANDLE.stylize();

  // titleBarHeight
  SCREEN_HANDLE.titleBarHeight = 25;

  // title
  SCREEN_HANDLE.title = PANE_obj.title;
  
  // windowIcon
  SCREEN_HANDLE.windowIcon = "windowIcon";

  // move it
  SCREEN_HANDLE.move(Number(PANE_obj.x), Number(PANE_obj.y));

  // scrollBar policies
  if (PANE_obj.hScrollPolicy != undefined) {
	 SCREEN_HANDLE.hScrollPolicy = PANE_obj.hScrollPolicy;
  }
  if (PANE_obj.VScroll != undefined) {
	 SCREEN_HANDLE.vScrollPolicy = PANE_obj.vScrollPolicy;
  }

  // default isDraggable
  if (PANE_obj.isDraggable == undefined) {
	 if (_global.DEBUG) {
		PANE_obj.draggable = true;
	 }
	 else {
		PANE_obj.draggable = false;
	 }
  }
  else {
	 SCREEN_HANDLE.draggable = PANE_obj.isDraggable;
  }

  // set content for backward compat.
  SCREEN_HANDLE.CONTENT = SCREEN_HANDLE.content;
  
  // fadeIn for niceness
  if (PANE_obj.fadeIn == false) {
	 SCREEN_HANDLE._alpha = 100;
  }
  else {
	 SCREEN_HANDLE.dynTween({duration:10, _alpha:100});
	 //SCREEN_HANDLE._alpha = 100;
  }

  // _global.log("returning SCREEN_HANDLE:"+SCREEN_HANDLE);
  return SCREEN_HANDLE;
}


//   // default hasShader
//   if (PANE_obj.hasShader == undefined) {
// 	 PANE_obj.hasShader = true;
//   }
//   SCREEN_HANDLE.setHasShader(PANE_obj.hasShader);

  
//   // default isResizable
//   if (PANE_obj.isResizable == undefined) {
// 	 PANE_obj.isResizable = false;
//   }
//   SCREEN_HANDLE.setResizable(PANE_obj.isResizable);

  
//   // default hasCloseBox 
//   if (PANE_obj.hasCloseBox == undefined) {
// 	 PANE_obj.hasCloseBox  = false;
//   }
//   SCREEN_HANDLE.setHasCloseBox(PANE_obj.hasCloseBox);

//   // default paneCloseHandler
//   if (PANE_obj.closeHandler != undefined) {
// 	 // _global.log("setting CloseHandler:"+PANE_obj.closeHandler);
// 	 SCREEN_HANDLE.setCloseHandler(PANE_obj.closeHandler);
//   }
  
//   if (PANE_obj.setBackground == true) {
// 	  SCREEN_HANDLE.CONTENT.attachMovie("background","background",_global.getNewDepth());
//   }







// enumerator function
_global.enumerateObject = function(object) {
  for (var key in object) {
	 _global.log("key1:"+key+", val1:"+object[key]);
	 for (var key2 in object[key]) {
		_global.log("-key2:"+key2+", val2:"+object[key][key2]);
	 }
  }
}


// GET ANGLE VALUE GIVEN TWO POINTS
_global.getAngle = function(src, dst) {
  var angle_conversion = 180*(Math.PI/180);
  var change_y = (dst.y-src.y);
  var change_x = (dst.x-src.x);
  var ANGLE = Math.atan(change_y/change_x);
  
  
  if (dst.x < src.x && dst.y <= src.y) {
	 ANGLE += angle_conversion;
  }
  // bottom left
  else if (dst.x < src.x && dst.y >= src.y) {
	 ANGLE -= angle_conversion;
  }
  return ANGLE;
}


// Spits out formatted XML starting at point.x, point.y
_global.layoutXML = function(XMLObj:XML, TARGET:MovieClip, point:Object) {

  var thisSECTION = XMLObj.firstChild;
  while (thisSECTION != null) {
	 var LABEL = thisSECTION.nodeName;
	 var DEPTH = _global.getNewDepth();
	 var CLIP = "SECTION_"+DEPTH;
	 TARGET.createTextField(CLIP, DEPTH, point.x, point.y, 1, 1);
	 TARGET[CLIP].autoSize = true;
	 TARGET[CLIP].selectable = false;
	 TARGET[CLIP].text = LABEL;
	 TARGET[CLIP].setTextFormat(_global.XMLHeadFormat);
	 point.y += 30;
	 
	 var thisDOC = thisSECTION.firstChild;
	 while (thisDOC != null) {
		var DEPTH = _global.getNewDepth();
		var CLIP = "LINK_"+DEPTH;
		var LINK = thisDOC.attributes.link;
		var LABEL = thisDOC.attributes.label;
		TARGET.createTextField(CLIP, DEPTH, point.x, point.y, 300, 1);
		TARGET[CLIP].autoSize = true;
		TARGET[CLIP].selectable = false;
		TARGET[CLIP].useHandCursor = false;
		TARGET[CLIP].styleSheet = _global.style_sheet;
		TARGET[CLIP].html = true;
		TARGET[CLIP].htmlText = "<A HREF='"+LINK+"' TARGET='_BLANK'>"+LABEL+"</A>";
		thisDOC = thisDOC.nextSibling;
		point.y += 15;
	 }
	 point.y += 20;
	 thisSECTION = thisSECTION.nextSibling;
  }
  
}



// create a menu at a point based on a XML File
_global.XMLMenu = function(menuObj:Object) {

  //menuObj.ROLE:String, ID:Number, IP:String, TARGET:MovieClip, point:Object

  // debug
//   if (_global.DEBUG) {
// 	 _global.log("_global.XMLMenu called, ROLE:"+menuObj.ROLE+", ID:"+menuObj.ID+", IP:"+menuObj.IP);
//   }

  // call the Hide function
  _global.XMLMenuHide();

  // unset any roll text
  _global.unsetRollText();

  // instantiate the provider
  var menuProvider:XML = new XML();

  // parse xml to create the links here
  var docXML:XML = new XML();
  docXML.ignoreWhite = true;
  docXML.onLoad = function(success) {
	 if (success) {
		var MENU = docXML.firstChild;
		while (MENU != null) {
		  var LINK = MENU.firstChild;
		  while (LINK != null) {

			 // so we can be generic about our attribute other than label
			 for (var attribute in LINK.attributes) {
				// certain attributes are more powerful than others
				if (attribute != "label" && attribute != "args" && attribute != "height" && attribute != "width") {
				  var ACTION = attribute;
				  var VALUE = LINK.attributes[attribute];

				  // searchReplace
				  // we iterate over the replace Object
				  for (var search in menuObj.replace) {
					 VALUE = VALUE.searchReplace("$"+search, menuObj.replace[search]);
				  }
				}
				else if (attribute == "args") {
				  var ARGS = LINK.attributes[attribute];
				  // searchReplace
				  // we iterate over the replace Object
				  for (var search in menuObj.replace) {
					 ARGS = ARGS.searchReplace("$"+search, menuObj.replace[search]);
				  }
				}

				

			 }


			 // add onto provider
			 //_global.log("XMLMenu: label:"+LINK.attributes.label+", action:"+ACTION+", value:"+VALUE+", args:"+LINK.attributes.args+", newwin:"+LINK.attributes.newwin);
			 menuProvider.addMenuItem({label:LINK.attributes.label, action:ACTION, value:VALUE, args:ARGS, height:LINK.attributes.height, width:LINK.attributes.width, newwin:LINK.attributes.newwin});

			 LINK = LINK.nextSibling;
		  }
		  MENU = MENU.nextSibling;
		}

		// MENU 
		// Attach and show the menu at a monster high depth
		_global.thisXMLMenu = menuObj.TARGET.attachMovie("Menu", "thisMenu", _global.DEPTH_HIGH*2);
		_global.thisXMLMenu.stylize();
		_global.thisXMLMenu.ID = menuObj.ID;
		_global.thisXMLMenu.ROLE = menuObj.ROLE;
		_global.thisXMLMenu.dataProvider = menuProvider;

		// collision test: move to left if menu too wide for target
		var x = menuObj.point.x;
		var test = 150-(menuObj.TARGET._width-x);
		if (test > 0) {
		  x -= test;
		}
		_global.thisXMLMenu.show(x, menuObj.point.y);


		// add a Mouse listener so that we close it on next mouseUp UNLESS
		// we happen to be over the menu, in which case, we'll call change
		Mouse.addListener(_global.XMLMenuListener);

		// call changeHandler
		_global.thisXMLMenu.addEventListener("change", _global.XMLMenuListener);

	 }
	 // error page
	 else {
		_global.errorPage(4);
	 }
  }
  // load it in each time!
  var xmlCacheBust:Number = Math.random();
  docXML.load("param?name=popup&context="+menuObj.ROLE+"&xmlCacheBust="+xmlCacheBust);
}

// Hide that Menu
_global.XMLMenuHide = function() {
  
  //_global.log("XMLMenuHide called:"+_global.thisXMLMenu.ROLE+"_"+_global.thisXMLMenu.ID);

  // hide any others
  if (_global.thisXMLMenu) {
	 _global.thisXMLMenu.hide();

	 // shrink ATK/DEF if that's the context
	 
	 _global.thisXMLMenu.removeMovieClip();
	 delete _global.thisXMLMenu;
  }
}

_global.XMLMenuListener = new Object();

_global.XMLMenuListener.onMouseUp = function(event) {
  //_global.log("XMLMenuListener.onMouseUp");
  // if we're not over the menu
  if (!_global.thisXMLMenu.hitTest(_root._xmouse, _root._ymouse, false)) {
	 //_global.log("NOT over a menu");
	 _global.XMLMenuHide();
	 Mouse.removeListener(_global.XMLMenuListener);
  }
}

_global.XMLMenuListener.change = function(event) {
  var item = event.menuItem;
  var index = event.target.indexOf(item);

  // shrink ATK/DEF (if applicable)
  //SCREEN.ATKDEF.CONTENT[event.target.ROLE+"_"+event.target.ID].ROLE._xscale = SCREEN.ATKDEF.CONTENT[event.target.ROLE+"_"+event.target.ID].ROLE._yscale = 100;
  //SCREEN.ATKDEF.CONTENT[event.target.ROLE+"_"+event.target.ID].NOPURGE = 0;

  // open an external url
  if (item.attributes.action == "url") {

	 // set default height and width
	 if (!item.attributes.width) {
		item.attributes.width = _global.NEW_WINDOW_WIDTH;
		item.attributes.height = _global.NEW_WINDOW_HEIGHT;
	 }

	 // getURL
	 // if target is set, do a javascript pop
	 if (item.attributes.newwin) {
		// pop the window out
		getURL("javascript:newWin=window.open('"+item.attributes.value+"','newWin','width="+item.attributes.width+",height="+item.attributes.height+",menubar=yes,toolbar=yes,location=yes,statusbar=yes,scrollbars=yes'); newWin.moveTo(200,100); newWin.focus(); void(0);");
	 }
	 // otherwise get it in the content window & unsubscribe from VIZ etc..
	 else {
		_global.contentLC.send(_global.socketnavListener, "unSubscribe");
		getURL(item.attributes.value);
	 }
	 


  }

  // pass off a system command to the XML Socket
  else if (item.attributes.action == "command") {
	 var CMD:String = "<COMMAND line='"+item.attributes.value+"' />";
	 if (_global.DEBUG) {
		_global.log("XMLMenuListener.change SENDING XML_SOCKET command:"+CMD);
	 }
	 // send it off!
	 _global.XML_SOCKET.send(CMD);
  }

  // run a function
  else if (item.attributes.action == "func") {
	 var FUNC:String = item.attributes.value;
	 var ARGS:String = item.attributes.args;
	 var ARGSarray:Array = ARGS.split(",");

	 _global.log("ARGSarray:"+ARGSarray.join(","));

	 if (_global.DEBUG) {
		_global.log("XMLMenuListener.change dub FUNCTION CALL:"+FUNC+", args:"+item.attributes.args);
	 }
	 //:REDO - this is ugly for ARGS
	 eval(FUNC)(ARGSarray[0],ARGSarray[1],ARGSarray[2],ARGSarray[3],ARGSarray[4]);
  }

}



// ----- MOVIE CLIP LOADER
_global.ContentListener = new Object();


ContentListener.onLoadStart = function(movieClip:Object) {
  if (_global.DEBUG) {
	 //_global.log("_global.MovieClipLoader ContentListener.onLoadStart: "+movieClip._url);
  }
}

ContentListener.onLoadComplete = function(movieClip:Object) {
  if (_global.DEBUG) {
	 //_global.log("_global.MovieClipLoader ContentListener.onLoadComplete: "+movieClip._url);
  }
}

ContentListener.onLoadError = function (movieClip:Object, errorCode:String) {
  if (_global.DEBUG) {
	 _global.errorPage(97);
  }
  _global.log("Error loading external swf file.\nPlease check all swf file permissions.\nmovieClip._url:"+movieClip._url+"\nErrorcode:"+errorCode);
	 
}

_global.MCLoader = new MovieClipLoader();
_global.MCLoader.addListener(ContentListener);
// ----- END MOVIE CLIP LOADER
