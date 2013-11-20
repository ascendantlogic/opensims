
/////////////////////////////////////// dynColorTween MX v1.01 /////////////////////////////////////////
//                                    June 6th 2002 Tatsuo Kato
///////////////////////////////////////////////////////////////////////////////////////////////////////
/*
MovieClip.dynColorTween

Player
	Flash Player 6
	
Syntax
	myMC.dynColorTween({duration: frames, [ra:n1, [rb:n2, [ga:n3, [gb:n4, [ba:n5, [bb:n6, [aa:n7, [ab:n8,
						[callback:path, [cbArgs:args]]]]]]]]});
	
Parameters
	Parameters should be passed in a form of object literal(s) which is called color tween object(s) here.
	Each object may contain the following.
	
	duration 
		An integer that specifies the number of the frames during which the tween should happen

	ra, rb. ga, gb, ba, bb, aa, ab
		ra is the percentage for the red component (-100 to 100).
		rb is the offset for the red component (-255 to 255).
		ga is the percentage for the green component (-100 to 100).
		gb is the offset for the green component (-255 to 255).
		ba is the percentage for the blue component (-100 to 100).
		bb is the offset for the blue component (-255 to 255).
		aa is the percentage for alpha (-100 to 100).
		ab is the offset for alpha (-255 to 255).
		
		(See 'Color.setTransform' in the ActionScript Dictionary for more info.)
		
	callback 
		A string of the full path to the function to execute as a callback
		
	cbArgs
		An object that contains the parameters to pass to the callback function
		
Description
	Method; Tweens the color of a movieclip from the current state to the state 
	which is specified with the passed color tween object. 
	A series of color tweens can be applied with one action by passing multiple color tween objects.

Example
	Two usage examples in the frame 2
	
*/
//-----------------------------------------------------------------------------------------------------
MovieClip.prototype.dynColorTween = function (colorTween_objects) {
	// An example of a color tween object to pass. Multiple color tweens can be passed too.
	// {duration:20, ra:50}
	var cte = this.createEmptyMovieClip("colorTweenEngine", 20003);
	var h = cte.holder = new ColorTweensHolder(this);
	var i = arguments.length, tweens = h.tweens, CT = ColorTween;

	while (i) {
		tweens.unshift(new CT(this, arguments[--i]));
	}
	h.begin();
}
MovieClip.prototype.colorReset = function (transformObj) {
	if (transformObj) {
		this.colorTweenEngine.holder.c.setTransform(transformObj);
	}
	this.colorTweenEngine.removeMovieClip();
}
//-----------------------------------------------------------------------------------------------------
_global.ColorTweensHolder = function (mc) {
	this.c = new Color(mc);
	this.transformObj = {};
	this.tweens = [];
}
ColorTweensHolder.prototype.begin = function () {
	var t = this.tweens.shift();
	// Invoke ColorTween.prototype'd method
	t.setUp(this);
	this.cnt = 0; // cnt : current frame count
	t.run(this);
}

_global.ColorTween = function (mc, tObj) { // cTween: Tween Class, tObj: A tween object
	this.init(mc, tObj);
}
ColorTween.prototype.init = function (mc, tObj) {
	this.mc = mc;
	this.dur = tObj.duration,      delete tObj.duration; // dur : total frame count
	
	if (tObj.callback != null) {
		this.callback = tObj.callback;
		delete tObj.callback;
	}
	if (tObj.cbArgs != null) {
		this.cbArgs = tObj.cbArgs; // cbArgs : An object of the parameters to pass to the callback function
		delete tObj.cbArgs;
	}

	this.props = {};
	for (var i in tObj) {
		this.props[i] = {end: tObj[i]}; // end: end value
	}
	// This ColorTween class instance should now look like:
	//{dur:20, props:{ra:{end:50}}}
}

// A method that gets current state of the color of the mc, and sets bgn and val properties.
// val: total value change, bgn: beginning value
ColorTween.prototype.setUp =  function (holder) {
	var props = this.props;
	var transformObj = holder.c.getTransform();
	for (var i in props) {
		var p = props[i];
		p.val = p.end - (p.bgn = transformObj[i]);
		delete p.end;
	}
	// This ColorTween class instance should now look like:
	// {dur:20, props:{ra:{bgn:100, val:-50, end:50}}}
}

ColorTween.prototype.run = function (holder) {
	var o = this, mc = o.mc, d = o.dur, props = o.props, obj = holder.transformObj;
	mc.colorTweenEngine.onEnterFrame = function () {
		var c = ++holder.cnt;
		if (c <= d) {
			for (var i in props) {
				var p = props[i];
				obj[i] = p.bgn + p.val*c/d
			}
			holder.c.setTransform(obj);
		} else {
			if (holder.tweens.length) {
				holder.begin();
			} else {
				mc.colorReset();
			}
			eval(o.callback)(o.cbArgs);
		}
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////////
