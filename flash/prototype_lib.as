//------------------------------------------------------------------//
// @LICENSE@
//
// Additional Prototype functions
// author: Lindsey Simon <lsimon@symbiot.com>, 
//         and lots of thanks to the flashcoders online
//------------------------------------------------------------------//

// searchReplace
String.prototype.searchReplace = function(search, replace) {
        return this.split(search).join(replace);
}


// ucWords
String.prototype.ucWords = function() {
	var Strings = this.split("");
	var replace_now = 1;
	for (var i=0; i<Strings.length; i++) {
		if (replace_now) {
      Strings[i] = Strings[i].toUpperCase();
      replace_now = 0;
		}
		// on a space reset replace_now to 1
		if (Strings[i] == " ") {
			replace_now = 1;
		}
	}
	var newString = Strings.join("");

	// trace("ucWords returning newString:"+newString);
	return newString;
}

// tests focus
TextField.prototype.isFocused = function () {
  //  (The 'this' is the current text field instance.)
  if (Selection.getFocus() == targetPath(this))  {
    return true;
  }
  return false;
};


// extend the TextField class
TextField.prototype.onKeyDown = function () {
  if (Key.getCode() == Key.ENTER && this.pressedOnce == undefined && this.isFocused()) {
    this.onSubmit();
    this.pressedOnce = true;
  }
};

TextField.prototype.onKeyUp = function () {
  if (Key.getCode() == Key.ENTER) {
    this.pressedOnce = undefined;
  }
}

// extend the Array Class
Array.prototype.reOrder = function () {
    // make a temporary clone
    var temp:Array = [].concat(this);

    // empty 'this'
    this.splice(0);

    // reorder
    var i:Number = temp.length;
    while (i) {
        this[--i] = temp[i];
    }
}
ASSetPropFlags(Array.prototype, "reOrder", 1);



Array.prototype.array_keys = function() {
	keys = new Array();
	for (i in this) {
		keys.push(i);
		// trace("pusghing i: " + i);
	}
	return keys.reverse();
}

Array.prototype.array_values = function() {
	values = new Array();
	keys = this.array_keys();
	length = keys.length;
	for (var i=0; i<length; i++) {
		values.push(this[keys[i]]);
	}
	return values;
}

Array.prototype.in_array = function(needle) {
	keys = this.array_keys();
	// trace("KEYS:"+keys.join("+"));
	length = keys.length;
	for (var i=0; i<length; i++) {
		// trace("i: " + keys[i]);
		if (this[keys[i]] == needle) {
			return true;
		}
	}
	return false;
}

ASSetPropFlags(Array.prototype, "array_keys,array_values,in_array", 1);


// -------- EXTEND MOVIE CLIP ----------// 


// STYLE IT UPZ
MovieClip.prototype.stylize = function(STYLE) {
 
  this.setStyleProperty("textColor", "0x333333");
  this.setStyle("themeColor", "0xFFFFCC");
  this.setStyle("labelStyle", "_global.DPTxtFormat");
  this.useHandCursor = false;
  //this.setStyle("headerColor", "0xFFFFFF");
  
}

MovieClip.prototype.getMovieClips = function() {
	// trace("getmovieclips for "+this);
	var MC_OBJ = new Object();

  //  Define the array to contain the movie clip references.
  MC_OBJ.MCs = new Array();
  MC_OBJ.TEXTs = new Array();
  MC_OBJ.UKs = new Array();

  //  Loop through all the contents of the movie clip.
  for (var i in this) {
		// trace("this[i]:"+this[i]);
    //  Add any nested movie clips to the array.
    if (this[i] instanceof MovieClip) {
      MC_OBJ.MCs.push(this[i]);
    }
    // text
    else if (typeof(this[i]) == "object") {
			// trace(this+ "TEXTs obj:"+this[i]);
      MC_OBJ.TEXTs.push(this[i]);
    }
    else {
			// trace(this+" get got unknown:"+this[i]+"::"+typeof(this[i]));
			MC_OBK.UKs.push(this[i]);
		}
  }
	// spit back der object
  return MC_OBJ;
}

// uses the getMovieClips method to iteratively walk through and remove and then delete clips
MovieClip.prototype.deleteMovieClips = function() {
	var MC_OBJ = this.getMovieClips();
	// text fields
	var currentTexts = MC_OBJ.TEXTs;
	var length = (currentTexts.length - 1);
	// trace("le:"+length);
	for (var i=length; i>=0; i--) {
		// trace("purging text:"+currentTexts[i]);
		currentTexts[i].removeTextField();
		delete currentTexts[i];
	}

	// movie clips
	var currentClips = MC_OBJ.MCs;
	var length = (currentClips.length - 1);
	// trace("le:"+length);
	for (var i=length; i>=0; i--) {
		currentClips[i].removeMovieClip();
		delete currentClips[i];
	}

	/*
	// save memory? by deleting unknown shite
	var currentUnknown = MC_OBJ.UKs;
	var length = (currentUnknown.length - 1);
	// trace("le:"+length);
	for (var i=length; i>=0; i--) {
		delete currentUnknown[i];
	}
	*/
}


// CENTER point of a clip
MovieClip.prototype.getCenter = function() {

  var point = new Object();
  var contentBounds = this.getBounds(this);

	point.x = contentBounds.xMax/2;
	point.y = contentBounds.yMax/2;
  return point;
}


// DRAW RECTANGLE
MovieClip.prototype.drawParallelogram = function(x, y, w, h, cornerRadius, color, linestyle, offset) {
	//  ==============
	//  mc.drawRect() - by Ric Ewing (ric@formequalsfunction.com) - version 1.1 - 4.7.2002
	// 
	//  x, y = top left corner of rect
	//  w = width of rect
	//  h = height of rect
	//  cornerRadius = [optional] radius of rounding for corners (defaults to 0)
	//  ==============


	if (arguments.length<4) {
		return;
 }

	if (linestyle) {
	  _global.log("LINESTYLE:"+linestyle.size);
    this.lineStyle(linestyle.size, linestyle.color, linestyle.alpha);
	}
	else {
	  _global.log("NO LINESTYLE BITCH");
	}

  // trace("drawing recangle: "+x+","+y+","+w+","+h+","+color);

	// start coloring
	if (color) {
		this.beginFill(color, 100);
	}

	//  if the user has defined cornerRadius our task is a bit more complex. :)

		//  draw top line
		x += offset;
		this.moveTo(x, y);
		this.lineTo(x+w, y);

		x -= offset;
		//  draw right line
		this.lineTo(x+w, y+h);


		//  draw bottom line
		this.lineTo(x, y+h);

		x += offset;
		//  draw left line
		this.lineTo(x, y);


	// stop coloring
	if (color) {
		this.endFill();
	}
};



// DRAW RECTANGLE
MovieClip.prototype.drawRectangle = function(x, y, w, h, cornerRadius, color, linestyle) {
	//  ==============
	//  mc.drawRect() - by Ric Ewing (ric@formequalsfunction.com) - version 1.1 - 4.7.2002
	// 
	//  x, y = top left corner of rect
	//  w = width of rect
	//  h = height of rect
	//  cornerRadius = [optional] radius of rounding for corners (defaults to 0)
	//  ==============


	if (arguments.length<4) {
		return;
 	}

	
	if (linestyle) {
    this.lineStyle(linestyle.size, linestyle.color, linestyle.alpha);
	 lineStyle(linestyle.size, linestyle.color, linestyle.alpha);
	}


  // trace("drawing recangle: "+x+","+y+","+w+","+h+","+color);

	// start coloring
	if (color) {
		this.beginFill(color, 100);
	}

	//  if the user has defined cornerRadius our task is a bit more complex. :)
	if (cornerRadius>0) {
		//  init vars
		var theta, angle, cx, cy, px, py;
		//  make sure that w + h are larger than 2*cornerRadius
		if (cornerRadius>Math.min(w, h)/2) {
			cornerRadius = Math.min(w, h)/2;
		}
		//  theta = 45 degrees in radians
		theta = Math.PI/4;
		//  draw top line
		this.moveTo(x+cornerRadius, y);
		this.lineTo(x+w-cornerRadius, y);
		// angle is currently 90 degrees
		angle = -Math.PI/2;
		//  draw tr corner in two parts
		cx = x+w-cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+w-cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		angle += theta;
		cx = x+w-cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+w-cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		//  draw right line
		this.lineTo(x+w, y+h-cornerRadius);
		//  draw br corner
		angle += theta;
		cx = x+w-cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+h-cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+w-cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+h-cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		angle += theta;
		cx = x+w-cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+h-cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+w-cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+h-cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		//  draw bottom line
		this.lineTo(x+cornerRadius, y+h);
		//  draw bl corner
		angle += theta;
		cx = x+cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+h-cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+h-cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		angle += theta;
		cx = x+cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+h-cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+h-cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		//  draw left line
		this.lineTo(x, y+cornerRadius);
		//  draw tl corner
		angle += theta;
		cx = x+cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
		angle += theta;
		cx = x+cornerRadius+(Math.cos(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		cy = y+cornerRadius+(Math.sin(angle+(theta/2))*cornerRadius/Math.cos(theta/2));
		px = x+cornerRadius+(Math.cos(angle+theta)*cornerRadius);
		py = y+cornerRadius+(Math.sin(angle+theta)*cornerRadius);
		this.curveTo(cx, cy, px, py);
	} 
	else {
		this.moveTo(x, y);
		this.lineTo(x+w, y);
		this.lineTo(x+w, y+h);
		this.lineTo(x, y+h);
		this.lineTo(x, y);
	}
	// stop coloring
	if (color) {
		this.endFill();
	}
};
 
MovieClip.prototype.drawOval = function (x, y, rx, ry) {
    this.moveTo (x+rx, y);
    this.curveTo (rx+x, 0.4142*ry+y, 0.7071*rx+x, 0.7071*ry+y);
    this.curveTo (0.4142*rx+x, ry+y, x, ry+y);
    this.curveTo (-0.4142*rx+x, ry+y, -0.7071*rx+x, 0.7071*ry+y);
    this.curveTo (-rx+x, 0.4142*ry+y, -rx+x, y);
    this.curveTo (-rx+x, -0.4142*ry+y, -0.7071*rx+x, -0.7071*ry+y);
    this.curveTo (-0.4142*rx+x, -ry+y, x, -ry+y);
    this.curveTo (0.4142*rx+x, -ry+y, 0.7071*rx+x, -0.7071*ry+y);
    this.curveTo (rx+x, -0.4142*ry+y, rx+x, y);
}; // adapted from Casper Schuirink


// DRAW CIRCLE
MovieClip.prototype.drawCircle = function(r, x, y, fillColor, linestyle_a, linestyle_b, linestyle_c){
  this.lineStyle(linestyle_a, linestyle_b, linestyle_c);
  if (fillColor) {
    this.beginFill(fillColor, 100);
  }
  
  this.moveTo(x+r, y);
  this.curveTo(r+x, -0.4142*r+y, 0.7071*r+x, -0.7071*r+y);
  this.curveTo(0.4142*r+x, -r+y, x, -r+y);
  this.curveTo(-0.4142*r+x, -r+y, -0.7071*r+x, -0.7071*r+y);
  this.curveTo(-r+x, -0.4142*r+y, -r+x, y);
  this.curveTo(-r+x, 0.4142*r+y, -0.7071*r+x, 0.7071*r+y);
  this.curveTo(-0.4142*r+x, r+y, x, r+y);
  this.curveTo(0.4142*r+x, r+y, 0.7071*r+x, 0.7071*r+y);
  this.curveTo(r+x, 0.4142*r+y, r+x, y);

  
  if (fillColor) {
    this.endFill();
  }

}// [Casper Schuirink]

// DRAW CIRCLE
MovieClip.prototype.drawBall = function(r:Number, x:Number, y:Number, fillColor:String, linestyle_a:Number, linestyle_b:String, linestyle_c:Number){
  this.lineStyle(linestyle_a, linestyle_b, linestyle_c);
  if (fillColor) {
	 var fillC = parseInt(fillColor);
	 
    this.beginGradientFill("radial", [0xCCCCCC,fillC], [60, 90], [50, 0xFF], { matrixType:"box", x:-(r+1), y:-(r+1), w:r+(r/2), h:r+(r/2), r:0 });
  }
  
  this.moveTo(x+r, y);
  this.curveTo(r+x, -0.4142*r+y, 0.7071*r+x, -0.7071*r+y);
  this.curveTo(0.4142*r+x, -r+y, x, -r+y);
  this.curveTo(-0.4142*r+x, -r+y, -0.7071*r+x, -0.7071*r+y);
  this.curveTo(-r+x, -0.4142*r+y, -r+x, y);
  this.curveTo(-r+x, 0.4142*r+y, -0.7071*r+x, 0.7071*r+y);
  this.curveTo(-0.4142*r+x, r+y, x, r+y);
  this.curveTo(0.4142*r+x, r+y, 0.7071*r+x, 0.7071*r+y);
  this.curveTo(r+x, 0.4142*r+y, r+x, y);

  
  if (fillColor) {
    this.endFill();
  }

}// [Casper Schuirink]


/*
MovieClip.prototype.drawCircle = function(radius, x, y, fillColor, linestyle_a, linestyle_b, linestyle_c) {

  this.lineStyle(linestyle_a, linestyle_b, linestyle_c);
  if (fillColor) {
    this.beginFill(fillColor, 100);
  }
  var angleDelta = Math.PI / 4;
  var ctrlDist = radius/Math.cos(angleDelta/2);
  var angle = 0;
  var rx, ry, ax, ay;
  this.moveTo(x + radius, y);
  for (var i = 0; i < 8; i++) {
    angle += angleDelta;
    rx = x + Math.cos(angle-(angleDelta/2))*(ctrlDist);
    ry = y + Math.sin(angle-(angleDelta/2))*(ctrlDist);
    ax = x + Math.cos(angle)*radius;
    ay = y + Math.sin(angle)*radius;
    this.curveTo(rx, ry, ax, ay);
  }
  if (fillColor) {
    this.endFill();
  }
};

*/

// DRAW LINES FROM SRC x,y to DST x,y IN COLOR
MovieClip.prototype.drawLine = function(src, dst, linestylea, linestyleb, linestylec) {
	this.lineStyle(linestylea, linestyleb, linestylec);
	this.moveTo(src.x, src.y);
	this.lineTo(dst.x, dst.y);
}


// GET A DATETIME
Date.prototype.getTimeStamp = function() {

	var DATE_DISPLAY_TEXT = this.getHours()+":";
	var DATE_DISPLAY_MINUTES = this.getMinutes();
	if (DATE_DISPLAY_MINUTES < 10) {
    DATE_DISPLAY_TEXT += "0";
	}
	DATE_DISPLAY_TEXT += DATE_DISPLAY_MINUTES+":";
	var DATE_DISPLAY_SECONDS = this.getSeconds();
	if (DATE_DISPLAY_SECONDS < 10) {
    DATE_DISPLAY_TEXT += "0";
	}
	DATE_DISPLAY_TEXT += DATE_DISPLAY_SECONDS;
	return DATE_DISPLAY_TEXT;
}

