///////////////////////////////////////// "dynTween" MX v1.34 ///////////////////////////////////////////
//June 6th 2002 Tatsuo Kato (Many thanks to Timothee Groleau and Robert Penner for the easing functions)
/////////////////////////////////////////////////////////////////////////////////////////////////////////

/*****************************************************************************************************
"dynTween" allows to apply motion tweens totally dynamically to movieclips.

Usage Example:
myMC.dynTween ( { duration: 40, _x: [400, "out", 30], _y: [200, "in"] } );

As seen there, the parameter(s) should be passed in a form of object(s).
You can pass a series of objects as well.
That will move "myMC" from the current position to (400,200) during 40 frames with easing effects,
"out" with strength 30 for _x and "in" with strength 50 as a default for _y.
There are 4 easing types; "in", "out", "inOut" and "outIn".
"inOut" makes the tween accelerate in the first half and decelerate in the second half.
"outIn" makes the tween decelerate in the first half and accelerate in the second half.
Easing strength can take a value from 0 up to 100.
If no strength is passed, it defaults to 50.
And if no easing is needed at all, either of the following examples is fine.

_x:[400]
or
_x:400


An example of the syntax with all the parameters used would look like:

myMC.dynTween ({duration : 40,
                _x:[300, "outIn", 30], _y:100, _xscale:[200, "out"], _yscale:[-200, "inOut"],
                _rotation:[720, "outIn"], _alpha:30, myProp1:[15, "in"], myProp2:-47,
                callback : "_root.myFunc", cbArgs : {arg1 : 5, arg2 : "hello"}
               },
               {duration : 10},
               {duration : 20, _x : 30}
);

callback is a function to pass, which you want the mc to execute when the tween is done.
Its scope should be _root, that is, 'this' in the function body refers to _root.
If the function which has called the dynTween method is passed as callback, the series of the tweens
will loop until the reset method is called.

cbArgs is an object of parameters to pass to the callback function. If there's only one parameter,
it doesn't need to be an object; just passing a value will be fine.

myProp1 and myProp2 in the example are user-defined variables of myMC.
The values of them can tween too that way if necessary.
You can simultaneously tween as many properties as you want, whether or not they are built-in,
as long as their values are "number".

And you can write a series of as many motion tweens as you want in a single action.
They will be done one after another automatically.

You can also make the caller MC just pause by passing no property arguments.

myMC.dynTween ({duration : 10});

This makes myMC pause for duration of 10 frames.

*****************************************************************************************************/
///////////////////////////////////////// dynTween MX v1.34 ////////////////////////////////////////////
//June 6th 2002 Tatsuo Kato (Many thanks to Timothee Groleau and Robert Penner for the easing functions)
////////////////////////////////////////////////////////////////////////////////////////////////////////

//-----------------------------------------------------------------------------------------------------
// Each parameter should be a tween object that holds the tween data such as:
// {duration:20, _x:[300, "out", 30], _alpha:20}
// , where 300 is the end value of _x and "out" is an easing type and 30 is an easing strength,
// and 20 is the end value of _alpha.
MovieClip.prototype.dynTween = function (tween_objects) {
	var te = this.createEmptyMovieClip("tweenEngine", 20002);
	var h = te.holder = new TweensHolder();
	var i = arguments.length, tweens = h.tweens, T = Tween;
	while (i) {
		tweens.unshift(new T(this, arguments[--i]));
	}
	h.begin();
}

// initData: A list of property name and value pairs, which the caller mc is set to.
MovieClip.prototype.reset = function (initObj) {
	delete this.tweenEngine.removeMovieClip();
	if (initObj) {
		for (var i in initObj) {
			this[i] = initObj[i];
		}
	}
}

ASSetPropFlags(MovieClip.prototype, ["dynTween", "reset"], 1);



//-----------------------------------------------------------------------------------------------------
// A class constructor for an object that contains an array of passed tween objects.
_global.TweensHolder = function () {
	this.tweens = []; // tween objects will be put into this array.
					  // The index order is the execution order.
}
TweensHolder.prototype.begin = function () {
	var t = this.tweens.shift();
	// setUp: prototype'd method of Tween class
	t.setUp();
	this.cnt = 0; // cnt : current frame count
	t.run(this);
}
//-----------------------------------------------------------------------------------------------------
// A class constructor that creates tween objects
_global.Tween = function (mc, tObj) {
	this.init(mc, tObj);
}

// A method that molds easy-to-handle objects out of the passed parameter.
Tween.prototype.init = function (mc, tObj) {
	this.mc = mc;
	this.dur = tObj.duration, delete tObj.duration; // dur : total frame count

	if (tObj.callback != null) {
		this.callback = tObj.callback; // callBack : callback method in a form of a full path string
		delete tObj.callback;
	}
	if (tObj.cbArgs != null) {
		this.cbArgs = tObj.cbArgs; // cbArgs : A list of the parameters to pass to the callback function
		delete tObj.cbArgs;
	}

	this.props = {}; // props: An objects that holds name/value pairs
	for (var i in tObj) {
		var pt = tObj[i];
		var p = this.props[i] = {};
		if (typeof pt == "number") {
			p.end = pt; // end: end value
			p.ease = "none";
		} else {
			p.end = pt[0]; // pt[0]: finishing value
			if (pt[1] == null) { // p[1]: A name of one of the easing functions defined below
				p.ease = "none";
			} else {
				p.ease = pt[1];
				var me = Tween.MAX_EASING;
				// pt[2]: easing strength (0 to 100)
				p.strength = pt[2] ? (me-1) * pt[2] / 100 + 1 : me / 2;
			}
		}
	}
	// Now the instance created with this constructor will look like:
	// {dur:20, props:{_x:{end:300, ease:"out", strength:30}, _alpha:{end:20}}}
}

// A method, with which the tween object sets the beginning values of the properties to the values
// of the mc's beginning state and set the total value changes from them.
Tween.prototype.setUp =  function () {
	var props = this.props
	var mc = this.mc;
	for (var i in props) {
		var p = props[i];
		p.val = p.end - (p.bgn = mc[i]); // val: value change amount, bgn: beginning value
		delete p.end;
	}
	// Now the instance created with this constructor will look like:
	// {dur:20, props:{_x:{bgn:50, val:250, ease:"out", strength:30}, _alpha:{bgn:100, val:-80}}}
}

// run: A method that defines onEnterFrame of the tween engine of the mc
// holder: TweensHolder class instance
Tween.prototype.run = function (holder) {
	var o = this, mc = o.mc, d = o.dur, props = o.props;
	mc.tweenEngine.onEnterFrame = function () {
		var c = ++holder.cnt;
		if (c <= d) {
			for (var i in props) {
				var p = props[i];
				mc[i] = o[p.ease](c, p.bgn, p.val, d, p.strength);
			}
		} else {
			if (holder.tweens.length) {
				holder.begin();
			} else {
				mc.reset();
			}
			eval(o.callback)(o.cbArgs);
		}
	}
}
//-----------------------------------------------------------------------------------------------------

Tween.MAX_EASING = 8;

// easing functions
// c: current frame count, b: beginning value,
// v: total value change,  d: duration, s: strength of the easing
Tween.prototype["none"]  = function (c, b, v, d) { return b + v*c/d;};
Tween.prototype["in"]    = function (c, b, v, d, s) { return b + v*Math.pow((c/d), s);};
Tween.prototype["out"]   = function (c, b, v, d, s) {return b + v*(1 - Math.pow(1-c/d, s));};
Tween.prototype["inOut"] = function (c, b, v, d, s) {
	if (c <= (d/=2)) return b + v/2 * Math.pow(c/d, s);
	return b + v - v/2*Math.pow(2-c/d, s);
}
Tween.prototype["outIn"] = function (c, b, v, d, s) {
	if (c <= (d/=2)) return b + v/2 * (1 - Math.pow(1-c/d, s));
	return b + v/2 *(1 + Math.pow(c/d-1, s));
}
////////////////////////////////////////////////////////////////////////////////////////////////////////

