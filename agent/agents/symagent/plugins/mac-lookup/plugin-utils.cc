/*
#######################################################################
#		SYMBIOT
#		
#		Real-time Network Threat Modeling
#		(C) 2002-2008 Symbiot, Inc.	---	ALL RIGHTS RESERVED
#		
#		Plugin agent to lookup remote machines' MAC addresses
#		
#		http://www.symbiot.com
#		
#######################################################################
#		Author: Borrowed Time, Inc.
#		e-mail: libsymbiot@bti.net
#		
#		Created:					03 Feb 2004
#		Last Modified:				25 Mar 2004
#		
#######################################################################
*/

//---------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------
#include "plugin-utils.h"

//---------------------------------------------------------------------
// Definitions
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// Module Global Variables
//---------------------------------------------------------------------
static	pthread_key_t									gEnvironKey;
static	ModEnviron*										gMainModEnvironPtr = NULL;

//---------------------------------------------------------------------
// InitModEnviron
//---------------------------------------------------------------------
void InitModEnviron ()
{
	int		errNum = 0;
	
	errNum = pthread_key_create(&gEnvironKey,DestroyModEnviron);
	if (errNum != 0)
		throw TSymLibErrorObj(errNum,"Unable to initialize thread environment");
	
	// Setup a toplevel environmental pointer
	CreateModEnviron();
	SetRunState(true);
	gMainModEnvironPtr = GetModEnviron();
}

//---------------------------------------------------------------------
// DestroyModEnviron
//---------------------------------------------------------------------
void DestroyModEnviron (void* arg)
{
	if (arg)
		delete(reinterpret_cast<ModEnviron*>(arg));
}

//---------------------------------------------------------------------
// CreateModEnviron
//---------------------------------------------------------------------
void CreateModEnviron (ModEnviron* parentEnvironPtr)
{
	ModEnviron*		environPtr = new ModEnviron;
	int				errNum = 0;
	
	if (!parentEnvironPtr && gMainModEnvironPtr)
		parentEnvironPtr = gMainModEnvironPtr;
	
	environPtr->parentEnvironPtr = parentEnvironPtr;
	if (parentEnvironPtr)
		environPtr->runState = parentEnvironPtr->runState;
	else
		environPtr->runState = false;
	environPtr->addressCount = 0;
	
	errNum = pthread_setspecific(gEnvironKey,environPtr);
	if (errNum != 0)
	{
		delete(environPtr);
		throw TSymLibErrorObj(errNum,"Unable to initialize thread environment");
	}
}

//---------------------------------------------------------------------
// GetModEnviron
//---------------------------------------------------------------------
ModEnviron* GetModEnviron ()
{
	return static_cast<ModEnviron*>(pthread_getspecific(gEnvironKey));
}

//---------------------------------------------------------------------
// DoPluginEventLoop
//---------------------------------------------------------------------
bool DoPluginEventLoop ()
{
	bool			result = false;
	ModEnviron*		environPtr = GetModEnviron();
	
	if (environPtr)
		result = environPtr->GetRunState();
	
	return result;
}

//---------------------------------------------------------------------
// SetRunState
//---------------------------------------------------------------------
void SetRunState (bool newState)
{
	ModEnviron*		environPtr = GetModEnviron();
	
	if (environPtr)
		environPtr->SetRunState(newState);
}

//---------------------------------------------------------------------
// StringToNum
//---------------------------------------------------------------------
double StringToNum (const std::string& s)
{
	double				num = 0.0;
	std::istringstream	tempStringStream(s);
	
	tempStringStream >> num;
	
	return num;
}
