/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: SystemScript.java,v 1.3 2006/02/08 03:54:52 mikee Exp $
 * $Id: SystemScript.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.3 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    SystemScript
{
    // protected fields

    protected String[] command_line = null;
    protected Process process = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.SystemScript.class.getName());


    /**
     * Constructor. Use the Runtime environment to execute a script.
     */

    public
	SystemScript (String[] command_line)
    {
		setCommandLine(command_line);
    }


    /**
     * Convert to a String.
     */

    public String
	toString ()
    {
		StringBuffer buf = new StringBuffer(command_line[0]);
	
		for (int i = 1; i < command_line.length; i++) {
			buf.append(" ");
			buf.append(command_line[i]);
		}
	
		return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the script's Process.
     */

    public Process
	getProcess ()
    {
		return process;
    }


    /**
     * Set the script's Process.
     */

    public void
	setProcess (Process process)
    {
		this.process = process;
    }


    /**
     * Get the script's command line.
     */

    public String[]
	getCommandLine ()
    {
		return command_line;
    }


    /**
     * Set the script's command line.
     */

    public void
	setCommandLine (String[] command_line)
    {
		this.command_line = command_line;
    }


    //////////////////////////////////////////////////////////////////////
    // runtime methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Run the script as a child process.
     */

    public void
	run ()
	throws Exception
    {
		setProcess(Runtime.getRuntime().exec(getCommandLine()));
		getProcess().waitFor();
    }


    /**
     * Get the exit code.
     */

    public int
	getExitCode ()
    {
		return getProcess().exitValue();
    }


    //////////////////////////////////////////////////////////////////////
    // output handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Accumulate the standard output stream.
     */

    public StringBuffer
	getStdOut ()
	throws Exception
	{
		StringBuffer buf = new StringBuffer();
	
		// accumulate the stdout stream	
		java.io.InputStream input_stream = getProcess().getInputStream();
		java.io.InputStreamReader stream_reader = new java.io.InputStreamReader(input_stream);
		java.io.BufferedReader buf_reader = new java.io.BufferedReader(stream_reader);
		String line = null;
	
		while (buf_reader.ready()) {
			line = buf_reader.readLine();
	
			if (line != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("out: " + line);
				}
		
				buf.append(line.trim()).append("\n");
			}
		}
		buf_reader.close();
	
		// clean the control characters out of the log	
		for (int i = 0; i < buf.length(); i++) {
			char c = buf.charAt(i);
	
			if (Character.isIdentifierIgnorable(c)) {
				buf.setCharAt(i, '\n');
			}
		}
	
		return buf;
	}


    /**
     * Accumulate the standard error stream.
     */

    public StringBuffer
	getStdErr ()
	throws Exception
	{
		StringBuffer buf = new StringBuffer();
	
		// accumulate the stderr stream
		java.io.InputStream input_stream = getProcess().getErrorStream();
		java.io.InputStreamReader stream_reader = new java.io.InputStreamReader(input_stream);
		java.io.BufferedReader buf_reader = new java.io.BufferedReader(stream_reader);
		String line = null;
	
		while (buf_reader.ready()) {
			line = buf_reader.readLine();
	
			if (line != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("err: " + line);
				}
		
				buf.append(line.trim()).append("\n");
				}
		}
	
		buf_reader.close();
	
		// show the exit code
		line = "exit status: " + String.valueOf(getExitCode());
	
		if (log_.isDebugEnabled()) {
			log_.debug(line);
		}
	
		buf.append(line.trim());
	
		// clean the control characters out of the log
		for (int i = 0; i < buf.length(); i++) {
			char c = buf.charAt(i);
	
			if (Character.isIdentifierIgnorable(c)) {
				buf.setCharAt(i, '\n');
			}
		}
		return buf;
	}
	
}
