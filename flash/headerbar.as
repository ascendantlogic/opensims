

// SET SOME STAGE VARS
Stage.scaleMode = "noScale";
Stage.align = "LT"; 

var headerBar =_root.attachMovie("FDraggableWindowSymbol", "headerBar", 1);

// Draggable Pane/Window font style
_global.DPTxtFormat = new TextFormat();
_global.DPTxtFormat.font = "_sans";
_global.DPTxtFormat.size = 13;
_global.DPTxtFormat.color = "0x333333";


headerBar.setSwappable(false);
headerBar.setSize(625 ,22);
headerBar.shadePane();
headerBar.setHasShader(false);
headerBar.setResizable(false);
headerBar.setHasCloseBox(false);
headerBar.setIsMovable(false);
headerBar.setTitlebarHeight(22);
headerBar.setPaneTitle(_root.title);
//headerBar.setStyleProperty("textColor", "0x333333");
headerBar.setStyle("labelStyle", "_global.DPTxtFormat");
