//------------------------------------------------------------------//
// @LICENSE@
//
// GLOBAL TEXT STYLES
// author: Lindsey Simon <lsimon@symbiot.com>
//------------------------------------------------------------------//

_global.style_sheet = new TextField.StyleSheet();
var cacheBust:Number = Math.random();
_global.style_sheet.load("styles.css?cacheBust="+cacheBust);

// generic text format
_global.TxtFormat = new TextFormat();
_global.TxtFormat.font = "_sans";
_global.TxtFormat.size = 12;
_global.TxtFormat.color = "0x333333";

_global.SmallTxtFormat = new TextFormat();
_global.SmallTxtFormat.font = "_sans";
_global.SmallTxtFormat.size = 10;
_global.SmallTxtFormat.color = "0x333333";

// Draggable Pane/Window font style
_global.DPTxtFormat = new TextFormat();
_global.DPTxtFormat.font = "_sans";
_global.DPTxtFormat.size = 13;
_global.DPTxtFormat.color = "0x333333";



//BoldOrangeTxt
_global.BoldOrangeTxtFormat = new TextFormat();
_global.BoldOrangeTxtFormat.font = "_sans";
_global.BoldOrangeTxtFormat.size = 13;
_global.BoldOrangeTxtFormat.color = "0xFF7B00";
//_global.BoldOrangeTxtFormat.bold = true;
_global.BoldOrangeTxtFormat.underline = false;

//BoldOrangeTxt
_global.BoldOrangeULTxtFormat = new TextFormat();
_global.BoldOrangeULTxtFormat.font = "_sans";
_global.BoldOrangeULTxtFormat.size = 13;
_global.BoldOrangeULTxtFormat.color = "0xFF7B00";
//_global.BoldOrangeULTxtFormat.bold = true;
_global.BoldOrangeULTxtFormat.underline = true;



// BoldBlueTxt
_global.BoldBlueTxtFormat = new TextFormat();
_global.BoldBlueTxtFormat.font = "_sans";
_global.BoldBlueTxtFormat.size = 13;
_global.BoldBlueTxtFormat.color = "0x000099";
_global.BoldBlueTxtFormat.underline = false;
//_global.BoldBlueTxtFormat.bold = true;




// GRID Format (i.e. IDS, ALT) style
_global.GridFormat = new TextFormat();
_global.GridFormat.font = "_sans";
_global.GridFormat.size = 10;

// GRID Header style for ALT_FIELDS
_global.GridHeader = new TextFormat();
_global.GridHeader.font = "_sans";
_global.GridHeader.size = 10;
_global.GridHeader.underline = true;

// ATKDEF label format style
_global.RoleFormat = new TextFormat();
_global.RoleFormat.font = "_sans";
_global.RoleFormat.align = "left";
_global.RoleFormat.size = 9;


// ATKDEF label format style
_global.graphY = new TextFormat();
_global.graphY.font = "_sans";
_global.graphY.align = "right";
_global.graphY.size = 10;

// for XML Section Headers
_global.XMLHeadFormat = new TextFormat();
_global.XMLHeadFormat.font = "_sans";
_global.XMLHeadFormat.color = "0xff7000";
_global.XMLHeadFormat.size = 18;
_global.XMLHeadFormat.bold = true;
