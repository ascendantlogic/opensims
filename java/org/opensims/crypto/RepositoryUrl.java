/**
 * @LICENSE@
 */

package org.opensims.crypto;

/**
 * $CVSId: RepositoryUrl.java,v 1.13 2007/05/29 02:04:47 brett Exp $
 * $Id: RepositoryUrl.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.13 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    RepositoryUrl 
{
    // public definitions
    public final static int MAX_BUFFER_SIZE = 102400;

    // protected fields
    protected javax.net.ssl.HttpsURLConnection connection = null;

    // quality assurance
    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.crypto.RepositoryUrl.class.getName());


    /**
     * Constructor.
     */

    public
	RepositoryUrl (org.opensims.Config config, String method, String uri_path, java.util.Hashtable param_table, StringBuffer content) throws Exception
	{	
		if (log_.isDebugEnabled()) {
			log_.debug("Entered RepositoryUrl constructor");
		}
		
		try {
			boolean overriding_args =  false;
			String http_url = config.getParam("repository.url");
	
			System.setProperty("javax.net.ssl.trustStore", config.getParam("auth.truststore_file"));
			System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
	
			// establish the HTTP "basic" authentication
			String user = config.getParam("repository.user");
			String password = config.getParam("repository.password");
		
			// construct the URL-encoded parameters
			StringBuffer url_param = new StringBuffer();
	
			if (log_.isDebugEnabled()) {
				log_.debug("About to create the connection URL");
			}
			
			for (java.util.Enumeration e = param_table.keys(); e.hasMoreElements(); ) 
			{
				overriding_args =  false;
				String param_name = (String) e.nextElement();
				String param_value = (String) param_table.get(param_name);
				
				// Allow for the arbitrary update various parameters (username, password and URL) 
				// to bypass the automatic reading data from the config object --ME
				if (param_name.equals("URL")) {
					http_url = param_value;
					overriding_args = true;
				}
				if (param_name.equals("user")) {
					user = param_value;
					overriding_args = true;
				}
				if (param_name.equals("password")) {
					password = param_value;
					overriding_args = true;
				}
				
				if (! overriding_args) {
					url_param.append((url_param.length() > 0) ? "&" : "?");
					url_param.append(param_name);
					url_param.append("=");
					url_param.append(param_value);
				}
			}
			
			if ((user != null) && (password != null)) {
				org.opensims.crypto.BasicAuthenticator basic_auth = new org.opensims.crypto.BasicAuthenticator(user, password);
				java.net.Authenticator.setDefault(basic_auth);
			}
			
			// open an http connection to the target server
			java.net.URL url = new java.net.URL(http_url + "/" + uri_path + url_param.toString());
			if (log_.isDebugEnabled()) {
				log_.debug("URL: " + url.toString());
			}
			
			setConnection((javax.net.ssl.HttpsURLConnection) url.openConnection());
			if (log_.isDebugEnabled()) {
				log_.debug("Connection type " + getConnection().getClass().getName());
			}
			
			/**
			 * @TODO enable this if the DNS/load balancing causes exceptions with SSL cert name resolution
	
			javax.net.ssl.HostnameVerifier hostname_verifier = new javax.net.ssl.HostnameVerifier () {
				public boolean verify (String hostname, javax.net.ssl.SSLSession session) {
				if (log_.isDebugEnabled()) {
					log_.debug("verify: " + hostname + " - " + session.toString() + " peer " + session.getPeerHost());
				}
	
				return true;
				}
			};
	
			getConnection().setHostnameVerifier(hostname_verifier);
			 */
	
			getConnection().setDoInput(true);
			getConnection().setDoOutput(true);
			getConnection().setRequestMethod(method);
			getConnection().setUseCaches(false);
			getConnection().connect();
	
			if (content != null) {
				postContent(content);
			}
	
			// show the response	
			if (log_.isDebugEnabled()) {
				log_.debug("url connect: response code " + getResponseCode() + " - " + getResponseMessage());
				log_.debug(getContentType());
			}
		}
		catch (Exception e) {
			log_.error("create url", e);
			log_.error("error thrown");
			throw e;
		}
	}


    //////////////////////////////////////////////////////////////////////
    // connection management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the HTTP connection.
     */

    public javax.net.ssl.HttpsURLConnection
	getConnection ()
    {
		return connection;
    }


    /**
     * Set the HTTP connection.
     */

    public void
	setConnection (javax.net.ssl.HttpsURLConnection connection)
    {
		this.connection = connection;
    }


    /**
     * Wrapper for getting the response code.
     */

    public int
	getResponseCode ()
    {
		int response_code = java.net.HttpURLConnection.HTTP_NOT_FOUND;
	
		try {
			response_code = getConnection().getResponseCode();
		}
		catch (Exception e) {
			log_.error("get response code", e);
		}
	
		return response_code;
    }


    /**
     * Wrapper for getting the response message.
     */

    public String
	getResponseMessage ()
    {
		String response_message = "OK";
	
		try {
			response_message = getConnection().getResponseMessage();
		}
		catch (Exception e) {
			log_.error("get response message", e);
		}
	
		return response_message;
    }


    /**
     * Disconnect the HTTP URL connection.
     */

    public void
	disconnect ()
    {
		try {
			getConnection().disconnect();
		}
		catch (Exception e) {
			log_.error("disconnect", e);
		}
    }


    //////////////////////////////////////////////////////////////////////
    // content management
    //////////////////////////////////////////////////////////////////////

    /**
     * Wrapper for getting the content type.
     */

    public String
	getContentType ()
    {
		return getConnection().getContentType();
    }


    /**
     * Returns a BufferedReader for the returned content.
     */

    public java.io.BufferedReader
	getContentReader ()
	throws Exception
    {
		java.io.InputStream in_stream = getConnection().getInputStream();
		java.io.InputStreamReader stream_reader = new java.io.InputStreamReader(in_stream);
	
		return new java.io.BufferedReader(stream_reader);
    }


    /**
     * Get the returned content, accumulated into a StringBuffer.
     */

    public StringBuffer
	getContent ()
	{
		StringBuffer buf = new StringBuffer();
		java.io.BufferedReader buf_reader = null;
	
		try {
			buf_reader = getContentReader();
			String line = buf_reader.readLine();
	
			while (line != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("| " + line);
				}
		
				buf.append(line);
				buf.append("\n");
		
				line = buf_reader.readLine();
			}
		}
		catch (Exception e) {
			log_.error("response content", e);
		}
		finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				}
				catch (Exception e2) {
					log_.error("close reader", e2);
				}
			}
		}
		return buf;
	}


    /**
     * Save the response content as the given file.
     */

    public void
	saveToFile (java.io.File download_file)
	throws Exception
	{
		if (log_.isInfoEnabled()) {
			log_.info("download " + download_file);
			log_.info(getConnection().getHeaderField("Content-Disposition"));
		}
	
		java.io.BufferedInputStream in_stream = null;
		java.io.OutputStream out_stream = null;
	
		try {
			in_stream = new java.io.BufferedInputStream(getConnection().getInputStream());
			out_stream = new java.io.FileOutputStream(download_file);
	
			int count = -1;
			byte[] data = new byte[MAX_BUFFER_SIZE];
	
			while ((count = in_stream.read(data, 0, MAX_BUFFER_SIZE)) != -1) {
				out_stream.write(data, 0, count);
		
				if (log_.isDebugEnabled()) {
					log_.debug("write bytes: " + count);
				}
			}
	
			out_stream.flush();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (in_stream != null) {
				in_stream.close();
			}
	
			if (out_stream != null) {
				out_stream.close();
			}
		}
	}


    /**
     * Post the buffer contents.
     */

    public void
	postContent (StringBuffer buf)
	throws Exception
    {
        java.io.OutputStream out_stream = null;
		String content = buf.toString();

        try {
			out_stream = getConnection().getOutputStream();
	
			if (log_.isDebugEnabled()) {
				log_.debug("post content - " + content.length() + " bytes |" + content + "|");
			}
	
			out_stream.write(content.getBytes(), 0, content.length());
			out_stream.flush();
		}
        catch (Exception e) {
            throw e;
        }
        finally {
            if (out_stream != null) {
                out_stream.close();
			}
		}
	}

}
