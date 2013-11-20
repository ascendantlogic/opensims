//------------------------------------------------------------------//
// @LICENSE@
//
// REPORTS
// authors: Lindsey Simon <lsimon@symbiot.com>, 
//          Paco Nathan <paco@symbiot.com>
//------------------------------------------------------------------//



// START A SCREEN
var PANE = _global.newScreen("Reports");
var SCREEN = PANE.CONTENT;

// ACCORDION
var REPORTS = SCREEN.attachMovie("Accordion", "REPORTS", _global.getNewDepth());

// POSITION AND SIZE
REPORTS._x = 5;
REPORTS._y = 10;
REPORTS.setSize(_global.SCREEN_WIDTH-10, _global.SCREEN_HEIGHT-50);


// Latest Reports
var LATEST = REPORTS.createChild("View", "LATEST", { label: "Latest Reports" });

var point = new Object();
point.x = 15;
point.y = 20;

// create the links here
var docXML:XML = new XML();
docXML.ignoreWhite = true;
docXML.onLoad = function(success) {
  if (success) {
	 _global.layoutXML(this, LATEST, point); 

  }
  // error page
  else {
	 _global.errorPage("XML Load Error", "Error loading and reading reports.xml.");
  }
}

// load it in each time
var xmlCacheBust:Number = Math.random();
docXML.load("reports.xml?xmlCacheBust="+xmlCacheBust);


// Custom Reports
REPORTS.createChild("View", "CUSTOM", { label: "Create Custom Report" });
