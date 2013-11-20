//------------------------------------------------------------------//
// @LICENSE@
//
// LIVE CONFIG
// authors: Lindsey Simon <lsimon@symbiot.com>,
//          Paco Nathan <paco@symbiot.com> 
//------------------------------------------------------------------//


// once we're connected, this gets called from socket_lib.as
_global.subscribeLive = function() {
  // VIZ
  var subscribeViz = _global.contentLC.send(_global.socketnavListener, "subscribe", {stream:"viz", delay:_global.VIZ_DELAY, refresh:_global.VIZ_REFRESH, activity:_global.VIZ_ACTIVITY, threat:_global.VIZ_THREAT, network_id:_global.VIZ_NETWORK_ID, throttle:_global.VIZ_THROTTLE});
  
  _global.log("result of subscribeViz: "+subscribeViz);

}



// For DEF services, IDS bars and ALT bars
SCREEN.SVC_BAR_HEIGHT = SCREEN.IDS_BAR_HEIGHT = SCREEN.ALT_BAR_HEIGHT = 15;

SCREEN.ATKDEF_X = 1;
SCREEN.ATKDEF_Y = 1;


// --------------------------------------------------// 

// SVC AVAIL COLORS FOR SVC GRID
// use avail attr
COLOR_SVC = new Array();
// service_timeout
COLOR_SVC[0] = new Object();
COLOR_SVC[0].color = "0xFFFF00";
COLOR_SVC[0].label = "Timeout";

// service_up
COLOR_SVC[1] = new Object();
COLOR_SVC[1].color = "0x00AA00";
COLOR_SVC[1].label = "Up";

// service_untestable
COLOR_SVC[2] = new Object();
COLOR_SVC[2].color = "0xFFFF00";
COLOR_SVC[2].label = "Untestable";

// service_lagging
COLOR_SVC[3] = new Object();
COLOR_SVC[3].color = "0xFE6400";
COLOR_SVC[3].label = "Lagging";

// service_down
COLOR_SVC[4] = new Object();
COLOR_SVC[4].color = "0xEE0000";
COLOR_SVC[4].label = "Down";

// service_unknown
COLOR_SVC[5] = new Object();
COLOR_SVC[5].color = "0xFF00FF";
COLOR_SVC[5].label = "Unknown";

// --------------------------------------------------// 
/* END CONNECTION COLORS */
