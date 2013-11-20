#!/usr/bin/perl

use XML::DOM;

# set some configs
$path = "/var/lib/opensims";
$nagios_path = "/etc/nagios/etc";

# find the config file
opendir(DIR, $path);
@files = grep { /^\w+\.model.xml$/ } readdir(DIR);
closedir(DIR);
$file = $files[0];

# parse it
my $parser = new XML::DOM::Parser;
my $doc = $parser->parsefile($path."/".$file);


# open some filehandles
open("HOSTS_CONFIG", ">$nagios_path/hosts.cfg") || die("Can't write config files in $nagios_path"); 
open("HOSTGROUPS_CONFIG", ">$nagios_path/hostgroups.cfg") || die("Can't write config files in $nagios_path");
open("SVC_CONFIG", ">$nagios_path/services.cfg") || die("Can't write config files in $nagios_path");


# setup the hosts
select("HOSTS_CONFIG");
print "# OPENSIMS hosts.cfg for nagios\n\n";
print "# Generic host definition template
define host{
        name                            generic-host    ; The name of this host template - referenced in other host definitions, used for template recursion/resolution
        notifications_enabled           1       ; Host notifications are enabled
        event_handler_enabled           0       ; Host event handler is disabled
        flap_detection_enabled          0       ; Flap detection is disabled
        process_perf_data               1       ; Process performance data
        retain_status_information       1       ; Retain status information across program restarts
        retain_nonstatus_information    1       ; Retain non-status information across program restarts

        register                        0       ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL HOST, JUST A TEMPLATE!
        }

";


# setup the services
select("SVC_CONFIG");
print "# OPENSIMS services.cfg for nagios\n\n";
print "# Generic service definition template
define service{
        ; The 'name' of this service template, referenced in other service definitions
        name                            generic-service
        active_checks_enabled           1       ; Active service checks are enabled
        passive_checks_enabled          1       ; Passive service checks are enabled/disabled
        parallelize_check               1       ; Active service checks should be parallelized
                                                ; (disabling this can lead to major performance problems)
        obsess_over_service             1       ; We should obsess over this service (if necessary)
        check_freshness                 0       ; Default is to NOT check service 'freshness'
        notifications_enabled           1       ; Service notifications are enabled
        event_handler_enabled           0       ; Service event handler is disabled
        flap_detection_enabled          0       ; Flap detection is disabled
        process_perf_data               1       ; Process performance data
        retain_status_information       1       ; Retain status information across program restarts
        retain_nonstatus_information    1       ; Retain non-status information across program restarts

        register                        0       ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL SERVICE, JUST A TEMPLATE!
        }

";



# hosts config
@hosts = ();

foreach my $host ($doc->getElementsByTagName('HOST')) {
	select("HOSTS_CONFIG");
	my $hostname = $host->getAttribute('ip_addr');
	push(@hosts, $hostname);
	print "define host{\n";
	print "\tuse\t\t\tgeneric-host\n";
	print "\thost_name\t\t\t$hostname\n";
	print "\talias\t\t\t$hostname\n";
	print "\taddress\t\t\t$hostname\n";
  	print "\tcheck_command\t\t\tcheck-host-alive\n";
	print "\tmax_check_attempts\t\t\t20\n";
	print "\tnotifications_enabled\t\t\t0\n";
	print "\tnotification_interval\t\t\t0\n";
	print "\t}\n\n";

	# services for this host
	# use a hash to ensure we don't do this twice
	%services;
	select("SVC_CONFIG");
	foreach my $service ($host->getElementsByTagName('SERVICE')) {
		$servicename = $service->getAttribute('name');
		$serviceport= $service->getAttribute('port');
		
		#select(STDOUT);
		select("SVC_CONFIG");
		
#		if (!$services{$servicename}) {
		if (1) {
		        #print STDOUT "ADDING $servicename\n";
			$services{$servicename} = 1;
			print "define service{\n";
			print "\tuse\t\t\tgeneric-service\n";
			print "\thost_name\t\t\t$hostname\n";
			print "\tservice_description\t\t\t${servicename}_${serviceport}\n";
			print "\tis_volatile\t\t\t0\n";
			print "\tcheck_period\t\t\t24x7\n";
			print "\tmax_check_attempts\t\t\t3\n";
			print "\tnormal_check_interval\t\t\t3\n";
			print "\tretry_check_interval\t\t\t1\n";
			#print "\tnotifications_enabled\t\t\t0\n";
			#print "\tcontact_groups\t\t\t\n";
			print "\tnotification_interval\t\t\t0\n";
			#print "\tnotification_period\t\t\t24x7\n";
			#print "\tnotification_options\t\t\tw,u,c,r\n";
			print "\tcheck_command\t\t\tcheck_tcp!$serviceport\n";
			print "\t}\n\n";
		}
		else {
		        #print STDOUT "NOT adding $servicename\n";
                }

	}

}

select("HOSTGROUPS_CONFIG");
print "define hostgroup{\n";
print "\thostgroup_name\t\t\tservers\n";
print "\talias\t\t\tservers\n";
print "\tcontact_groups\t\t\tadmins\n";
print "\tmembers\t\t\t".join(",",@hosts)."\n";
print "\t}\n\n";

# print out a message to the user
print STDOUT "\n\nConfiguration files written successfully!\n";
