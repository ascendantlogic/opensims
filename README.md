### OpenSIMS 1.0 ###
Below is the README as it was packaged up for the last release on June 14th, 2008. Email and web addresses may or may not still be used. This is just for preservation on something other than SourceForge (currently) since SourceForge [has apparently sold their soul to the devil](http://www.theregister.co.uk/2013/11/08/gimp_dumps_sourceforge_over_dodgy_ads_and_installer/).

    OpenSIMS / AgentSDK / Ceteri
    $CVSId: README,v 1.96 2006/05/18 23:05:20 mikee Exp $
    $Id: README 1 2008-01-10 18:37:05Z smoot $
    
    Paco NATHAN <paco@symbiot.com>
    Mike W. ERWIN <mikee@symbiot.com>
    Jamie PUGH <jamie@symbiot.com>
    Dan CAMPER <dan@bti.net>
    Lindsey SIMON <lsimon@symbiot.com>
    Jim NASBY <jnasby@symbiot.com>
    William HURLEY <whurley@symbiot.com>
    Mark ZAVALA <mzavala@symbiot.com>
    
    @LICENSE@
    
    
    ######################################################################
    
        1. INTRODUCTION
    
    Project OpenSIMS includes three main components:
    
        * OpenSIMS -  security infrastructure management system
      * AgentSDK -  software developers kit for security agents
      * Ceteri   -  collaborative framework for risk metrics
    
    The essential concept is that most networking environments (such as
    for the web server from which you downloaded this text) have some kind
    of network security devices -- or at least some network equipment
    which provides security features: routers, switches, firewalls, IDS,
    anti-spam, anti-virus, web servers, mail servers, NAT, VPN,
    authentication, etc.  If your equipment lives at a high-end NOC, it
    might be running under the watchful eye of something like HP OpenView
    or IBM Tivoli which can monitor many devices.  If you've got big bucks
    to spend in your IT budget, you might have IPS devices running instead
    of firewalls, effectively coupling the firewall and IDS functions.
    
    Looking at the market stats, however, those scenarios are probably not
    your scenario.  So why not get some application which can create
    coupling for the equipment that you do have?  Right, that would be a
    SIMS.  Here we present a code base for an open source SIMS.
    
    The AgentSDK provides the "glue" used to link together all those
    different kinds of networking and security devices.  Its plugin
    architecture allows highly extensible support for many different
    vendors.  Running in the context of Tomcat 5.x or some commercial
    servlet container, the agents provide an effective security model
    based on mutual authentication over TLS.  The end results include
    support for:
    
      * network autodiscovery
      * log aggregation
      * security alert correlation
      * network behavioral modeling
      * countermeasures
    
    Most of the OpenSIMS web application is written in Java (and related
    XML tools), so it makes sense that the reference implementation be
    based on the Tomcat application server.  The code is general enough to
    deploy on other servlet containers.  It also includes a Flash-based
    GUI.  Flash is not open source, however the Flash players are
    available for most popular platforms.
    
    The other main component, Ceteri, is a project aimed at producing a
    collaborative framework for exchanging risk metrics.  This is based on
    using Tomcat and Axis for the platform reference implementation, with
    support for Apache through a module which wraps the AgentSDK.
    
    There are a few good analogies for the concept of collaborative risk
    metrics.  One is the means by which financial firms analyze risk in
    foreign currency trading.  Another is how credit card firms develop
    credit ratings and fraud profiles.  Perhaps an even better analogy is
    found in how exterior routing protocols exchange metrics to adapt
    their routing topologies.
    
    For more details, see our homepage:  http://opensims.org/
    
    
    ######################################################################
    
        2. DEPENDENCIES
    
    It pays to keep in mind that intelligent security infrastructure
    management is largely a systems integration problem; the acronym
    "SIMS" begins with the letters "SI" for good reason.  It is expected
    that the OpenSIMS operators will have plenty of experience as system
    administrators, integrating many different open source systems.
    
    The total time required for an OpenSIMS installation on a bare
    RedHat/Fedora box is expected to be about an hour, depending on
    download rates.  For now, it is probably best to download and compile
    the dependencies from source, rather than relying on RPMs, which may
    be significantly out of date.
    
    
        2.1 - Open Source Projects Required For Install/Runtime
    
    To install this release of OpenSIMS, it is assumed that you already
    have the following installed and running:
    
      * gcc (v3.3.2 or later)
        http://gcc.gnu.org/
    
      * openssl (v0.9.7d or later)
        http://www.openssl.org/
    
      * Perl (v5.8.x or later)
        http://www.perl.org/
    
      * Perl packages "Getopt::Std", "HTML::Entities"
        http://www.cpan.org/
    
      * Snort (v2.0.4 or later)
        http://www.snort.org/
    
      * NMAP (v3.50 or later)
        http://www.insecure.org/nmap/
    
    You probably have those already, if you're working with network
    security infrastructure.
    
    
        2.2 - Apache/Jakarta Dependencies
    
    These might take a bit more work for those who haven't worked much in
    Java before, but there are plenty of good HOWTO docs online.
    
    The three components that are needed include:
    
      * Java J2SE SDK (v1.4.2 or later)
        http://java.sun.com/j2se/
    
      * Ant (v1.6.1 or later)
        http://ant.apache.org/
    
      * Tomcat (v5.x or later)
        http://jakarta.apache.org/tomcat/
    
    On some systems which include an older version of Ant (such as Fedora)
    you'll probably need to remove the previous version of "/etc/ant.conf"
    first to avoid installation errors.
    
    We plan to automate checking for these dependencies, and arranging for
    packages in various distros.
    
    
        2.4 - Select A Database Vendor
    
    As of the most recent build (OpenSIMS v0.6.0b) a database server is
    NOW REQUIRED for running OpenSIMS.  We use Torque to generate the SQL
    schema and Java wrappers, for whichever the database platform you
    select.
    
    First, on a new OpenSIMS installation, you need to generate some build
    properties:
    
      ant -v init
    
    Edit the generated "build/build.properties" file so that it lists the
    correct database vendor, user, and password.  Next, connect to the
    "tools/torque" directory.  Under the "database" subdirectory, there
    are templates for each of the supported vendors.  We have tested so
    far on:
    
      * PostgreSQL (v7.4.3 or later)
        http://www.postgresql.org/
    
      * MySQL (v4.0.18 or later)
        http://www.mysql.com/
    
      * Oracle (v10g or later)
        http://www.oracle.com/
    
    Because the database connections are based on JDBC, you need to
    configure your database server to provide a TCP listener; JDBC cannot
    connect through Unix sockets.
    
    If you are using one of the databases listed above, SKIP THE REST OF
    THIS SECTION.
    
    Otherwise, if you are using some different kind of database, edit your
    "build/build.properties" and "tools/torque/database/VENDOR" files to
    provide the correct means for making a DB connection on your server.
    Then in the "tools/torque" directory, run:
    
      ant preflight clean config build test datadump
    
    That build will shoot a bunch o' logging to console.  You can ignore
    most of the warnings, but hopefully you will see a couple of host
    definitions being used to test Java access for INSERT and SELECT.
    
    To test the Torque results, take a look at the "opensims" database (or
    schema, depending on your vendor) to insure that the following tables
    have been created:
    
      alert
      alert_def
      alert_type
      host
      id_table
    
    If so, your SQL schema is in place, the Java source for its
    wrappers has been generated, and you should be fine for resuming
    the OpenSIMS installation.
    
    BTW, we're eager to hear about other databases which get used for
    OpenSIMS installations.
    
    
        2.4 - We've Tackled The Hard Problems
    
    Given that the forgoing requirements are in place, the rest of the
    install should run automagically.  Yeah, that may look like a lot of
    install work... But think about this: it's essentially (1) some
    popular open source OS on a box, (2) serving JSP pages, (3) providing
    some kind of database, and (4) running Snort -- reasonably standard
    fare for a web server.
    
    Packaged builds are underway for RedHat/Fedora (rpm), Gentoo (ebuild),
    Debian (deb), FreeBSD (port), Mac OS X (pkg), and a Windows installer.
    We're looking for people to join the OpenSIMS project to shepherd each
    platform family.
    
    The tarball download for OpenSIMS already includes the following
    required, third-party open source components:
    
      * log4j (v1.2.8 or later)
        http://logging.apache.org/log4j/docs/
    
      * JUnit (v3.8 or later)
        http://www.junit.org/
    
      * JDOM (v0.10b or later)
        http://www.jdom.org/
    
      * JavaMail API (v1.3.1 or later)
        http://java.sun.com/products/javamail/
    
      * JavaBeans Activation Framework (v1.0.2 or later)
        http://java.sun.com/products/javabeans/jaf/
    
      * Axis (v1.1 or later)
        http://ws.apache.org/axis/
    
      * Torque (v3.1.x or later)
        http://db.apache.org/torque/
    
      * GeoIP (Java API v1.1.4 or later and GPL country database)
        http://www.maxmind.com/app/java
    
    If your system requires other versions, you may need to refer to the
    build scripts for the Torque database generator and the OpenSIMS
    webapp, and change their shared classpath settings accordingly.
    
    TROUBLESHOOT: About the log4j JAR file...  On many platforms, the
    log4j JAR must be referenced from the webapp "lib" directory to avoid
    a security violation from the class loader, and the webapp build
    script will handle that automatically.  So, if you use some other
    log4j JAR file, it should not live in the Tomcat "common/lib"
    directory, for that reason.
    
    TROUBLESHOOT: The installation of JDOM also includes JAR files for the
    SAXPath, Jaxen, and Xerces-j distributions.  Even if you use a JDOM
    version other than what is included in the OpenSIMS download, leave
    all those other JAR files under the JDOM directory, just where the
    are.  Or you must modify the webapp build script, accordingly.
    
    
        2.5 - Los Gotchas
    
    There are certain to be other gotchas which we haven't discovered.
    Let us know what you find: info@opensims.org
    
    
    ######################################################################
    
        3. GETTING STARTED
    
    Installations for OpenSIMS and AgentSDK have been tested on the
    following platforms:
    
      * Gentoo    stable
      * Fedora    stable
      * RedHat Enterprise stable
      * Debian "Testing"  stable
      * FreeBSD   in test
      * Mac OS X    in test
      * Solaris   AgentSDK only, so far
      * Win2k/WinXP   AgentSDK only, so far
    
    See the "docs" directory for platform-specific README files.
    
    The following steps are required to build OpenSIMS and AgentSDK, based
    on using Tomcat 5.x as the servlet container.
    
    
        3.1 - Distribution Tarball
    
    Download and extract the tarball to a particular directory, for
    example under one of "/opt/", "/usr/local/", "/usr/share/" on Unix or
    Linux.
    
    
        3.2 - System Properties
    
    Check that some non-root user has been created for running Tomcat,
    such as a "tomcat" user.  For example:
    
      /usr/sbin/groupadd tomcat
      /usr/sbin/useradd -g tomcat tomcat
    
    Be sure that the environment variables are set correctly in the root
    shell which will run the installation scripts, for PATH, JAVA_HOME,
    ANT_HOME, CATALINA_HOME, and CLASSPATH.  For example, you might need
    to append to "/etc/profile" with something that resembles:
    
      export JAVA_HOME=/opt/j2sdk1.4.2_05
      export ANT_HOME=/opt/apache-ant-1.6.1
      export CATALINA_HOME=/opt/tomcat
      export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:${PATH}
      export CLASSPATH="$JAVA_HOME/lib/tools.jar:$CLASSPATH"
    
    Edit the file "build/build.properties" to suit your platform's file
    layout and paths to executables for each named dependency.  For
    example, specify file locations if your installation of Snort or
    Tomcat has been customized.  Whatever you add to "build.properties"
    will override the default settings in "default.properties", as is
    quite often the case in Ant build scripts.
    
    TROUBLESHOOT: Do not change the truststore and keystore passphrases,
    just leave them for now.
    
    
    Then select which database platform you prefer to use.  For example,
    here is how a typical configuration for PostgreSQL looks:
    
      db.platform = postgresql
      db.jdbc.driver = org.postgresql.Driver
      db.jdbc.jar = /opt/postgresql-7.4.3/lib/pg74.215.jdbc3.jar
      db.user = root
      db.password = foo
    
    The default settings pre-configured in this download are, roughly
    speaking, what one would use for RedHat with PostgreSQL.
    
    TROUBLESHOOT: you may need to edit "hosts.allow" on a Linux/Unix
    server to let JDBC connect to your database TCP listener.
    
    
        3.3 - Installer Script
    
    First, you might want to run the "preflight" checks, using the Ant
    build script in the base directory.  That will test the versions found
    for each dependency, and will also attempt to connect to the database
    server:
    
      ant
    
    If all goes well, next run the same script again (as the root user),
    this time invoking the OpenSIMS installer:
    
      ant -v install | tee install.txt
    
    That will call several other installer scripts in subdirectories,
    following the layout given in Section 4 - FILE LAYOUT.
    
    If you get prompted about whether or not you prefer to "handle SSL
    keys with high security", you probably want to answer "no".  Otherwise
    you must enter a private key passphrase each time your application
    server or agents restart.
    
    At the end of a successful installation, the build script prompts for
    whether you want to send an email back to OpenSIMS.org, requesting
    that your Certificate Signing Request be signed by the OpenSIMS
    Project.  If you agree, OpenSIMS will email back a signed cert, so
    that your OpenSIMS installation may use secure web services (mutual
    authentication) to obtain notifications about OpenSIMS source code
    upgrades and updated security data.
    
    TROUBLESHOOT: If you run into other troubles with the install, please
    send email to <info@opensims.org> with a copy of your "install.txt"
    file.  We like to see log files attached along with any installation
    bug reports; of course, edit the log to scrub out any details you
    don't care to share.
    
    TROUBLESHOOT: If the KeyManager fails during the "openssl" calls to
    generate keys, it is probably because you have a version which does
    not support some of the command-line options being used.  The simplest
    fix is to go into "opensims/tools/keymgr/build.xml" and comment out
    the two "<arg/>" statements for "-set_serial" and its parameter.
    
    TROUBLESHOOT: The scripts which detect the network interface
    configuration are expecting to have something sane come out of your
    "ifconfig" utility, in terms of network, broadcast, mask and IP
    address numbers.  If, for instance, you have an ISP which sends
    whacked out numbers for DHCP (i.e., Time Warner) then the CIDRs
    generated by the OpenSIMS scripts make look a wee bit strange.  Choose
    some network/interface during the install, then go back and edit the
    "webapp.xml" config file afterwards.  We have begun to build
    regression testing for the various failure modes from wacky ISPs;
    email us a session from your "ifconfig" if you see troubles like that
    and we'll work that into our bug fixes and regression testing.
    
    
        3.4 - Application Server
    
    For the examples given here, we assume that you are working with
    Tomcat 5.x running standalone.  The SSL port (default 8443) needs to
    be the same as what is specified system properties in the OpenSIMS
    "build.properties" file.  OpenSIMS expects to have an SSL listener
    provided by Tomcat 5.x or some similar web container.
    
    Optionally, you can run through a Jk connector from Apache, if you
    must run Tomcat 4.x instead, but you'll need to grok "mod_jk" quite
    well and not bug us when it croaks.
    
    Edit the Tomcat config "$CATALINA_HOME/conf/server.xml", by doing a
    Copy/paste in the SSL HTTP/1.1 connector definition listed below.  It
    has its port set to 8443 -- or use another, so long as it is defined
    in your "build.properties" file.
    
        <Connector
         className="org.apache.coyote.tomcat5.CoyoteConnector"
         debug="1"
         scheme="https"
         port="8443"
         acceptCount="100"
         maxThreads="150"
         minSpareThreads="25"
         maxSpareThreads="75"
         enableLookups="false"
         disableUploadTimeout="true"
         tcpNoDelay="true"
         connectionLinger="-1"
         connectionTimeout="600000"
         connectionUploadTimeout="600000"
         keepAlive="true"
         maxKeepAliveRequests="-1"
         serverSocketTimeout="0"
         secure="true"
         sslProtocol="TLS"
         algorithm="SunX509"
         truststoreFile="/var/ssl/truststore.jks"
         truststorePass="changeit"
         truststoreType="JKS"
         keystoreFile="/var/ssl/keystore.jks"
         keystorePass="symbiot"
         keystoreType="JKS"
         clientAuth="false"
         compression="off"
        />
    
    
    In any case, be certain to enable "keep-alives" for persistent
    connections (which the agents use) set to the maximum possible.  For
    the Tomcat 5.x example "<Connector>" listed above, the attribute
    "connectionTimeout" is set to "-1" for this effect. If that point gets
    missed, agents may disconnect prematurely.
    
    The most recently updated documentation for these connector options is
    available online at the Jakarta Tomcat site:
    
      http://jakarta.apache.org/tomcat/tomcat-5.0-doc/config/http.html
    
    After that, add again to "$CATALINA_HOME/conf/server.xml" with these
    XML elements as content inside of the existing "<Host>" element.  You
    may need to change the paths, depending on your file layout:
    
        <Context
         className="org.apache.catalina.core.StandardContext"
         debug="1"
         docBase="${CATALINA_HOME}/webapps/opensims" 
         displayName="OpenSIMS"
         path="/opensims"
         charsetMapperClass="org.apache.catalina.util.CharsetMapper"
         mapperClass="org.apache.catalina.core.StandardContextMapper"
         wrapperClass="org.apache.catalina.core.StandardWrapper"
         cachingAllowed="false"
         cookies="true"
         crossContext="false"
         privileged="false"
         reloadable="false"
         swallowOutput="false"
         useNaming="true"
        >
          <Environment
           name="client.timeout"
           description="Client Session Timeout Period in seconds"
           override="true"
           type="java.lang.Integer"
           value="300"
          />
          <Environment
           name="debug.flash"
           description="Flash Client Debug Flag (0/1)"
           override="false"
           type="java.lang.String"
           value="0"
          />
       </Context>
    
    TROUBLESHOOT: If the use of log4j in the webapp throws security
    exceptions -- typically about "ClassLoader" permissions.  Try
    disabling the "-security" option in the Tomcat startup, if you see
    those kinds of exceptions in the Tomcat logs.
    
    
        3.5 - Snort Configuration
    
    The agent that watches Snort expects to see alerts in a particular
    format.  The config for Snort needs to include these command line
    options for running the daemon:
    
      -y    show year in timestamp
      -I    show network interface
      -A fast   fast file format
    
    If your "rules" directory is not inside the Snort "/etc/conf"
    directory as usual, be sure to edit the "build.properties" for Snort
    settings.
    
    You probably will want to turn off some of the less-significant Snort
    rules which will cause much noise in the OpenSIMS analysis:
    
      /etc/snort/bad-traffic.rules
    
      #alert ip any any <> 127.0.0.0/8 any (msg:"BAD-TRAFFIC loopback
      traffic"; classtype:bad-unknown;
      reference:url,rr.sans.org/firewall/egress.php; sid:528; rev:4;)
      #alert ip any any -> any any (msg:"BAD-TRAFFIC same SRC/DST"; sameip;
      reference:cve,CVE-1999-0016;
      reference:url,www.cert.org/advisories/CA-1997-28.html;
      classtype:bad-unknown; sid:527; rev:4;)
    
    Note that the "alert_def.xml" file includes features for disabling and
    filtering rules from security devices, including Snort.  Those
    features will get enabled in an upcoming release.
    
    
        3.6 - Custom Configurations
    
    Edit the generated config file "/etc/opensims/webapp.xml" as needed.
    You probably won't need to do that much, except for peripheral
    features such as notification, or whenever your network configuration
    changes.
    
    
        3.7 - Daemonic Invocations
    
    Start these required services, in order, given that network and
    database services are already running -- and Snort plus any other
    security devices being correlated:
    
      tomcat
      symagent
    
    More of the overall installation process will become automated as we
    get feedback about OpenSIMS on specific platforms, and as we get
    packaged in the distros.
    
    TROUBLESHOOT: you need be sure to open ports 8443:8445 on your
    firewall for the OpenSIMS agents, clients, and webapp to use.
    
    
        3.8 - Operation
    
    Once you've got Tomcat running with the OpenSIMS webapp servlets and
    accepting input from the agents, then take a look at the diagnostics
    page:
    
      https://YOUR_DOMAIN:8443/opensims/test
    
    That should show a text dump of the internal memory structures -- what
    we use for QA back in the OpenSIMS lab.  If you see "bogey" and
    "alert" entries, congrads!  At this point you'll likely see the same
    stuff showing up animated in the Flash GUI, so try pointing your
    browser at:
    
      https://YOUR_DOMAIN:8443/opensims/
    
    Then click on the "Live" button and you should start to see the
    animation.  NB: in the current correlation state machine, alerts will
    be displayed if they involve a host which is known by autodiscovery to
    be on your network; others are ignored as false positives.
    
    TROUBLESHOOT: If the browser shows "Server Socket Error" when you
    click on the "Live" button, then the Flash XMLSocket listener (default
    port: 8445/tcp) is probably not running.  Sometimes, if you try to
    stop/start Tomcat too rapidly, the previous listener socket will not
    have enough time to shutdown before the new instance gets attempted.
    The required wait interval depends on the operating system, but it can
    be up to a couple of minutes.  If that is the case, you'll probably
    see the following errors in the Tomcat logs:
    
      java.net.BindException: Address already in use
    
    The best approach is to stop the symagent service, stop the tomcat
    service, "kill -9" any orphaned processes for either, then wait a
    couple of minutes before starting those two services again.  On a
    Linux/Unix system, you can use "netstat -an | grep LISTEN" to monitor
    the state of listener sockets.
    
    TROUBLESHOOT: Alerts disapper after a while.  Current settings will
    delete information about attacks and attackers from the database after
    72 hours.  Edit the "webapp.xml" config file, and change the
    "data.age_limit" parameter to modify how long your attack data
    persists.  We've tested this with high data rates, but depending on
    the file system, database, and processors... your mileage may vary.
    Suggestion: try running Gentoo on a dual-processor box with
    hyperthreading.
    
    
        3.9 - Troubleshooting
    
    It's probably still a little too early to be writing all the FAQs, at
    least until we've had some installs running on several different
    systems.  We should have some user/developer forums up soon.  For now,
    contact us directly and somebody will try to help get the problems
    fixed:
    
      info@opensims.org
    
    One good practice for debugging an OpenSIMS in operation is to tail
    the logs together:
    
      cd /var/log
      tail -f opensims/* tomcat/* snort/alert
    
    Something along those lines.  We like to see those kinds of log files
    attached alongside any runtime bug reports, too.
    
    
        3.10 - Compiling From Source Code
    
    SKIP THIS SECTION IF you are using OpenSIMS but not working with its
    source code as a developer.  The following dependencies are only
    required for development and QA of the webapp, so you won't need them
    for OpenSIMS at runtime.  If you plan to work with the Java source
    code, here's what you'll need:
    
      * Smack (v1.3.0 or later)
        http://www.jivesoftware.com/xmpp/smack/
    
      * Informa (v0.5.5 or later)
        http://informa.sourceforge.net/
    
      * Velocity/Anakia (v1.4 or later)
        http://jakarta.apache.org/velocity/anakia.html
    
      * FindBugs (v0.8.2 or later)
        http://findbugs.sourceforge.net/
    
      * SLOCCount (v2.24 or later)
        http://www.dwheeler.com/sloccount/
    
    
    Be sure to follow the FindBugs installation notes and copy its JAR
    file "findbugs-ant.jar" into the $ANT_HOME/lib directory.
    
    One gotcha is that some components (such as MaxMind's GeoIP) are
    distributed as GPL'ed source code, and the OpenSIMS webapp build
    script expects to use JAR files for third-party Java code.  Most other
    project package their code in JAR files, so that's a fairly reasonable
    assumption.  In the case of GeoIP, there's a tool included in OpenSIMS
    which builds the required JAR file.  See the "tools/geoip" build
    script.
    
    Beyond those caveats, the best places to start working with OpenSIMS
    source code are in the "agent", "webapp", or "flash" directories.
    
    
    ######################################################################
    
        4. FILE LAYOUT
    
    opensims          (release distribution - OpenSIMS home directory)
      |
      +- README         (you're soaking in it)
      |
      +- LICENSE          (OpenSIMS/AgentSDK/Ceteri license agreement)
      |
      +- build          (properties files, build targets)
          |
          +- build.properties     (OpenSIMS customized system properties)
          |
          +- default.properties     (OpenSIMS default system properties)
          |
          +- ifconf.properties      (selected network properties)
          |
          +- keymgr.properties      (SSL cert distinguishing name properties)
          |
          +- log4j.properties     (OpenSIMS logging properties)
          |
          +- torque.properties      (Torque schema generator/runtime properties)
          |
          +- release.properties     (OpenSIMS build/version information)
          |
          +- properties.xml       (shared properties for Ant build scripts)
          |
          +- opensims.war       (OpenSIMS compile/build target - WAR file)
      |
      +- build.xml          (OpenSIMS/AgentSDK system build script)
      |
      +- agent          (reuse for installing AgentSDK on remote hosts)
          |
          +- sdk.xml        (XML definitions for recursive build script)
          |
          +- agents         (source code for Agents and Plugins)
          |
          +- library        (source code for "libsymbiot" library)
      |
      +- docs         (static web content)
          |
          +- images         (images used in the webapp reporting and Flash GUI)
          |
          +- javadoc        (JavaDoc for the OpenSIMS webapp Java API)
          |
          +- manual         (OpenSIMS manual XML source and Velocity/Anakia scripts)
          |
          +- pdf          (PDF versions of developer notes)
          |
          +- qa         (reports generated by unit metrics)
          |
          +- torque         (Torque-generated schema docs for OpenSIMS database)
      |
      +- flash          (Flash GUI source code and config files - for dev only)
      |
      +- java
          |
          +- classpath.xml        (shared Java classpath for third-party dependencies)
          |
          +- lib          (third-party Java libraries)
          |
          +- org
              |
              +- opensims       (OpenSIMS webapp Java API source code - for dev only)
      |
      +- tools          (tools to integrate third-party components)
          |
          +- depend         ("preflight" dependencies checking)
          |
          +- geoip          (MaxMind GeoIP integration)
          |
          +- ifconf         (detect network interface configuration)
          |
          +- keymgr         (SSL key management)
          |
          +- nagios         (Nagios integration)
          |
          +- report         (report development test rig)
          |
          +- snort          (Snort integration)
          |
          +- torque         (Torque scripts to generate XML/SQL/Java db access)
             |
             +- database        (vendor-specific database properties)
             |
             +- lib         (location for shared JAR files)
             |
             +- schema        (XML schema definitions used by Torque)
                |
                +- opensims-schema.xml    (XML schema definition for OpenSIMS database)
                |
                +- opensims-data.dtd    (XML schema DTD for database dump/backup in XML)
             |
             +- src         (SQL files generated by Torque)
                |
                +- opensims-schema.sql    (SQL database schema)
                |
                +- opensims-opensims-all-data.xml (SQL database dump/backup in XML)
      |
      +- webapp         (build scripts for OpenSIMS webapp and Flash GUI)
    
    
    $CATALINA_HOME          (Tomcat home directory)
      |
      +- conf
          |
          +- server.xml       (modify to add the special OpenSIMS webapp <Context/>)
      |
      +- common
          |
          +- lib          (location for shared JAR files)
      |
      +- webapps
          |
          +- opensims       (root dir for OpenSIMS webapp and Flash GUI)
              |
              +- config.xml       (Flash GUI config/menu/layout - runtime)
              |
              +- ALT.xml        (alert menu items)
              |
              +- ATK.xml        (attacker menu items)
              |
              +- DEF.xml        (defender menu items)
              |
              +- uml        (OpenSIMS UML finite state machine definitions)
              |
              +- xsl        (OpenSIMS XSLT templates)
    
    
    /etc
      |
      +- opensims         (OpenSIMS/AgentSDK config files)
          |
          +- webapp.xml       (OpenSIMS webapp config - edit it to set runtime properties)
          |
          +- symagent.xml       (AgentSDK local agent config)
          |
          +- certs          (AgentSDK links to SSL certs)
    
    
    /var
      |
      +- lib
          |
          +- opensims       (OpenSIMS library files, XML data sources)
              |
              +- ${NETWORK}.model.xml   (XML representation of network model, from autodiscovery)
              |
              +- ifconfig.xml     (XML representation of network interface configuration)
              |
              +- ${FQDN}.req.pem      (OpenSIMS cert signing request sent to the Ceteri repository)
              |
              +- ${FQDN}.signed.pem     (OpenSIMS signed cert retured from the Ceteri repository)
              |
              +- alert_defs.xml     (OpenSIMS alert definitions - top-level overlay)
              |
              +- alert_types_template.xml   (OpenSIMS alert types / IDS rule patterns)
              |
              +- snort_alert_defs.xml   (OpenSIMS alert definitions derived from Snort rules)
              |
              +- world.xml        (optional XML file that fixes ISO 3166 country code info)
      |
      +- log
          |
          +- opensims       (OpenSIMS log files)
              |
              +- symagent_error_log.txt   (AgentSDK runtime error log)
              |
              +- symagent_message_log.txt   (AgentSDK runtime debug log)
              |
              +- webapp_message_log.txt   (OpenSIMS runtime debug log)
      |
      +- ssl          (SSL keys/certs)
          |
          +- truststore.jks       (Java SSL truststore)
          |
          +- keystore.jks       (Java SSL keystore)
          |
          +- symbiotnetcacert.pem     (Ceteri repository RootCA public cert)
          |
          +- cacert.pem       (local RootCA public cert)
          |
          +- private
              |
              +- cakey.pem        (local RootCA private key)
          |
          +- certs
              |
              +- ${FQDN}.pem      (local application server cert)
    
    
    ######################################################################
    
        5. HIGHLIGHTS / TO-DO's / CAVEATS
    
    KeyManager.  Ant scripts which tie together the magic Mike invoked to
    leverage complementary features from both "openssl" and "keytool" to
    support mutual authentication simultaneously in both Apache (pem) and
    Tomcat (jks) as application servers.  In other words, making C/C++ and
    Java play nice together.  Not readily found elsewhere.  The chain of
    authority is crafted such that an OpenSIMS server acts as a CA for its
    agents, while operating within a collaborative framework using a
    remote repository (see Ceteri project) as its CA.
    
    
    AgentSDK.  Camper created a secure, robust, extensible plugin
    architecture.  Each agent knows just enough to phone home to the
    OpenSIMS servlet.  From there the UML/XML-driven state machines in the
    correlation engine sequence the plugin configurations.  The security
    analysis for windows of vulnerability.
    
      * Windows support is (for now) based on (a wee bit o') Cygwin.
      * needs WEM-to-syslog gateway.
      * no countermeasures in this release (not yet).
    
    
    Autodiscovery.  This server-side process flow sequences the agent
    plugins.  The results are a network model represented as an XML
    document, periodically updated, which includes analysis based on
    ARP/NMAP/AMAP/PCAP/SMTP.
    
      * availability monitoring is not implemented in this release.
      * still migrating SNMP walk from the commercial version.
      * still migrating network traffic modeling (drives NBAD).
    
    
    Security Alert Definitions.  This is a general framework for
    representing alert definitions in XML, based on a wide variety of
    security devices and vendors.  Includes support for URL references,
    flexible means for type categorization, XPath/XQuery filtering based
    on platform/service versions (leverages autodiscovery to reduce false
    positives).
    
    Support is provided for the two reference implementation componenents:
    Snort and SpamAssassin -- for example, you can edit or update your
    Snort rules, and the Ant scripts will use a "layered" set of XML
    transforms to preserve edits and annotations in your alert defs
    document.
    
    
    Flash XMLSocket Listener.  Our client GUI in Java is a rare bird,
    since there are not many other examples readily available for
    integrating Tomcat and Flash XMLSocket.  This provides a lightweight
    protocol for managing multiple, extensible streams that combine both
    push and pull updates -- using the correlation engine as the data
    source.
    
      * still extending our data sources, to define streams in XQuery.
      * no support for multiple users or auth in this release.
    
    
    Flash GUI.  Lindsey has created one of the few (if not the only)
    existing open source real-time security visualization GUIs based on
    Flash.
    
    
    Notification.  Extensible support for several modes of notification:
    XMPP (Jabber), SMTP (email), text to telnet socket, HTTP/HTTPS "post",
    and others.
    
      * for low-rent proof-of-concept, try SOCAT w/ TelnetNotify.
      * still migrating over syslog support.
      * RSS support based on Informa is being developed.
    
    
    Geolocation.  The servlet provides "pluggable" support for adapting to
    a variety of geolocation data sources.  The popular "GeoIP" database
    from MaxMind is used for our reference implementation.
    
    
    Persistence Layer.  OpenSIMS is database "vendor-agnostic", since
    object-relational mapping (ORM) support allows it to switch between
    RDBMS vendors, embedded SQL, or memory-only.  We have integrated the
    use of the Torque open source ORM package for cross-platform database
    generation, for that purpose.
    
      * Torque for PostgreSQL, MySQL, or Oracle - so far
      * Cloudscape, MSSQL, Sybase, and some embedded DBs come next.
    
    Note that OpenSIMS also makes ample use of JDOM throughout the code
    for XML management of DOM, XPath, XSL, etc.  Many thanks to Jason
    Hunter, Brent MacLaughlin, et al., for their fine work.  We may also
    integrate some aspects of the XMLBeans project at Apache.
    
    
    UML Activity Diagram Modeling.  For a moment, forget what you know
    about CASE tools (Eclipse, Poseidon, whatever your favorite IDE may be
    this week, etc.) and forget about using UML to generate code, or XMI.
    Imagine having an open source implementation for a lightweight XML
    representation of UML Activity Diagrams which specify finite state
    machines in Java/XML, while taking a few cues from Roy Fielding's REST
    architectural design philosophy.
    
      * needs to render the UML diagrams.
      * does not yet handle "fork"/"join".
      * subscriber UML has been defined but not implemented.
    
    
    Reporting.  The webapp servlet generates XML from the combined data
    sources (in-memory, database, other XML files), which gets transformed
    as HTML (PDF?, Excel?) to render specific reports.  There is enough
    included in this release to give a flavor for what is intended, but
    the reporting can go *much* much further, and might be good to
    refactor as something based on Velocity.
    
      * needs to render to PDF and XLS.
    
    
    Autoconfig.  Portions are included in this release, such as the
    network interface discovery/selection and SSL key manager, as well as
    what is included in the AgentSDK.  Lindsey has developed a
    dependencies checking tool in "tools/depend" which runs preflight
    tests and report about what you may need to install or upgrade.
    
      * adding more cross-platform detection.
      * auto-download for dependencies.
    
    Note that the "build/*.properties" files are intended to be used only
    during initial install and then again during compiles and builds.  In
    turn, each of those tasks will generate and install their own XML
    files which provide the "runtime" config that you need to use, edit,
    and maintain.  See the FILE LAYOUT below for more detail.
    
    
    ######################################################################
    
        6. FLASH GUI NOTES
    
    Using the Flash Player version 7.0 r18+ should work for viewing the
    GUI.  That player is available for *BSD, Linux, Mac, and Windows.
    
    For compilation, Macromedia Flash MX 2004 version 7+ is required.  The
    easiest way is to open the project file:
    
      opensims/flash/openSIMS.flp
    
    ... then publish the "*.swf" files from there.
    
    The "*.fla" files all contain an instance of a shared movieclip in
    "component_lib.swf" so that they all share the same library of
    components and fonts.  Each of the "*.fla" files includes its
    necessary "*.as" files, which is where the source code for the GUI
    actually lives.
    
    The "*.xml" files set parameters, menu items, etc., so check there to
    customize the display.
    
    To display an RSS feed accessed from a remote site, they must publish
    a "crossdomain.xml" policy file at the root level of their web site:
    
      <?xml version="1.0"?>
      <!-- http://www.foo.com/crossdomain.xml -->
      <cross-domain-policy>
        <allow-access-from domain="*" />
      </cross-domain-policy>
    
    Also see the examples at:
    
      http://opensims.org/crossdomain.xml
      http://opensims.org/feed.xml
    
    Otherwise the security sandbox for Flash will block access.
    
    
    ######################################################################
    
        7. PostgreSQL NOTES
    
    Although PostgreSQL's default configuration is sized to work on the smallest machines
    possible, there are still some issues that can sneak up when trying to install it.
    
    - Shared Memory and Semaphores
    PostgreSQL depends on shared memory and semaphores for it's inter-process
    communication. Many OS's do not provide enough of either by default. If this
    happens, you will get either IpcMemoryCreate errors or IpcSemaphoreCreate
    errors when trying to start PostgreSQL.
    http://www.postgresql.org/docs/7.4/static/kernel-resources.html has information
    on configuring shared memory and semaphores.
    
    - TCP listener By default, PostgreSQL doesn't start a TPC listener, which JDBC
    requires. This is changed by adding the following to postgresql.conf:
    
        tcpip_socket = true
    
    postgresql.conf is normally located in the PostgreSQL data directory, which is
    usually /usr/local/postgresql/data, ~postgresql/data, or ~pgsql/data. The OS
    specific README in the docs directory will give you the default data directory
    location, when available.
    
    Tuning:
    There are some parameters that should be modified to improve the performance of
    PostgreSQL.
    
    - Hash joins in version 7 are not very performant. It is recomended you disable them by setting 
    
        enable_hash_join = false
    
    - effective_cache_size effective_cache_size is a parameter in postgresql.conf
    that tells PostgreSQL how much memory the OS will use for disk caching.
    Setting this can have a large impact on performance. It is expressed in 8K
    pages, and a good rule of thumb is to set it to 80% of the amount of memory the
    machine has. This equates to:
    
        effective_cache_size = (memory in MB) / 10
    
    There are other tuning parameters that may be worth tweaking as well (notably
    sort_mem), but they are beyond the scope of this document. More information may
    be found at http://www.varlena.com/varlena/GeneralBits/Tidbits/perf.html#conf.
    Note that OpenSIMS will VACUUM ANALYZE the database every time it deletes old
    alert information. (TODO: mention appropriate config setting for OpenSIMS)
    
    ######################################################################
    
        8. PROJECT CONTRIBUTORS
    
    Genaro BENITEZ
    Steve BEVILACQUA
    Matt BRADBURY
    Dan CAMPER
    David CHANG
    Mike W. ERWIN
    Erwin GOSEL
    William HURLEY
    Frank MILANO
    Jim NASBY
    Erin NATHAN
    Paco NATHAN
    Jamie PUGH
    Suresh RAMACHANDRAN
    Haroon RASHID
    Lindsey SIMON
    Linas VARDYS
    Rick WATERS
    Paul WOLFE
    Mark ZAVALA
    
    OpenSIMS logo design by Jason STOUT <stoutillustration@earthlink.net>
    
    ######################################################################
