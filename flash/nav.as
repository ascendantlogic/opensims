//------------------------------------------------------------------//
// NAV 
// @LICENSE@
//
// authors: Lindsey Simon <lsimon@symbiot.com>, 
//          Paco Nathan <paco@symbiot.com>
//------------------------------------------------------------------//

// SET SOME STAGE VARS
Stage.scaleMode = "noScale";
Stage.align = "LT"; 
Stage.showMenu = false;


// DEFINE LISTENERS BASED ON INSTANCE
_global.socketnavListener = "snLC_"+_root.instance;
_global.navListener = "nLC_"+_root.instance;
_global.contentListener = "cLC_"+_root.instance;


// Load DynTween Libs, thanks to Tatsuo Kato for the tweening prototypes.
// http://www.tatsuokato.com
#include "DynTween.as"
_root._alpha = 0;
_root.dynTween({duration:10, _alpha:100});

// get styles
#include "global_styles.as"


// init linkClips container
_global.linkClips = new Array();


// set up logo
_root.attachMovie("LOGO_SMALL", "LOGO_SMALL", 1);
LOGO_SMALL._x = 5;
LOGO_SMALL._y = 0;


// stylize a bit
_global.styles.AccordionHeader.setStyle("fontSize", "14");

// make Accordion
_root.attachMovie("Accordion", "navAccordion", 2);
_root.navAccordion._x = 0;
_root.navAccordion._y = 50;
_root.navAccordion.setSize(150, 280);
_root.navAccordion.headerHeight = 25;
_root.navAccordion.setStyle("themeColor", "0x999999");

// _root.createTextField("DEBUG", 3, 2, 60, 100, 200);
// _root.DEBUG.text = "testing 123";
// _root.DEBUG.multiline = true;
// _root.DEBUG.wordWrap = true;

// parse xml to create the links here
var docXML:XML = new XML();
docXML.ignoreWhite = true;
docXML.onLoad = function(success) {
  if (success) {
	 var NAV = docXML.firstChild;
	 var MENU = NAV.firstChild;
	 
	 var kerningHeight:Number = 20;

	 while (MENU != null) {

		// create the pane in the accordion for this menu
		var menuPane = _root.navAccordion.createChild("View", MENU.attributes.name, { label: MENU.attributes.name });
		
		var nextDepth:Number = 1;
		var nextY:Number = 5;
		
		var ITEM = MENU.firstChild;
		while (ITEM != null) {
		  var linkName:String = "itemLink_"+nextDepth;
		  var linkClip:MovieClip = menuPane.createEmptyMovieClip(linkName, nextDepth);

		  // push onto container
		  _global.linkClips.push(linkClip);
		  /*
		  _global.linkClips[nextDepth] = new Array();
		  _global.linkClips[nextDepth]['menu'] = MENU.attributes.name;
		  _global.linkClips[nextDepth]['linkName'] = linkName;
		  */

		  // pass ITEM 
		  linkClip.ITEM = ITEM;

		  // position
		  linkClip._x = 5;
		  linkClip._y = nextY;

		  // increment
		  nextDepth++;
		  nextY += kerningHeight;

		  // make a text field
		  linkClip.createTextField("link", 1, 5, 0, 0, 1);


		  // use var to reference
		  var itemLink = linkClip.link;
		  itemLink.autoSize = true;
		  itemLink.selectable = false;

		  //itemLink.embedFonts = true;

		  // set text and format
		  itemLink.text = ITEM.attributes.name;
		  itemLink.setTextFormat(_global.BoldBlueTxtFormat);
		  itemLink.setNewTextFormat(_global.BoldBlueTxtFormat);



		  linkClip.onRollOver = function() {
			 if (!this.inContent) {
				this.link.setTextFormat(_global.BoldOrangeTxtFormat);
			 }
		  }

		  linkClip.onRollOut = function() {
			 if (!this.inContent) {
				this.link.setTextFormat(_global.BoldBlueTxtFormat);
			 }
		  }

		  // on Release handler
		  linkClip.onRelease = function() {

			 // unsubscribe to anything currently subscribed to
			 _global.navLC.send(_global.socketnavListener, "unSubscribe");

			 // reset the current selected one
			 var i:Number = _global.linkClips.length + 1;
			 //_global.navLC.send(_global.contentListener, "log", "length:"+i);

			 while (--i > -1) {
				var thisLinkClip = _global.linkClips[i];

				if (thisLinkClip.inContent) {
				  thisLinkClip.link.setTextFormat(_global.BoldBlueTxtFormat);
				  thisLinkClip.inContent = false;
				}
			 }
			 
			 // set this one to content window
			 this.link.setTextFormat(_global.BoldOrangeULTxtFormat);
			 this.inContent = true;

			 // get URL in content window, but send request via socket.swf
			 // this prevents a race condition between unsubscribes
			 if (this.ITEM.attributes.href) {
				var URL:String = this.ITEM.attributes.href+"&instance="+_root.instance;
				_global.navLC.send(_global.socketnavListener, "loadContent", URL);
				
			 }
		  }
		  
		  ITEM = ITEM.nextSibling;
		}
		MENU = MENU.nextSibling;
	 }

	 _root.DEBUG.text = "success:"+success;
  }
  // error page
  else {
	 var msg:String = "Error loading XML, status:"+docXML.status+", loaded:"+docXML.loaded;
	 _root.DEBUG.text = msg;
	 _global.log(msg);
  }
}

// load in the config
var xmlCacheBust:Number = Math.random();
//&xmlCacheBust="+xmlCacheBust
docXML.load("param?name=nav_config");
//docXML.load("nav.xml?xmlCacheBust="+xmlCacheBust);



// Local Connection
_global.navLC = new LocalConnection();
_global.navLC.connect(_global.navListener);
