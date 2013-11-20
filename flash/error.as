//------------------------------------------------------------------//
// @LICENSE@
//
// OPENSIMS ERROR 
// authors: Lindsey Simon <lsimon@symbiot.com>, 
//          Paco Nathan <paco@symbiot.com>
//
//------------------------------------------------------------------//

// Load some styles for text
#include "global_styles.as"

// Load up the movieClip prototype library
#include "prototype_lib.as"

// Load the global functions library
#include "global_lib.as"


//---  START A SCREEN and add everything else to it
var WHOLE_SCREEN = _global.newDraggableWindow({target:_root, instanceName:"WHOLE_SCREEN", width:300, height:300, alpha:100,  x:"0", y:"0", title:"Error Page :: Code "+_root.code, isScrolling:false, hasShader:false, isResizable:false, hasCloseBox:false, setBackground:true, swappable:false});


// SET SCREEN
_global.SCREEN = WHOLE_SCREEN.CONTENT;

SCREEN.createTextField("HEADER", 2, 10, 20, 1, 1);
SCREEN.createTextField("MESSAGE", 3, 10, 80, 300, 300);

SCREEN.HEADER.autoSize = true;
SCREEN.MESSAGE.multiline = true;
SCREEN.MESSAGE.wordWrap = true;
SCREEN.HEADER.setNewTextFormat(_global.XMLHeadFormat);
SCREEN.MESSAGE.setNewTextFormat(_global.DPTxtFormat);

SCREEN.HEADER.text = "Retrieving Error Header ...";
SCREEN.MESSAGE.text = "Retrieving Error Message ...";

// get status
_global.docXML = new XML();
_global.docXML.ignoreWhite = true;
_global.docXML.onLoad = function(success) {
  if (success) {
	 var ERR = this.firstChild;
	 var HEAD = ERR.childNodes[0].firstChild.nodeValue;
	 SCREEN.HEADER.text = HEAD;

	 var MSG = ERR.childNodes[1].firstChild.nodeValue;
	 SCREEN.MESSAGE.text = MSG;
  }
  else {
	 SCREEN.HEADER.text = "Unable to retrieve error header server.";
	 SCREEN.MESSAGE.text = "Unable to retrieve error message from server.";
  }
}

_global.docXML.load("param?name=error&code="+_root.code);
