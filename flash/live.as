//------------------------------------------------------------------//
// @LICENSE@
//
// LIVE
// authors: Lindsey Simon <lsimon@stream.com>,
//          Paco Nathan <paco@stream.com> 
//------------------------------------------------------------------//

// Screenshape definitions
var ScreenShapes = new Array();
ScreenShapes['internal'] = new Object();
ScreenShapes['internal'].label = _global.ATKDEF_THREAT_MENU_INTERNAL;
ScreenShapes['internal'].data = "internal";
ScreenShapes['internal'].ATKDEF_WIDTH = _global.ATKDEF_WIDTH;
ScreenShapes['internal'].ATKDEF_HEIGHT = _global.ATKDEF_HEIGHT;
ScreenShapes['internal'].ROLE_SPACING = _global.ATKDEF_ROLE_SPACING_IN;
ScreenShapes['internal'].HSCROLL = "off";
ScreenShapes['internal'].VSCROLL = "off";

ScreenShapes['perimeter'] = new Object();
ScreenShapes['perimeter'].label = _global.ATKDEF_THREAT_MENU_PERIMETER;
ScreenShapes['perimeter'].data = "perimeter";
ScreenShapes['perimeter'].ATKDEF_WIDTH = _global.ATKDEF_WIDTH;
ScreenShapes['perimeter'].ATKDEF_HEIGHT = _global.ATKDEF_HEIGHT;
ScreenShapes['perimeter'].ROLE_SPACING = _global.ATKDEF_ROLE_SPACING_PER;
ScreenShapes['perimeter'].HSCROLL = "off"; 
ScreenShapes['perimeter'].VSCROLL = "on"; 

/*
ScreenShapes['model'] = new Object();
ScreenShapes['model'].label = "Model Editor";
ScreenShapes['model'].data = "model";
ScreenShapes['model'].ATKDEF_WIDTH = _global.ATKDEF_WIDTH;
ScreenShapes['model'].ATKDEF_HEIGHT = _global.ATKDEF_HEIGHT;
ScreenShapes['model'].ROLE_SPACING = _global.ATKDEF_ROLE_SPACING_ME;
ScreenShapes['model'].HSCROLL = false;
ScreenShapes['model'].VSCROLL = true;
*/


//---  START A SCREEN and add everything else to it
var WHOLE_SCREEN = _global.newDraggableWindow({target:_root.UI_CONTENT, instanceName:"WHOLE_SCREEN", width:_global.SCREEN_WIDTH, height:_global.SCREEN_HEIGHT, alpha:100,  x:0, y:0, title:_global.SCREEN_TITLE+" :: "+_global.SCREEN_TITLE_LIVE, isScrolling:false, fadeIn:false});


// SET SCREEN
_global.SCREEN = WHOLE_SCREEN.CONTENT;

// enable Menu
WHOLE_SCREEN.enableMenuBar();




// ATKDEF menu listener
SCREEN.MenuListener = new Object();
SCREEN.MenuListener.change = function(event) {
  var item = event.menuItem;
  var index = event.target.indexOf(item);
  var instanceName = item.attributes.instanceName;
  var groupName = item.attributes.groupName;

  if (_global.DEBUG) {
	 _global.log("SCREENMenuListener item:"+item+", instanceName:"+instanceName+", groupName:"+groupName+", target:"+event.target);
  }

  // refresh
  if (groupName == "refresh") {
	 _global.VIZ_REFRESH = instanceName;
	 event.target.setMenuItemEnabled(item, false);
	 
	 // ENABLE THE OTHERS
	 var length = _global.refreshs_menu.length;
	 for (var i=0; i<length; i++) {
		if (_global.refreshs_menu[i].data != instanceName) {
		  // add it on proper now
		  event.target.setMenuItemEnabled(event.target.getMenuItemAt(i));
		}
	 }
  }
  // throttle
  else if (groupName == "throttle") {
	 _global.VIZ_THROTTLE = instanceName;
	 event.target.setMenuItemEnabled(item, false);
	 
	 // ENABLE THE OTHERS
	 var length = _global.THROTTLE_MENU_OPTIONS.length;
	 for (var i=0; i<length; i++) {
		if (_global.THROTTLE_MENU_OPTIONS[i] != instanceName) {
		  // add it on proper now
		  event.target.setMenuItemEnabled(event.target.getMenuItemAt(i));
		}
	 }
  }


  // network
  else if (groupName == "network_id") {
	 _global.VIZ_NETWORK_ID = instanceName;
	 event.target.setMenuItemEnabled(item, false);
	 
	 // ENABLE THE OTHERS
	 var length = SCREEN.DATA.NETS.length;
	 for (var i=0; i<length; i++) {
		if (SCREEN.DATA.NETS[i].data != instanceName) {
		  // add it on proper now
		  event.target.setMenuItemEnabled(event.target.getMenuItemAt(i));
		}
	 }
  }
  
  // unsubscribe and then resubscribe to viz now
  _global.contentLC.send(_global.socketnavListener, "unSubscribe", {stream:"viz"});
  //_global.XML_SOCKET.unSubscribe({stream:"viz"});
  _global.contentLC.send(_global.socketnavListener, "subscribe", {stream:"viz", delay:_global.VIZ_DELAY, refresh:_global.VIZ_REFRESH, activity:_global.VIZ_ACTIVITY, threat:_global.VIZ_THREAT, network_id:_global.VIZ_NETWORK_ID, throttle:_global.VIZ_THROTTLE});

}


// Refresh menu 
_global.refreshs_menu = new Array();
for (var i=1; i<10; i++) {
  if (i == "1") {
	 _global.refreshs_menu.push({label:"real time", data:i*1000});
  }
  else {
	 _global.refreshs_menu.push({label:i+" sec.", data:i*1000});
  }
}
var refresh_menu = WHOLE_SCREEN.menuBar.addMenu(_global.SCREEN_MENU_REFRESH_TITLE);
var length = _global.refreshs_menu.length;
for (var i=0; i<length; i++) {
  if (_global.VIZ_REFRESH == _global.refreshs_menu[i].data) {
	 refresh_menu.addMenuItem({ label:_global.refreshs_menu[i].label, type:"radio", selected:true, enabled:false, instanceName:_global.refreshs_menu[i].data, groupName:"refresh" });
  }
  else {
	 refresh_menu.addMenuItem({ label:_global.refreshs_menu[i].label, type:"radio", selected:false, enabled:true, instanceName:_global.refreshs_menu[i].data, groupName:"refresh" });
  }
}
refresh_menu.addEventListener("change", SCREEN.MenuListener);



// Throttle menu
var throttle_menu = WHOLE_SCREEN.menuBar.addMenu(_global.SCREEN_MENU_THROTTLE_TITLE);
var length = _global.THROTTLE_MENU_OPTIONS.length;
for (var i=0; i<length; i++) {
  if (_global.VIZ_THROTTLE == _global.THROTTLE_MENU_OPTIONS[i]) {
	 throttle_menu.addMenuItem({ label:_global.THROTTLE_MENU_OPTIONS[i], type:"radio", selected:true, enabled:false, instanceName:_global.THROTTLE_MENU_OPTIONS[i], groupName:"throttle" });
  }
  else {
	 throttle_menu.addMenuItem({ label:_global.THROTTLE_MENU_OPTIONS[i], type:"radio", selected:false, enabled:true, instanceName:_global.THROTTLE_MENU_OPTIONS[i], groupName:"throttle" });
  }
}
throttle_menu.addEventListener("change", SCREEN.MenuListener);



// Network menu
_global.network_menu = WHOLE_SCREEN.menuBar.addMenu(_global.SCREEN_MENU_NETWORK_TITLE);
_global.network_menu.addEventListener("change", SCREEN.MenuListener);




// init the DATA model
SCREEN.DATA = new Object();

// LOAD IN THE CONFIG
#include "live_config.as"

// the Attackers and Defenders MenuListener, Profiles, etc ...
#include "live_ATKDEF.as"



// SCREEN glow function
WHOLE_SCREEN.glowRed = function() {
  this.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.ATKDEF.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.ALT.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.METRIC.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.GRAPH.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.HISTOGRAM.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

  this.CONTENT.RSS.titleArea_mc.dynColorTween(
							{duration:7, rb:255},
							{duration:7, rb:0, callback: this + ".glowRed"}
							)

}

WHOLE_SCREEN.unGlowRed = function() {
  this.titleArea_mc.colorReset({rb:0});
  this.CONTENT.ATKDEF.titleArea_mc.colorReset({rb:0});
  this.CONTENT.ALT.titleArea_mc.colorReset({rb:0});
  this.CONTENT.METRIC.titleArea_mc.colorReset({rb:0});
  this.CONTENT.GRAPH.titleArea_mc.colorReset({rb:0});
  this.CONTENT.HISTOGRAM.titleArea_mc.colorReset({rb:0});
  this.CONTENT.RSS.titleArea_mc.colorReset({rb:0});
}



// initialize LINEKEYS array for storing on screen line references
//SCREEN.LINEKEYS = new Array();

// init colors
var COLOR_VULN = new Array();
var COLOR_THREAT = new Array();


// Create a menu for the ScreenShapes views
_global.threats_menu = new Array();
for (var SHAPE in ScreenShapes) {
  _global.threats_menu.push({label:ScreenShapes[SHAPE].label, data:ScreenShapes[SHAPE].data});
}
_global.threats_menu.push({label:"Combined", data:"combined"})


// For the position pool
var XY_Positions = function(ROLE:String, THREAT:String, TARGET:MovieClip) {
  
  TARGET.center = TARGET._parent.getCenter();

  var ROLE_SPACING:Number = ScreenShapes[THREAT].ROLE_SPACING;
  if (!ROLE_SPACING) {
	 ROLE_SPACING = 15;
  }
  TARGET["ATKDEF_"+ROLE+"_LIMIT"] = _global.VIZ_THROTTLE;

  if (_global.DEBUG) {
	 //_global.log("XY_Positions for threat:"+THREAT+", role:"+ROLE+", SPACING:"+ROLE_SPACING+"LIMIT:"+TARGET["ATKDEF_"+ROLE+"_LIMIT"]);
  }

  if (THREAT == "internal") {
	 var radius:Number = _global[ROLE+"_RADIUS"];
	 var circumference:Number = (2*radius)*Math.PI;
	 TARGET["ATKDEF_"+ROLE+"_LIMIT"] = Math.round(circumference/ROLE_SPACING);


	 var angle_inc:Number = Math.round(360/TARGET["ATKDEF_"+ROLE+"_LIMIT"]);
	 var angle:Number = 0;
  }
  else if (THREAT == "perimeter") {
	 TARGET.ATKDEF_ROLE_TOP = 20;
	 TARGET.ATKDEF_ROLE_BOTTOM = (100*(10+ROLE_SPACING))-TARGET.ATKDEF_ROLE_TOP;
	 TARGET.ATKDEF_ROLE_LEFT = 110;
	 TARGET.ATKDEF_ROLE_RIGHT = TARGET._parent.width-TARGET.ATKDEF_ROLE_LEFT-15;

  }
//   else if (_global.VIZ_THREAT == "model") {
// 	 SCREEN.ATKDEF_ROLE_TOP = 20;
// 	 SCREEN.ATKDEF_ROLE_BOTTOM = (100*(10+ROLE_SPACING))-SCREEN.ATKDEF_ROLE_TOP;
// 	 SCREEN.ATKDEF_ROLE_LEFT = 20;
// 	 SCREEN.ATKDEF_ROLE_RIGHT = ScreenShapes[_global.VIZ_THREAT].ATKDEF_WIDTH-ROLE_SPACING;
// 	 var Y:Number = SCREEN.ATKDEF_ROLE_TOP;
// 	 var X:Number = SCREEN.ATKDEF_ROLE_LEFT;
// 	 var modulo:Number = Math.round((ScreenShapes[_global.VIZ_THREAT].ATKDEF_WIDTH-50)/ROLE_SPACING);

//   }
  

  if (_global.DEBUG) {
	 //_global.log("XY_Positions: SCREEN_"+ROLE+"_LIMIT: "+TARGET["ATKDEF_"+ROLE+"_LIMIT"]+", TOP:"+SCREEN.ATKDEF_ROLE_TOP+", BOTTOM:"+SCREEN.ATKDEF_ROLE_BOTTOM+", LEFT:"+SCREEN.ATKDEF_ROLE_LEFT+", RIGHT:"+ SCREEN.ATKDEF_ROLE_RIGHT);
  }

  var i:Number = -1;
  var LIMIT:Number = TARGET["ATKDEF_"+ROLE+"_LIMIT"];
  while (++i < LIMIT) {
	 this[i] = new Array();
	 // perimeter
	 if (THREAT == "perimeter") {
		this[i].y = TARGET.ATKDEF_ROLE_TOP + (i*ROLE_SPACING);
		
		// we need to know whether its an atk or def for rectangle
		if (ROLE == "ATK") {
        this[i].x = TARGET.ATKDEF_ROLE_LEFT;
		}
		else if (ROLE == "DEF") {
        this[i].x = TARGET.ATKDEF_ROLE_RIGHT;
		}
	 }
	 // internal
	 else if (THREAT == "internal") {
      this[i].x = Math.round(TARGET.center.x + (Math.cos(angle) * radius));
		this[i].y = Math.round((TARGET.center.y-20) + (Math.sin(angle) * radius));

		//_global.log("angle:"+angle+",angle_inc:"+angle_inc+", rad:"+radius);
		

		if ((i != 0 ) && (i % 2 == 0)) {
		  angle += 180;
		}
		else {
		  angle += angle_inc;
		}
	 }

	 //_global.log(ROLE+", threat:"+THREAT+", i:"+i+", X:"+this[i].x+", Y:"+this[i].y);

	 // model editor
// 	 else if (_global.VIZ_THREAT == "model") {
// 		// reset X, add to Y
// 		if ((i != 0) && (i % modulo == 0)) {
// 		  X = SCREEN.ATKDEF_ROLE_LEFT;
// 		  Y += ROLE_SPACING;
// 		}
// 		this[i].x = X;
// 		this[i].y = Y;
// 		X += ROLE_SPACING;
		
// 	 }

	 // HORIZONTAL
// 	 else if (_global.VIZ_THREAT == "missile_command") {
// 		this[i].x = SCREEN.ATKDEF_ROLE_LEFT + (i*ROLE_SPACING);
		
// 		// we need to know whether its an atk or def for rectangle
// 		if (ROLE == "ATK") {
//         this[i].y = SCREEN.ATKDEF_ROLE_TOP;
// 		}
// 		else if (ROLE == "DEF") {
//         this[i].y = SCREEN.ATKDEF_ROLE_BOTTOM;
// 		}
// 	 }
	 
	 // SET IT SO IT'S FREE FREE FREE
	 this[i].used = 0;
  }
}


// GET FIRST AVAILABLE POSITION IN POOL OF XY POSITIONS FOR ATK/DEF
XY_Positions.prototype.getFirstAvailablePosition = function(ROLE, TARGET) {

  //_global.log("getFirstAvailablePosition ROLE:"+ROLE+", target:"+TARGET);
  var point:Object = new Object;
  var LIMIT:Number = TARGET["ATKDEF_"+ROLE+"_LIMIT"];
  var i:Number = -1;
  while (++i < LIMIT) {
	 if (this[i].used == 0) {
		this[i].used = 1;
		point.x = this[i].x;
		point.y = this[i].y;
		
		// set the index so we can tie to instance for freeing up when purged
		point.index = i;

		//_global.log("returning point.x:"+point.x+", point.y:"+point.y);

		return point;
	 }
  }
}
  

// Add a movieclip prototype for drawing ATKDEF lines
var drawATKDEFLine = function(TARGET, src, dst, ANGLE, TARGET_COLOR, IDS, ALT_ID, dst_id) {
  
  if (_global.DEBUG) {
	 _global.log("drawATKDEFLine src:"+src+" dst:"+dst+" ALERT:"+ALT_ID+", src:"+SCREEN.DATA.ALT[ALT_ID]["source"]);
  }


  // if no color, set to default
  if (!TARGET_COLOR) {
	 var TARGET_COLOR = COLOR_VULN[0].color;
  }
  
  // check for PIX and reset dst.x to PIX border line
  if (SCREEN.DATA.ALT[ALT_ID]["source"] == "PIX") {
	 dst.x = SCREEN.PIX_X;
  }

  var LINE_ALPHA:Number = _global.ALPHA;
  
  TARGET.lineStyle(1, TARGET_COLOR, LINE_ALPHA);
  TARGET.moveTo(src.x, src.y);
  TARGET.lineTo(dst.x, dst.y);
  
  // 2 more lines above and below to give 3D effect
  TARGET.lineStyle(2, TARGET_COLOR, LINE_ALPHA-10);
  TARGET.moveTo(src.x, src.y);
  TARGET.lineTo(dst.x, dst.y+1);
  TARGET.moveTo(src.x, src.y);
  TARGET.lineTo(dst.x, dst.y-1);
  

  // CREATE HOVER CLIP TO MAKE LINE STAND OUT ONROLLOVER
  TARGET.createEmptyMovieClip("HOVER", _global.getNewDepth());
  var HOVER_WRAP:Number = 4;
  
  // IF THERE'S AN IDS ATTRIBUTE, DRAW A CIRCLE ON DST
  if (IDS) {
	 TARGET.lineStyle(1, COLOR_THREAT[IDS].color, LINE_ALPHA);
	 TARGET.moveTo(src.x, src.y);
	 TARGET.lineTo(dst.x, dst.y+3);
	 TARGET.moveTo(src.x, src.y);
	 TARGET.lineTo(dst.x, dst.y-3);
	 TARGET.linestyle = "";
	 
	 // use dstPort as a unique identifier for the circle cause there could be > 1 circle
	 TARGET.createEmptyMovieClip(IDS, _global.getNewDepth());
	 
	 var IDS_ALERT_RADIUS = 4;

	 // figure out the dst_id role
	 var dstRole = SCREEN.getRole(dst_id, TARGET._parent);
	 
	 // GROW THE ALT CIRCLE
	 if (TARGET._parent[dstRole+"_"+dst_id]) {
      if (TARGET._parent[dstRole+"_"+dst_id].IDS_ALERT_WIDTH && TARGET._parent[dstRole+"_"+dst_id].IDS_ALERT_WIDTH >= 4) {
        TARGET._parent[dstRole+"_"+dst_id].IDS_ALERT_WIDTH += 3;
        IDS_ALERT_RADIUS = TARGET._parent[dstRole+"_"+dst_id].IDS_ALERT_WIDTH;
        TARGET[IDS].drawCircle(IDS_ALERT_RADIUS-2, dst.x, dst.y, 0, 1, "0xFFFFFF", _global.ALPHA);
        TARGET[IDS].drawCircle(IDS_ALERT_RADIUS-1, dst.x, dst.y, 0, 2, COLOR_THREAT[IDS].color, _global.ALPHA);
		}
		else {
        TARGET._parent[dstRole+"_"+dst_id].IDS_ALERT_WIDTH = IDS_ALERT_RADIUS;
        TARGET[IDS].drawCircle(IDS_ALERT_RADIUS, dst.x, dst.y, COLOR_THREAT[IDS].color, 0, "0xFFFFFF", _global.ALPHA);
		}
	 }

	 // if ids, make a light up circle too
	 TARGET.HOVER.createEmptyMovieClip("IDS", _global.getNewDepth());
	 TARGET.HOVER.IDS.drawCircle(IDS_ALERT_RADIUS+1, dst.x, dst.y, 0, 0, "0x333333", _global.ALPHA);
	 TARGET.HOVER.IDS._visible = false;
	 // TARGET.ATKDEF.CONTENT[LINEKEY][dstPort].attachMovie("alert", "Alert", _global.getNewDepth());
	 HOVER_WRAP++;
  }
  
  // HOVER
  TARGET.HOVER.lineStyle(1, "0x999999", LINE_ALPHA);
  TARGET.HOVER.moveTo(src.x, src.y);
  TARGET.HOVER.lineTo(dst.x, dst.y+HOVER_WRAP);
  TARGET.HOVER.moveTo(src.x, src.y);
  TARGET.HOVER.lineTo(dst.x, dst.y-HOVER_WRAP);
  // default to not visible
  TARGET.HOVER._visible = false;

  TARGET.attachMovie("mask", "MASK", _global.getNewDepth());
  TARGET.MASK._x = src.x;
  TARGET.MASK._y = src.y;
  TARGET.setMask(TARGET.MASK);
  
  // Make a mask and move it in the right direction to simulate movement
  TARGET.onEnterFrame = function() {
	 if (TARGET.MASK._width < 300) {
		TARGET.MASK._width = TARGET.MASK._height += 120;
	 }
	 else {
		TARGET.MASK.removeMovieClip();
		TARGET.setMask();
		return;
	 }
  }
}

// Line Animation FOR TRAFFIC FLOW dots
var LineAnimate = function (movieclip, LINE_LENGTH, XMOV, YMOV) {
  movieclip._x += XMOV;
  movieclip._y += YMOV;
  movieclip.LENGTH += _global.LINE_ANIM_SPEED;

  // reset when the X value has matched the length
  if (movieclip.LENGTH >= LINE_LENGTH) {
	 movieclip._x = 0;
	 movieclip._y = 0;
	 movieclip.LENGTH = 0;
  }
}






// returns either ATK or DEF
SCREEN.getRole = function(ID:Number, TARGET:MovieClip) {
  if (TARGET["DEF_"+ID]) {
	 var Role = "DEF";
  }
  else if (TARGET["ATK_"+ID]) {
	 var Role = "ATK";
  }
  return Role;
}

// clear the SCREEN
SCREEN.clearScreen = function() {
  if (_global.DEBUG) {
	 _global.log("clearScreen called");
  }

  // clear intervals
  // this.clearScreenIntervals();
  _global.log("LAG INTERVAL WAS:"+this.lagInterval);
  clearInterval(this.lagInterval);
  _global.log("LAG INTERVAL NOW:"+this.lagInterval);

  SCREEN.deleteMovieClips();

}


// REMOVE AN ATKDEFLine
SCREEN.ClearATKDEFLine = function(LINEKEY:MovieClip, TARGET:MovieClip, LINEKEYS_position:Number) {

  _global.unsetRollText();

  // :REDO possible to overwrite one - you subtract 4 and then you're at where one is already

  TARGET["DEF_"+TARGET[LINEKEY].DST_ID].IDS_ALERT_WIDTH -= 3;
  TARGET["ATK_"+TARGET[LINEKEY].DST_ID].IDS_ALERT_WIDTH -= 3;

  
  var linekey_anim = LINEKEY+"_anim";
  clearInterval(eval(linekey_anim));
  delete eval(linekey_anim);

  TARGET[LINEKEY].anim.removeMovieClip();
  delete TARGET[LINEKEY].anim;

  TARGET[LINEKEY].removeMovieClip();
  delete TARGET[LINEKEY];
  
  // CLEAN UP LINEKEYS ARRAY
  if (LINEKEYS_position) {
    TARGET.LINEKEYS.splice(LINEKEYS_position,1);
  }
  else {
	 var LINEKEYS_TOP = (TARGET.LINEKEYS.length - 1);
	 for (var i=LINEKEYS_TOP; i>=0; i--) {
    	if (TARGET.LINEKEYS[i] == LINEKEY) {
		  TARGET.LINEKEYS.splice(i,1);
		  return;
		}
	 }
  }
}



// CLEAR SCREEN and reset the XY_Positions pool to match new SHAPE
SCREEN.SwapATKDEF = function(screenShape) {
  if (_global.DEBUG) {
	 _global.log("SwapATKDEF to:"+screenShape);
	 _global.log("SCREEN:"+SCREEN);
  }
  //return;
  
  // SET THE NEW SHAPE
  _global.VIZ_THREAT = screenShape;

  var targets:Array = new Array();

  if (SCREEN.ATKDEF_PERIMETER) {
	 targets.push("PERIMETER");
  }
  if (SCREEN.ATKDEF_INTERNAL) {
	 targets.push("INTERNAL");
  }

  var length:Number = targets.length;
  for (var i=0; i<length; i++) {
	 var TARGET:MovieClip = SCREEN["ATKDEF_"+targets[i]];

	 // get rid of animations
	 if (_global.LINE_ANIM) {
		var length2 = TARGET.CONTENT.LINEKEYS.length;
		for (z=0; z<length2; z++) {
		  var LINEKEY:String = TARGET.CONTENT.LINEKEYS[z];
		  var linekey_anim = LINEKEY+"_anim";
		  clearInterval(eval(linekey_anim));
		}
	 }
  
	 // dump it
	 TARGET.removeMovieClip();
  }
  
  // tell the screen to redraw the ATKDEF
  this.initATKDEF = false;
  
}



//ATKDEF Borders
SCREEN.drawATKDEFBorders = function(TARGET:MovieClip) {
  
  // make movie clip for border lines
  var border_mc:MovieClip = TARGET.createEmptyMovieClip("border_mc", 2);
  
  // circles
  if (TARGET.THREAT == "internal") {
	 
	 border_mc.drawCircle(_global.DEF_RADIUS, TARGET.center.x, TARGET.center.y-20, 0, 0, "0x006600", _global.ALPHA);
	 
  }
  // straight lines on two Xs
  else if (TARGET.THREAT == "perimeter") {

	 // ATK
	 border_mc.lineStyle(1, 0x990000, _global.ALPHA);
	 border_mc.moveTo(TARGET.ATKDEF_ROLE_LEFT, TARGET.ATKDEF_ROLE_TOP);
	 border_mc.lineTo(TARGET.ATKDEF_ROLE_LEFT, TARGET.ATKDEF_ROLE_BOTTOM);
	 
	 
	 // DEF
	 border_mc.lineStyle(1, 0x006600, _global.ALPHA);
	 border_mc.moveTo(TARGET.ATKDEF_ROLE_RIGHT, TARGET.ATKDEF_ROLE_TOP);
	 border_mc.lineTo(TARGET.ATKDEF_ROLE_RIGHT, TARGET.ATKDEF_ROLE_BOTTOM);

	 // PIX
	 border_mc.lineStyle(1, 0xCC0000, _global.ALPHA);
	 SCREEN.PIX_X = (TARGET.ATKDEF_ROLE_RIGHT-25);
	 border_mc.moveTo(SCREEN.PIX_X, TARGET.ATKDEF_ROLE_TOP);
	 border_mc.lineTo(SCREEN.PIX_X, TARGET.ATKDEF_ROLE_BOTTOM);
	 

  }
}

// PASS OFF THE DATA FOR DRAWING THE SCREEN
// -- draw Attackers and Defenders --// 
SCREEN.drawATKDEF = function(ROLE:String, TARGET:MovieClip) {

  //_global.log("ROLE:"+ROLE+", THREAT:"+TARGET.THREAT);

  // DEBUG
  if (_global.DEBUG && _global.TIMESTAMPS) {
	 var startATKDEF = getTimer();
  }
 
  // START DRAWING THE ATTACKERS AND DEFENDERS
  var ATKDEF_LENGTH = this.DATA[ROLE].length;
  var i = ATKDEF_LENGTH++;
  while (--i >= 0) {
	 var HOST_ID = this.DATA[ROLE][i]['id'];
	 var HOST_IP = this.DATA[ROLE][i]['ip'];
	 var OS = this.DATA[ROLE][i]['os'];
	 var instance = ROLE+"_"+HOST_ID;

	 // MAKE SURE IT'S NOT ON THE STAGE ALREADY
	 if (!TARGET[instance]) {
    	// get available position
		var point = TARGET[ROLE+"_pool"].getFirstAvailablePosition(ROLE, TARGET);
		
		// IF WE GET A VALID POSITION
		if (point) {

		  //_global.log("Valid ROLE:"+ROLE+", THREAT:"+TARGET.THREAT+", point:"+point.x+","+point.y);

		  // CONTAINER CLIP
		  var MACHINE = TARGET.createEmptyMovieClip(instance, _global.getNewDepth(1));

		  // set some properties
		  MACHINE.ID = HOST_ID;
		  MACHINE.IP = HOST_IP;
		  MACHINE.OS = OS;

		  MACHINE.index = point.index;
		  MACHINE.useHandCursor = false;
		  MACHINE._alpha = _global.ALPHA;

		  // ATTACH MACHINE ICON placeholder
		  MACHINE.attachMovie("ROLE", "ROLE", _global.getNewDepth(1));

		  MACHINE.ROLE.createEmptyMovieClip("ICON", _global.getNewDepth(1));
		  MACHINE.ROLE.ICON._y -= 8;
		  MACHINE.ROLE.ROLE = ROLE;

		  // defender
		  if (ROLE == "DEF") {
			 MACHINE.ROLE.rollText = OS;

			 // convert ?? to unknown
			 if (OS == "??" || !OS) {
				OS = "unknown";
			 }
			 
			 // move def icons
			 MACHINE.ROLE.ICON._x -= 6;

			 // set the icon
			 MACHINE.icon= "images/os/"+OS.searchReplace(" ","_")+".jpg";
			 
		  }
		  // attacker
		  else {
			 MACHINE.ROLE.ICON._x -= 8;
			 MACHINE.ROLE.ICON._y += 1;
			 
			 var CC = this.DATA[ROLE][i]['cc'];

			 // convert ?? to unknown
			 if (CC == "??" || !CC) {
				CC = "unknown";
			 }

			 MACHINE.icon = "images/flags/"+CC+".jpg";
			 MACHINE.location = this.DATA[ROLE][i]['country'];
			 MACHINE.ROLE.rollText = MACHINE.location;
		  }

		  // load the icon in
		  _global.MCLoader.loadClip(MACHINE.icon, MACHINE.ROLE.ICON);
		  

		  // set these points for connection line drawing, then we can move the icons
		  MACHINE.ROLE.orig_x = MACHINE.ROLE._x = point.x;
		  MACHINE.ROLE.orig_y = MACHINE.ROLE._y = point.y;
		  

		  // textfield for IP
		  MACHINE.ROLE.createTextField("label",_global.getNewDepth(),0,0,1,1);
		  //MACHINE.ROLE.label.embedFonts = true;

		  MACHINE.ROLE.label.autoSize = true;
		  MACHINE.ROLE.label.setNewTextFormat(_global.RoleFormat);
		  MACHINE.ROLE.label.text = MACHINE.IP;
		  MACHINE.ROLE.label.selectable = false;


    	  // TEXT LAYOUT DIFFERENT FOR EACH VIEW
		  if (TARGET.THREAT == "internal") {
			 
			 var src = new Object();
			 var dst = new Object();
			 
			 src.x = TARGET.center.x;
			 src.y = TARGET.center.Y;
			 dst.x = MACHINE.ROLE._x;
			 dst.y = MACHINE.ROLE._y;
			 
			 // CALCULATE ANGLE
			 var ANGLE = _global.getAngle(src,dst);
			 
			 // top left
			 if (dst.x < src.x && dst.y < src.y) {
				//_global.log("moving label for top lefter:"+MACHINE.IP);
				MACHINE.ROLE.label._x -= MACHINE.ROLE.label._width+1;
			 }
			 // bottom left
			 else if (dst.x < src.x && dst.y > src.y) {
				//_global.log("moving label for bottom lefter:"+MACHINE.IP);
    	  		MACHINE.ROLE.label._x -= MACHINE.ROLE.label._width+1;
			 }
			 MACHINE.ROLE.label._y += 4;
			 
			 // adjust atk def icon along angle
			 var xmov = 10*Math.cos(ANGLE);
			 var ymov = 10*Math.sin(ANGLE);
			 MACHINE.ROLE._x += xmov;
			 MACHINE.ROLE._y += ymov;
			 
			 
		  }
		  // PERIMETER VIEW
		  else if (TARGET.THREAT == "perimeter") {
			 
			 // ATK TO LEFT
			 if (ROLE == "ATK") {
          	MACHINE.ROLE._x -= 10;
          	MACHINE.ROLE.label._x -= MACHINE.ROLE.label._width+8;
          	
			 }
			 // DEF TO RIGHT
			 else {
          	MACHINE.ROLE._x += 10;
				MACHINE.ROLE.label._x += 7; 
			 }
			 MACHINE.ROLE.label._y -= 7;
		  }
		  // MODEL
		  else if (TARGET.THREAT == "model") {
			 
			 MACHINE.ROLE.label._x -= 16; 
			 MACHINE.ROLE.label._y += 7;

			 // add in the key
			 var KEY = MACHINE.ROLE.attachMovie("gold_key", "AGENT_KEY", _global.getNewDepth());
			 KEY._x += 15;
			 KEY._y -= 8;
		  }

		  
		  //_global.log("WARN: "+this.DATA[ROLE][i]['warn']);
		  // GLOW FUNCTIONS
		  if (this.DATA[ROLE][i]['warn'] == 1) {
			 // Makes the thing pulsate with color
			 MACHINE.glow = function() {
				this.dynColorTween(
										 {duration:10, rb:255},
										 {duration:10, rb:0, callback: this + ".glow"}
										 );
			 }
			 MACHINE.glow();
		  }
		  

		  // TWEEN X-Y Scales on RollOver and RollOut
		  MACHINE.ROLE.onRollOver = SCREEN.MACHINERollOver;
		  MACHINE.ROLE.onRollOut = SCREEN.MACHINERollOut;

		  // SHOW THE MENU FOR THIS ATK or DEF
		  MACHINE.ROLE.onPress = SCREEN.ATKDEFPress;
		  MACHINE.ROLE.onRelease = SCREEN.ATKDEFRelease;

		  // ADD TO RUNNING LIST
		  TARGET[ROLE+"_list"].push(HOST_ID);
		  
		}
	 } //  if !there
  } // foreach
  
  // DEBUG
  if (_global.DEBUG && _global.TIMESTAMPS) {
	 var stopATKDEF = getTimer();
	 _global.log("Total drawATKDEF("+ROLE+") time:"+(stopATKDEF - startATKDEF));
  }


}



SCREEN.MACHINERollOver = function() {
  _global.setRollText(this.rollText);
  
  // set no purge so this doesn't undraw under your cursor
  this._parent.NOPURGE = true;
  
  // grow
  this.dynTween({duration:1, _xscale:_global.ATKDEF_ROLLOVER_SCALE, _yscale:_global.ATKDEF_ROLLOVER_SCALE});
}

SCREEN.MACHINERollOut = function() {
  _global.unsetRollText();
  
  // allow undraw
  this._parent.NOPURGE = false;
  
  // ungrow
  this.dynTween({duration:1, _xscale:100, _yscale:100});
}



// ALT Line Handler
SCREEN.ATKDEFLinePress = function() {
  _global.log("linekeyhandler ids:"+this.IDS+", ALT_ID:"+this.ALT_ID);
  
  // INCIDENT REPORT
  if (this.IDS) {
		_global.unsetRollText();

		var pointObj:Object = new Object();
		pointObj.x = this._parent._parent._xmouse;
		pointObj.y = this._parent._parent._ymouse;

		var replaceObj:Object = new Object();
		replaceObj.ID = this.ALT_ID;
		replaceObj.SCREEN = SCREEN;

		_global.XMLMenu({ROLE:"ALT", ID:this.ALT_ID, IP:null, TARGET:this._parent._parent, point:pointObj, replace:replaceObj});

  }
}

//onRelease
SCREEN.ATKDEFPress = function() {
  _global.unsetRollText();
	 
  this._xscale = this._yscale = 130;

  var pointObj:Object = new Object();
  pointObj.x = this._parent._parent[this.ROLE+"_"+this._parent.ID].ROLE._x;
  pointObj.y = this._parent._parent[this.ROLE+"_"+this._parent.ID].ROLE._y;
  //_global.log("ATKDEFPress pointObj.x:"+pointObj.x+", this:"+this);
  

  var replaceObj:Object = new Object();
  replaceObj.ID = this._parent.ID;
  replaceObj.IP = this._parent.IP;
  replaceObj.SCREEN = SCREEN;

  _global.XMLMenu({ROLE:this.ROLE, ID:this._parent.ID, IP:this._parent.IP, TARGET:this._parent._parent, point:pointObj, replace:replaceObj});

}


// -------- CONNECTION LINES ---------//
SCREEN.drawCONs = function(TARGET:MovieClip) {
  

  // DEBUG
  if (_global.DEBUG && _global.TIMESTAMPS) {
	 var startCON = getTimer();
  }
  

  // DRAW LINES FOR NEW CONs
  var length:Number = this.DATA.CON.length;
  var i:Number = -1;
  while (++i < length) {
	 var src_id:Number = this.DATA.CON[i]['src'];
	 var dst_id:Number = this.DATA.CON[i]['dst'];
	 
    // key is src_id+dst_id
    var LINEKEY:String = "con_"+src_id+"_"+dst_id;
	 var LEVEL:Number = this.DATA.CON[i]['level'];
	 
	 
	 //_global.log("*DRAW CON: "+TARGET[LINEKEY]+":, src:"+src_id+", dst:"+dst_id);
	 

	 // GET X,Y for SRC AND DST ON STAGE
	 var src:Object = new Object();
	 var dst:Object = new Object();
	 if (TARGET["ATK_"+src_id]) {
		src.x = TARGET["ATK_"+src_id].ROLE.orig_x;
		src.y = TARGET["ATK_"+src_id].ROLE.orig_y;
	 }
	 else if (TARGET["DEF_"+src_id]) {
		src.x = TARGET["DEF_"+src_id].ROLE.orig_x;
		src.y = TARGET["DEF_"+src_id].ROLE.orig_y;
		
	 }
	 
	 if (TARGET["ATK_"+dst_id]) {
		dst.x = TARGET["ATK_"+dst_id].ROLE.orig_x;
		dst.y = TARGET["ATK_"+dst_id].ROLE.orig_y;
	 }
	 else if (TARGET["DEF_"+dst_id]) {
		dst.x = TARGET["DEF_"+dst_id].ROLE.orig_x;
		dst.y = TARGET["DEF_"+dst_id].ROLE.orig_y;
	 }	


    // line not already there, src and dst are real
	 if (!TARGET[LINEKEY] && (src_id != dst_id) && (src.x && dst.x)) {

		// Push to the Persistent var
		TARGET.LINEKEYS.push(LINEKEY);

		var ALT_ID:Number = this.DATA.CON[i]['alert'];
		
		// draw
      var LINEKEY_mc:MovieClip = TARGET.createEmptyMovieClip(LINEKEY, _global.getNewDepth());
      //LINEKEY_mc.useHandCursor = false;
      
      var THIS_COLOR = COLOR_VULN[LEVEL].color;
      var COLOR_ANIM = COLOR_VULN[LEVEL].color_anim;
		
      // CALCULATE ANGLE
		var ANGLE:Number = _global.getAngle(src,dst);
		
		// DRAW THE CONNECTION LINE
		drawATKDEFLine(LINEKEY_mc, src, dst, ANGLE, THIS_COLOR, this.DATA.CON[i]['ids'], this.DATA.CON[i]['alert'], dst_id);

      LINEKEY_mc._alpha = _global.ALPHA;
		LINEKEY_mc.LINEKEY = LINEKEY;
		LINEKEY_mc.LEVEL = LEVEL;
		LINEKEY_mc.IDS = this.DATA.CON[i]['ids'];
		
		// SET UP SOME PROPERTIES FOR THE INCIDENT PROFILE
		LINEKEY_mc.SRC_ID = src_id;
		LINEKEY_mc.DST_ID = dst_id;
		LINEKEY_mc.ALT_ID = ALT_ID;

		// onPress & onRelease ATKDEFLine
		LINEKEY_mc.onPress = SCREEN.ATKDEFLinePress;
		LINEKEY_mc.onRelease = SCREEN.ATKDEFLineRelease;

		// set the rollText
		LINEKEY_mc.rollText = this.DATA.ALT[LINEKEY_mc.ALT_ID].text;

      // MAKE THE LINE STAND OUT IF YOU'RE OVER IT
      LINEKEY_mc.onRollOver = function() {
		  var ALT_mc = SCREEN.ALT.CONTENT[this.ALT_ID];
		  _global.setRollText(this.rollText);
		  this.HOVER._visible = this.HOVER.IDS._visible = ALT_mc.HOVER._visible = true;
		  this._alpha = ALT_mc._alpha = _global.ALPHA_FADED;
		}
		// rollOut
		LINEKEY_mc.onRollOut = function() {
		  var ALT_mc = SCREEN.ALT.CONTENT[this.ALT_ID];
		  _global.unsetRollText();
		  this.HOVER._visible = this.HOVER.IDS._visible = ALT_mc.HOVER._visible = false;
		  this._alpha = ALT_mc._alpha = _global.ALPHA;
		}

		// ANIMATION OF PACKET FLOW for ALERTS
		if (this.DATA.CON[i]['ids'] && _global.LINE_ANIM) {

		  var XMOV:Number = (Math.floor(_global.LINE_ANIM_SPEED*Math.cos(ANGLE)*100)/100);
		  var YMOV:Number = (Math.floor(_global.LINE_ANIM_SPEED*Math.sin(ANGLE)*100)/100);
		  
		  var ANIM:MovieClip = LINEKEY_mc.createEmptyMovieClip("anim", _global.getNewDepth());
		  var LINE_LENGTH:Number = (Math.floor(Math.sqrt((dst.x-src.x)*(dst.x-src.x) + (dst.y-src.y)*(dst.y-src.y)))*100)/100;
		  ANIM.drawCircle(1, src.x, src.y, COLOR_ANIM, 0, COLOR_ANIM, _global.ALPHA);
		  ANIM.LENGTH = 0;
		  var ANIM_INTERVAL = LINEKEY+"_anim";
		  set(ANIM_INTERVAL, setInterval(LineAnimate, _global.LINE_ANIM_INTERVAL, ANIM, LINE_LENGTH, XMOV, YMOV));
		  // var tween_args = "duration: 60, _x:"+dst.x+", _y:"+dst.y;
		  // _global.log("TWEEN ARGS:"+tween_args);
		  // LINEKEY_mc.anim.dynTween({duration: 20, _x:dst.x, _y:dst.y});
		  
		}

		// _global.log("drawing CON from ("+src.x+","+src.y+") to ("+dst.x+","+dst.y+") with LINEKEY: "+LINEKEY);
	 }


	 else {
		// _global.log("not valid CON"+ " ATK:"+ATK_list.join(",")+" DEF:"+DEF_list.join(","));
	 }
  }
  
  
  // DEBUG
  if (_global.DEBUG && _global.TIMESTAMPS) {
	 var stopCON = getTimer();
	 _global.log("Total drawCON time:"+(stopCON - startCON));
  }
}

// THIS KILLS OLD CONNECTION LINES, called when new data comes in NOT on interval
SCREEN.PurgeCONs = function(TARGET:MovieClip) {

  // go backwards since we're gonna splice
  var i:Number = TARGET.LINEKEYS.length;
  
  //_global.log("PurgeCON "+TARGET.THREAT+", i:"+i);
  
  while (--i >=0) {
    var LINEKEY:MovieClip = TARGET.LINEKEYS[i];
	 
    // IF no longer in the current list
    if (!this.CURRENT_LINEKEYS[LINEKEY]) {
    	//_global.log("killing off old LINEKEY: "+LINEKEY);
    	this.ClearATKDEFLine(LINEKEY, TARGET, i);
	 }
  }
}



// AFTER A FADE OUT, LOSE THE ROLE CLIP
SCREEN.clearThisATKDEF = function(MACHINE) {

  // REMOVE THE ATK OR DEF CLIP
  MACHINE.ROLE.removeMovieClip();
  delete MACHINE.ROLE;
  MACHINE.deleteMovieClips();
  MACHINE.removeMovieClip();
  delete MACHINE;

  _global.unsetRollText();
}

// PURGE ATTACKERS AND DEFENDERS AND REMOVE STALE MOVIE CLIPS
SCREEN.PurgeATKDEF = function(ROLE, TARGET:MovieClip) {

  //_global.log("PurgeATKDEF: "+ROLE+": "+TARGET.THREAT);

  // NOW COMPARE AGAINST THE PERSISTENT LIST
  var CURRENT_ROLE = "CURRENT_"+ROLE;
  var ROLELIST = ROLE+"_list";
  var ROLEPOOL = ROLE+"_pool";
  
  var ROLETOP = (TARGET[ROLELIST].length - 1);
  //  purge old ones leftover in the continuing list
  //  we need to go in reverse so we can splice
  for (var i=ROLETOP; i>=0; i--)  {
	 var id = TARGET[ROLELIST][i];
	 var MACHINE = TARGET[ROLE+"_"+id];

	 //MACHINE.ROLELIST = ROLELIST;

	 // IF NOT IN CURRENT LIST AND IT IS IN FACT DRAWN
	 if (!this[CURRENT_ROLE][id] && MACHINE && MACHINE.NOPURGE != 1) {

		// _global.log("**GONNA PURGE ID:"+id);

		// FREE UP ITS POOL POSITION
		TARGET[ROLEPOOL][MACHINE.index].used = 0;
		
		// CLEAR THIS ONE OUT THE LIST
		TARGET[ROLELIST].splice(i,1);		  
		
		// LOSE THE TEXT FIRST
		MACHINE.ROLE.label.removeTextField();
		
		// FADE EM OUT
		MACHINE.dynTween({duration:5, _alpha:40, callback:SCREEN+".clearThisATKDEF", cbArgs:MACHINE});
		
	 }
  }
}


// RSS Feed
SCREEN.drawRSSPane = function() {

  var PANE = _global.newDraggableWindow({target:this, instanceName:"RSS", width:_global.RSS_WIDTH, height:_global.RSS_HEIGHT, x:_global.RSS_X, y:_global.RSS_Y, title:_global.RSS_TITLE, hScrollPolicy:"off", vScrollPolicy:"off", isDraggable:false, isResizable:false, hasCloseBox:false, hasShader:false, swappable:false});

  
  // enable the menu bar
  PANE.enableMenuBar();

  // RSS Menu
  _global.rss_menu = PANE.menuBar.addMenu("Source");

  var length = _global.RSS_URL.length;
  _global.log("RSS MENU LENGTH: "+length);
  
  for (var i=0; i<length; i++) {
	 if (i == 0) {
		_global.rss_menu.addMenuItem({ label:_global.RSS_URL[i], type:"radio", selected:true, enabled:false, instanceName:i, groupName:"rss" } );
	 }
	 else {
		_global.rss_menu.addMenuItem({ label:_global.RSS_URL[i], type:"radio", selected:false, enabled:true, instanceName:i, groupName:"rss" } );
	 }
  }

  _global.rss_menu.addEventListener("change", SCREEN.RSSMenuListener);
  

  // TextArea for rendering the contents of the xml feed
  this.RSS.CONTENT.attachMovie("TextArea", "FEED", _global.getNewDepth());
  var TEXT = this.RSS.CONTENT.FEED;
  TEXT._alpha = 100;
  TEXT.selectable = false;
  TEXT.editable = false;
  TEXT.wordWrap = false;
  TEXT.vScrollPolicy = "on";
  TEXT.setSize(_global.RSS_WIDTH,_global.RSS_HEIGHT-15);
  TEXT.html = true;
  TEXT.styleSheet = _global.style_sheet;

  // seed it once
  this.refreshRSSPane(_global.RSS_URL_INDEX);

  // set an interval so it runs every so often
  _global.rssInterval = setInterval(this, "refreshRSSPane", _global.RSS_REFRESH_INTERVAL, _global.RSS_URL_INDEX);
  

}


// menuListener
SCREEN.RSSMenuListener = new Object();
SCREEN.RSSMenuListener.change = function(event) {
  
  var item = event.menuItem;
  var index = event.target.indexOf(item);
  var instanceName = item.attributes.instanceName;
  var groupName = item.attributes.groupName;

  // set the index
  _global.RSS_URL_INDEX = instanceName;

  if (_global.DEBUG) {
	 _global.log("RSSMenuListener item:"+item+", index:"+index+", instanceName:"+instanceName+", groupName:"+groupName+", target:"+event.target);
  }

  // set the text
  SCREEN.RSS.CONTENT.FEED.text = "<LI><SPAN CLASS='sans'>Loading new feed from "+_global.RSS_URL[_global.RSS_URL_INDEX]+"</SPAN>";


  // first clear the interval
  clearInterval(_global.rssInterval);

  // seed it
  SCREEN.refreshRSSPane(_global.RSS_URL_INDEX);

  // reset an interval so it runs every so often
  _global.rssInterval = setInterval(SCREEN, "refreshRSSPane", _global.RSS_REFRESH_INTERVAL, _global.RSS_URL_INDEX);

  // disable this one now
  event.target.setMenuItemEnabled(item, false);
  
  // ENABLE THE OTHERS
  var length = _global.rss_menu.length;
  for (var i=0; i<length; i++) {
	 if (_global.rss_menu[i].instanceName != i) {
		// add it on proper now
		event.target.setMenuItemEnabled(event.target.getMenuItemAt(i));
	 }
  }
}



// killRSSinterval
_global.killRSSInterval = function() {
  clearInterval(_global.rssInterval);
}

// RSS Feed
_global.rssFeed = new XML();
_global.rssFeed.ignoreWhite = true;
_global.rssFeed.onLoad = function(success) {
  
  if (success) {
	 if (_global.DEBUG) {
		_global.log("Success loading RSS feed.");
	 }
	  
	 // retrieving all items from the rss file 
	 var channel = this.firstChild.childNodes[0];
	 var itemsArr = channel.childNodes;
	 var length:Number = itemsArr.length;
	 var TEXT:String = "";

	 // iterate
	 for (var i=0; i < length; i++){
		//_global.log("node name:'"+itemsArr[i].nodeName+"', type: "+itemsArr[i].nodeType+", val:"+itemsArr[i].firstChild.nodeValue);

		if (itemsArr[i].nodeName == "item") {
		  var item = itemsArr[i].childNodes;
		  var length2:Number = item.length;
		  for (var z=0; z < length2; z++) {
			 //_global.log("ITEM:"+itemsArr[i].childNodes[z].nodeName);

			 if (itemsArr[i].childNodes[z].nodeName == "title") {

				TEXT += "<span class='sansbluehead'>"+itemsArr[i].childNodes[z].firstChild.nodeValue+"</span>";
			 }
			 else if (itemsArr[i].childNodes[z].nodeName == "description") {

				TEXT += "<span class='sans'>"+itemsArr[i].childNodes[z].firstChild.nodeValue+"</span>";
			 }
			 else if (itemsArr[i].childNodes[z].nodeName == "link") {
				TEXT += "<A HREF='"+itemsArr[i].childNodes[z].firstChild.nodeValue+"' TARGET='_blank'>"+itemsArr[i].childNodes[z].firstChild.nodeValue+"</A>";
			 }

			 TEXT += "<BR>";

		  }

		  // between items
		  TEXT += "<BR>";

		}
		else {
		  //_global.log("-NOT ITEM");
		}
	 }
	 //_global.log("TEXT:"+TEXT);
	 SCREEN.RSS.CONTENT.FEED.text = TEXT;
  }
  else {
	 _global.errorPage(3);
  }
}

// refresh the RSS pane
SCREEN.refreshRSSPane = function(index:Number) {
  if (!index) {
	 index = 0;
  }

  // must bust cache in flash so we always load it fresh
  var xmlCacheBust:Number = Math.random();
  var URL:String = _global.RSS_URL[index]+"&xmlCacheBust="+xmlCacheBust;

  if (_global.DEBUG) {
	 _global.log("refreshRSSPane called for:"+URL);
  }
  // load
  _global.rssFeed.load(URL);
}



/*
  MODELS in the stream, that is what they are
*/

// Net parser
SCREEN.parse_NET = function(nodeArray) {
  var container:Array = new Array();
  container['id'] = nodeArray[0];
  container['label'] = nodeArray[1];

  
  // push onto DATA
  this.DATA.NET.push(container);

}

// Attackers parser
SCREEN.parse_ATK = function(nodeArray) {
  var container = new Array();

  container['id'] = nodeArray[0];
  container['ip'] = nodeArray[1];
  // country code and name
  container['cc'] = nodeArray[2];
  container['country'] = nodeArray[3];
  container['warn'] = nodeArray[4];
  
  _global.CURRENT_ATK[container['id']] = 1;
  
  // push onto DATA
  this.DATA.ATK.push(container);
}

// Defenders parser
SCREEN.parse_DEF = function(nodeArray) {
  var container = new Array();

  container['id'] = nodeArray[0];
  container['ip'] = nodeArray[1];
  container['os'] = nodeArray[2];
  container['warn'] = nodeArray[3];

  _global.CURRENT_DEF[container['id']] = 1;
  
  // push onto DATA
  this.DATA.DEF.push(container);
}

// Connections parser
SCREEN.parse_CON = function(nodeArray) {
  var container = new Array();
  container['src'] = nodeArray[0];
  container['dst'] = nodeArray[1];
  container['ids'] = nodeArray[2];
  container['alert'] = nodeArray[3]
  container['state'] = nodeArray[4];
  container['level'] = nodeArray[5];
  container['duplex'] = nodeArray[6];
		
  // add to current linekeys
  _global.CURRENT_LINEKEYS["con_"+container['src']+"_"+container['dst']] = 1;
  
  // push onto DATA
  this.DATA.CON.push(container);
}

// Alerts parser
SCREEN.parse_ALT = function(nodeArray) {
  // nodeArray[1] is ALT_ID
  var ALT_ID = nodeArray[1];

  // for the counter
  _global.CURRENT_ALT.push(ALT_ID);

  var container = new Array();
  container['tick'] = nodeArray[0];
  container['source'] = nodeArray[2];
  container['count'] = nodeArray[3];
  container['ids'] = nodeArray[4];
  container['src'] = nodeArray[5];
  container['src_ip'] = nodeArray[6];
  container['srcPort'] = nodeArray[7];
  container['src_role'] = nodeArray[8];
  container['dst'] = nodeArray[9];
  container['dst_ip'] = nodeArray[10];
  container['dstPort'] = nodeArray[11];
  container['dst_role'] = nodeArray[12];
  container['text'] = nodeArray[13];
  container['cost'] = nodeArray[14];
  container['threat'] = nodeArray[15];
  container['vuln'] = nodeArray[16];
  container['risk'] = nodeArray[17];
  container['ci'] = nodeArray[18];
  container['risk_txt'] = nodeArray[19];

  this.DATA.ALT[ALT_ID] = container;
}


SCREEN.parse_VULN_TYPE = function(nodeArray) {
  var id = nodeArray[0];
  COLOR_VULN[id] = new Object();
  COLOR_VULN[id].color = nodeArray[1];
  COLOR_VULN[id].label = nodeArray[2];
  COLOR_VULN[id].description = nodeArray[3];
  COLOR_VULN[id].color_anim = nodeArray[4];
}

SCREEN.parse_THREAT_TYPE = function(nodeArray) {
  var id = nodeArray[0];
  COLOR_THREAT[id] = new Object();
  COLOR_THREAT[id].color = nodeArray[1];
  COLOR_THREAT[id].label = nodeArray[2];

}


// VIZ STREAM 
SCREEN.preParseStream_VIZ = function() {

 
  // INITIALIZE MODEL ARRAYS
  _global.CURRENT_LINEKEYS = new Array();
  _global.CURRENT_ATK = new Array();
  _global.CURRENT_DEF = new Array();
  _global.CURRENT_ALT = new Array();
  
  this.DATA.NET = new Array();
  this.DATA.HOST = new Array();
  this.DATA.ATK = new Array();
  this.DATA.CON = new Array();
  this.DATA.DEF = new Array();
  this.DATA.ALT = new Object();
  this.DATA.METRIC = new Object();


}



SCREEN.clearPreLoader = function() {
  PRELOADER.removeMovieClip();
  delete PRELOADER;
}

SCREEN.drawATKDEFPanes = function() {

  if (_global.VIZ_THREAT == "combined") {

		// PERIMETER
		var PANE = _global.newDraggableWindow({target:SCREEN, instanceName:"ATKDEF_PERIMETER", width:_global.ATKDEF_WIDTH/2, height:_global.ATKDEF_HEIGHT, x:_global.ATKDEF_X, y:_global.ATKDEF_Y, title:_global.ATKDEF_TITLE+" - Perimeter", hScrollPolicy:ScreenShapes["perimeter"].HSCROLL, vScrollPolicy:ScreenShapes["perimeter"].VSCROLL, isDraggable:false, isResizable:false, hasCloseBox:false, hasShader:false, swappable:false});

		PANE.CONTENT.THREAT = "perimeter";
		PANE.CONTENT.ATK_pool = new XY_Positions("ATK", "perimeter", PANE.CONTENT);
		PANE.CONTENT.DEF_pool = new XY_Positions("DEF", "perimeter", PANE.CONTENT);
		PANE.CONTENT.LINEKEYS = new Array();
		PANE.CONTENT.ATK_list = new Array();
		PANE.CONTENT.DEF_list = new Array();

		// enable the menu bar
		PANE.enableMenuBar();
		
		// ------------- Threats Menu ------------- //
		var threat_menu = PANE.menuBar.addMenu(_global.ATKDEF_THREAT_MENU_TITLE);
		var length = _global.threats_menu.length;
		for (var i=0; i<length; i++) {
		  if (_global.VIZ_THREAT == _global.threats_menu[i].data) {
			 threat_menu.addMenuItem({ label:_global.threats_menu[i].label, type:"radio", selected:true, enabled:false, instanceName:_global.threats_menu[i].data, groupName:"threat" } );
		  }
		  else {
			 threat_menu.addMenuItem({ label:_global.threats_menu[i].label, type:"radio", selected:false, enabled:true, instanceName:_global.threats_menu[i].data, groupName:"threat" } );
		  }
		}
		threat_menu.addEventListener("change", SCREEN.ATKDEFMenuListener);


		// INTERNAL
		var PANE = _global.newDraggableWindow({target:SCREEN, instanceName:"ATKDEF_INTERNAL", width:_global.ATKDEF_WIDTH/2, height:_global.ATKDEF_HEIGHT, x:_global.ATKDEF_X+this.ATKDEF_PERIMETER.width, y:_global.ATKDEF_Y, title:_global.ATKDEF_TITLE+" - Internal", hScrollPolicy:false, vScrollPolicy:false, isDraggable:false, isResizable:false, hasCloseBox:false, hasShader:false, swappable:false});
		PANE.CONTENT.THREAT = "internal";
		PANE.CONTENT.DEF_pool = new XY_Positions("DEF", "internal", PANE.CONTENT);

		PANE.CONTENT.LINEKEYS = new Array();
		PANE.CONTENT.ATK_list = new Array();
		PANE.CONTENT.DEF_list = new Array();
  }

  else {

	 // PERIMETER OR INTERNAL based on _global.VIZ_THREAT
	 var PANE = _global.newDraggableWindow({target:SCREEN, instanceName:"ATKDEF_"+_global.VIZ_THREAT.toUpperCase(), width:_global.ATKDEF_WIDTH, height:_global.ATKDEF_HEIGHT, x:_global.ATKDEF_X, y:_global.ATKDEF_Y, title:_global.ATKDEF_TITLE+" - "+_global.VIZ_THREAT.ucWords(), hScrollPolicy:ScreenShapes[_global.VIZ_THREAT].HSCROLL, vScrollPolicy:ScreenShapes[_global.VIZ_THREAT].VSCROLL, isDraggable:false, isResizable:false, hasCloseBox:false, hasShader:false, swappable:false});


	 PANE.CONTENT.THREAT = _global.VIZ_THREAT;
	 PANE.CONTENT.ATK_pool = new XY_Positions("ATK", _global.VIZ_THREAT, PANE.CONTENT);
	 PANE.CONTENT.DEF_pool = new XY_Positions("DEF", _global.VIZ_THREAT, PANE.CONTENT);
	 PANE.CONTENT.LINEKEYS = new Array();
	 PANE.CONTENT.ATK_list = new Array();
	 PANE.CONTENT.DEF_list = new Array();
	 
	 // enable the menu bar
	 PANE.enableMenuBar();
	 
	 // ------------- Threats Menu ------------- //
	 var threat_menu = PANE.menuBar.addMenu(_global.ATKDEF_THREAT_MENU_TITLE);
	 var length = _global.threats_menu.length;
	 for (var i=0; i<length; i++) {
		if (_global.VIZ_THREAT == _global.threats_menu[i].data) {
		  threat_menu.addMenuItem({ label:_global.threats_menu[i].label, type:"radio", selected:true, enabled:false, instanceName:_global.threats_menu[i].data, groupName:"threat" } );
		}
		else {
		  threat_menu.addMenuItem({ label:_global.threats_menu[i].label, type:"radio", selected:false, enabled:true, instanceName:_global.threats_menu[i].data, groupName:"threat" } );
		}
	 }
	 threat_menu.addEventListener("change", SCREEN.ATKDEFMenuListener);



	 // ------------- Traffic menu  ------------- //
	 /*
		var traffic_menu = PANE.menuBar.addMenu("Traffic");
  
		// go through connections
		var length = _global.traffics_menu.length;
		var i = -1;
		while (++i < length) {
		traffic_menu.addMenuItem({label:_global.traffics_menu[i].label, selected:true, enabled:true, instanceName:_global.traffics_menu[i].data, groupName:"traffic", icon:"CON_"+i});
		}
		traffic_menu.addEventListener("change", SCREEN.ATKDEFMenuListener);
	 */
  
	 // -------------  END Traffic menu  ------------- //

  }

}

SCREEN.postParseStream_VIZ = function() {

  // kill PreLoader
  if (PRELOADER) {
	 // FADE EM OUT
	 PRELOADER.dynTween({duration:5, _alpha:0, callback:SCREEN+".clearPreLoader"});
	 
  }

  // attackers & defenders window
  if (_global.ATKDEF_X) {

	 // draw up the window if not there
	 if (!this.initATKDEF) {
		this.initATKDEF = true;
		this.drawATKDEFPanes();
		this.drawATKDEFBorders(this.ATKDEF_PERIMETER.CONTENT);
		this.drawATKDEFBorders(this.ATKDEF_INTERNAL.CONTENT);
	 }

  
	 // SET THE OBJ VARS FOR THE PURGES TO WORK
	 this.CURRENT_ATK = _global.CURRENT_ATK;
	 this.CURRENT_DEF = _global.CURRENT_DEF;
	 this.CURRENT_LINEKEYS = _global.CURRENT_LINEKEYS;
	 
	 // TIDY our global placeholders
	 delete _global.CURRENT_ATK;
	 delete _global.CURRENT_DEF;
	 delete _global.CURRENT_LINEKEYS;

	 // DRAW THE VIZ STUFF
	 // PERIMETER
	 if (this.ATKDEF_PERIMETER) {
		this.drawATKDEF("DEF", this.ATKDEF_PERIMETER.CONTENT);
		this.drawATKDEF("ATK", this.ATKDEF_PERIMETER.CONTENT);
		this.drawCONs(this.ATKDEF_PERIMETER.CONTENT);
		
		// purge perimeter
		this.PurgeATKDEF("DEF", this.ATKDEF_PERIMETER.CONTENT);
		this.PurgeATKDEF("ATK", this.ATKDEF_PERIMETER.CONTENT);
		this.PurgeCONs(this.ATKDEF_PERIMETER.CONTENT);
	 }

	 // INTERNAL
	 if (this.ATKDEF_INTERNAL) {
		// draw internal
		this.drawATKDEF("DEF", this.ATKDEF_INTERNAL.CONTENT);
		this.drawCONs(this.ATKDEF_INTERNAL.CONTENT);
		
		// purge internal
		this.PurgeATKDEF("DEF", this.ATKDEF_INTERNAL.CONTENT);
		this.PurgeCONs(this.ATKDEF_INTERNAL.CONTENT);
	 }
	 
  }
  
  // RSS FEED
  if (_global.RSS_X && !this.RSS) {
	 this.drawRSSPane();
  }

  // METRICS WINDOW
  if (_global.METRIC_X && !this.METRIC) {
	 this.drawMETRICPane();
  }

  // GRAPH WINDOW
  if (_global.GRAPH_X && !this.GRAPH) {
	 this.drawGRAPHPane();
  }


  // ALT WINDOW
  if (_global.ALT_X) {
	 if (!this.ALT) {
		this.drawALTPane(_global.ALT_X, _global.ALT_Y, _global.ALT_WIDTH, _global.ALT_HEIGHT);
	 }
	 
    this.drawALTs();
  }


  // CHECK FOR OVERTHROTTLED
  if (_global.CURRENT_ALT.length == _global.VIZ_THROTTLE) {
	 WHOLE_SCREEN.glowingRed = true;
	 WHOLE_SCREEN.glowRed();
  }
  else {
	 if (WHOLE_SCREEN.glowingRed == true) {
		WHOLE_SCREEN.unGlowRed();
	 }
  }


}






// ATKDEF menu listener
SCREEN.ATKDEFMenuListener = new Object();
SCREEN.ATKDEFMenuListener.change = function(event) {
  var item = event.menuItem;
  var index = event.target.indexOf(item);
  var instanceName = item.attributes.instanceName;
  var groupName = item.attributes.groupName;

  if (_global.DEBUG) {
	 _global.log("ATKDEFMenuListener item:"+item+", instanceName:"+instanceName+", groupName:"+groupName+", target:"+event.target);
  }

  // threat
  if (groupName == "threat") {
	 event.target.setMenuItemEnabled(item, false);
	 
	 // ENABLE THE OTHERS
	 var length = _global.threats_menu.length;
	 for (var i=0; i<length; i++) {
		if (_global.threats_menu[i].data != instanceName) {
		  // add it on proper now
		  event.target.setMenuItemEnabled(event.target.getMenuItemAt(i));
		}
	 }

	 // set
	 var subscribeThreat:String = undefined;
	 if (instanceName == "combined") {
		subscribeThreat = "perimeter";
	 }
	 else {
		subscribeThreat = instanceName;
	 }


	 // unsubscribe and then resubscribe to viz now
	 _global.contentLC.send(_global.socketnavListener, "unSubscribe", {stream:"viz"});


	 // swap ATKDEF screen layout
	 SCREEN.SwapATKDEF(instanceName);
	 
	 var subscribeViz = _global.contentLC.send(_global.socketnavListener, "subscribe", {stream:"viz", delay:0, refresh:_global.VIZ_REFRESH, activity:_global.VIZ_ACTIVITY, threat:subscribeThreat, network_id:_global.VIZ_NETWORK_ID, throttle:_global.VIZ_THROTTLE});

	 _global.log("result of subscribeViz: "+subscribeViz+", with threat:"+subscribeThreat);

  }
  

}



// ------------------------------------------------------------// 
//   MAIN DATA PARSING 
// ------------------------------------------------------------// 
/*
  This parses the custom data format of the XMLSocket stream
  Each nodeName is the 1st element of each array
  and the values are stored in a sequential array to be mapped below.
*/
SCREEN.parseStreamData = function(DataString) {
 
  if (_global.DEBUG_VERBOSE) {
	 _global.log("SCREEN.parseStreamData: "+DataString);
  }
  
  // Split up the datastring
  var streamXML:Array = DataString.split("|");
  // USE STREAMINFO at 0 key, and ignore 0 key when parsing
  var STREAMSTUFF = streamXML[0];
  var STREAMINFO = STREAMSTUFF.split("&");
  var STREAMNAME = STREAMINFO[0];
  // var revision = STREAMINFO[2];
  // var tick_cursor = STREAMINFO[3];
  // var tick_start = STREAMINFO[4];

  
  // set tickstamp
  var TICKSTAMP = new Date(STREAMINFO[1]);
  
  // show name of packet in debug
  if (_global.DEBUG) {
	 _global.log(STREAMNAME+" PACKET at "+TICKSTAMP.getTimeStamp());
	 if (_global.TIMESTAMPS) {
		_global.log("New DataString length:"+DATA_LENGTH);
		var startParseTime = getTimer();
	 }
  }


  // RUN THE STREAM'S PRE PARSER, which creates more objects on DATA
  var PRE_PARSER = "SCREEN.preParseStream_"+STREAMNAME;
  eval(PRE_PARSER)();


  // LOOP AWAY
  //  we end up ignoring streamXML[0] in this loop, but that's just the
  //  streamname and info which we parsed already above.
  var i = streamXML.length++;
  while (--i) {
	 // _global.log("nodeString: "+streamXML[i]);
	 var nodeArray = streamXML[i].split("&");
	 var nodeName = nodeArray.shift();

	 // RUN THE APPROPRIATE NODE'S PARSER
	 var PARSER = "SCREEN.parse_"+nodeName;
	 //_global.log("running parser "+PARSER);
	 eval(PARSER)(nodeArray);
	 
  }
  
  // timestamp checking
  if (_global.DEBUG && _global.TIMESTAMPS) {
	 var stopParseTime = getTimer();
	 _global.log("Total Data Parse Time: "+(stopParseTime - startParseTime));
  }
  
  
  
  // RUN THE STREAM'S POST PARSER
  var POST_PARSER = "SCREEN.postParseStream_"+STREAMNAME;
  eval(POST_PARSER)();  

  // _global.log("--------------- END OF XML ------------------");
  
}




// THROW UP THE PRELOADER
var PRELOADER:MovieClip = attachMovie("PreLoader", "PRELOADER", _global.getNewDepth());
PRELOADER.title.text = _global.SCREEN_TITLE;
PRELOADER.status.text = "Subscribing to streams ...";

// place it
var point = WHOLE_SCREEN.getCenter();
PRELOADER._x = point.x-(PRELOADER._width/2);
PRELOADER._y = point.y-(PRELOADER._height/2);

// get initString from socket
_global.contentLC.send(_global.socketnavListener, "sendInitString");

// Look for packets from socket_lib.as and parse when they come in
_global.contentLC.parseStreamData = function(DataString) {
  SCREEN.parseStreamData(DataString);
}

// --------------------------// 
// SUBSCRIBE to viz (live_config.as)
// --------------------------// 
_global.subscribeLive();




