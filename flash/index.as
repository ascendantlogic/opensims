//------------------------------------------------------------------//
// @LICENSE@
//
// OPENSIMS INDEX 
// authors: Lindsey Simon <lsimon@symbiot.com>
//
//------------------------------------------------------------------//

/* 
There are 3 frames in index.fla.
Frame 1 loads config.xml and parses it.
Frame 2 is blank.
Frame 3 checks that config.xml is done being parsed, 
and then it loads this file, index.as.
*/


// Load some styles for text
#include "global_styles.as"

// Load up the movieClip prototype library
#include "prototype_lib.as"

// Load the global functions library
#include "global_lib.as"

// Load DynTween Libs, thanks to Tatsuo Kato for the tweening prototypes.
// http://www.tatsuokato.com
#include "DynTween.as"
#include "DynColorTween.as"



// ------------------------------------------// 
// content clip for all other movie clips to get loaded into
_root.createEmptyMovieClip("UI_CONTENT", _global.getNewDepth(1));
_root.UI_CONTENT._x = 0;
_root.UI_CONTENT._y = 0;



// Load in live.swf
_global.MCLoader.loadClip("live.swf", _root.UI_CONTENT);

