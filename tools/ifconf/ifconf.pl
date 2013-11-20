#!/usr/bin/perl

# $CVSId: ifconf.pl,v 1.7 2004/10/11 06:18:34 paco Exp $
# $Id$
# OpenSIMS - interface configuration (ifconfig)
# Paco NATHAN <paco@symbiot.com>

$, = "\t";    # set output field separator
$\ = "\n";    # set output record separator

use strict;
use Getopt::Std;
use vars qw/ %opt /;

## parse command line options

my $opt_string = 'hd:';

getopts("$opt_string", \%opt)
    or usage(1);

&usage(0) if $opt{h};
&usage(1) if ($#ARGV != 3);

if (defined($opt{d})) {
    print STDERR "debugging mode is ON, set to level " . $opt{d};
}
else {
    $opt{d} = 0;
}

my $arg_ifconfig_exec = $ARGV[0];
my $arg_route_exec = $ARGV[1];
my $arg_os_name = $ARGV[2];
my $arg_fqdn = $ARGV[3];


######################################################################
## main entry point

my %gateway_list;

open(ROUTE, "$arg_route_exec -n |") ||
    die "Cannot execute $arg_route_exec - $!";

# 0.0.0.0         67.107.81.193   0.0.0.0         UG    1      0        0 eth0

while (<ROUTE>) {
    chomp;
    print if ($opt{d} > 1);

    if (/^0\.0\.0\.0\s+([\d\.]+)\s+.*\s(\S+)$/) {
	my $gateway = $1;
	my $intf = $2;

	print $gateway, $intf if ($opt{d} > 1);

	$gateway_list{$intf} = $gateway;
    }
}

close(ROUTE);


open(IFCONFIG, "$arg_ifconfig_exec |") ||
    die "Cannot execute $arg_ifconfig_exec - $!";

my $start = 1;
my ($intf, $mac_addr, $ip_addr, $broadcast, $mask);
my @valid_input;
my %input_list;

print '<?xml version="1.0" encoding="UTF-8"?>
<HOST
 name="' . $arg_fqdn . '"
>
';

while (<IFCONFIG>) {
    chomp;
    print if ($opt{d} > 1);

    # parse each line, based on the "os.name"
    # for example values, see:
    # http://lopica.sourceforge.net/os.html

    if ($arg_os_name =~ /^linux$/i) {
	&parse_line_linux($_);
    }
    elsif (($arg_os_name =~ /^freebsd$/i) ||
	   ($arg_os_name =~ /^mac os x$/i)
	   ) {
	&parse_line_bsd($_);
    }

    # ready for the next interface?

    s/^\s+$//;

    if ($_ eq "") {
	$start = 1;

	if ($intf ne "") {
	    #my $network = $broadcast & $mask;
	    my $network = &quad2ip($ip_addr) & $mask;

	    my $range = $broadcast - $network + 1;

	    if ($range > 16777216) {
		# somethin fishy 'bout that broadcast address -
		# can you say "Time Warner"?

		$range = &quad2ip("255.255.255.255") - $mask + 1;

		if ($range > 16777216) {
		    $range = 16777216;
		}
	    }

	    my $split = 32 - (log($range) / log(2));
	    my $cidr = &ip2quad($network) . "/" . $split;

	    $input_list{$intf} = $cidr;

	    if ($opt{d} > 0) {
		print &ip2quad($broadcast), &ip2quad($mask);
		print $range, $split, $broadcast, $mask;
	    }

	    my $gateway = $gateway_list{$intf};

	    if (!defined($gateway)) {
		my @list = values(%gateway_list);
		$gateway = $list[0];
	    }

	    print '
<INTERFACE
 name="' . $intf . '"
 mac_addr="' . $mac_addr . '"
 ip_addr="' . $ip_addr . '"
 selected="false"
>
<NETWORK
 cidr="' . $cidr . '"
 broadcast="' . &ip2quad($broadcast) . '"
 netmask="' . &ip2quad($mask) . '"
 gateway="' . $gateway . '"
 description="' . $arg_fqdn . '"
 topology="internal"
/>
</INTERFACE>
';
	}
    }
}

print '
<SELECT
 validargs="' . join(",", @valid_input) . '"
>
Which of the following is the selected interface/network?
';

foreach $intf (@valid_input) {
    print "", $intf, $input_list{$intf};
}

print "</SELECT>";
print "</HOST>";

close(IFCONFIG);

exit 0;


######################################################################
## parse an output line from "ifconfig" on a Linux platform

sub
    parse_line_linux
{
    $_ = shift;

    # parse first line?

    if ($start == 1) {
	$intf = "";
	$mac_addr = "";

	if (/^([\w\d]+)\s+.*HWaddr\s+([\w\d\:]+)\s*$/) {
	    $intf = $1;
	    $mac_addr = $2;

	    $mac_addr =~ s/\://g;
	    $mac_addr =~ y/a-z/A-Z/;

	    push(@valid_input, $intf);
	}

	$start = 0;
    }
    elsif (/^\s+inet addr\:([\d\.]+)\s+Bcast\:([\d\.]+)\s+Mask\:([\d\.]+)\s*$/) {
	$ip_addr = $1;
	$broadcast = &quad2ip($2);
	$mask = &quad2ip($3);

	if ($opt{d} > 0) {
	    print "ip", $ip_addr, "broadcast", $broadcast, "mask", $mask;
	}
    }
}


######################################################################
## parse an output line from "ifconfig" on a BSD Unix platform

sub
    parse_line_bsd
{
    $_ = shift;

    # parse first line?

    if ($start == 1) {
	$intf = "";
	$mac_addr = "";

	if (/^([\w\d]+)\:\s+flags.*$/) {
	    # xl0:	flags=8843<UP,BROADCAST,RUNNING,SIMPLEX,MULTICAST> mtu 1500

	    $intf = $1;
	    push(@valid_input, $intf);
	}

	$start = 0;
    }
    elsif (/^\s+ether\s+([\w\d\:]+)\s*$/) {
	# ether 00:10:4b:72:66:6a

	$mac_addr = $1;

	$mac_addr =~ s/\://g;
	$mac_addr =~ y/a-z/A-Z/;
    }
    elsif (/^\s+inet\s+([\d\.]+)\s+netmask\s+(\w+)\s+broadcast\s+([\d\.]+)\s*$/) {
        # inet 172.16.1.8 netmask 0xffffff00 broadcast 172.16.1.255

	$ip_addr = $1;
	$mask = hex($2);
	$broadcast = &quad2ip($3);

	if ($opt{d} > 0) {
	    print "ip", $ip_addr, "broadcast", $broadcast, "mask", $mask;
	}
    }
}


######################################################################
## message about this program and how to use it

sub
    usage
{
    my $errno = shift;

    print STDERR << "EOF";

This script parses the output from "ifconfig" and "route" to setup the
interface/network configuration for managing OpenSIMS and AgentSDK.

usage: $0 [ -h | -d N ] ifconfig_exe route_exe os_name fqdn

  -h		this (help) message
  -d N		set debugging level to N (default 0)

  ifconfig_exe	path to "ifconfig" executable
  route_exe	path to "route" executable
  os_name	operating system (from Ant "os.name" property)
  fqdn		fully qualified domain name

example: $0 /sbin/ifconfig /sbin/route linux www1.example.com
EOF

    exit $errno;
}


######################################################################
## convert a numeric IP address to a dotted quad

sub
    ip2quad
{
    my $ip = shift;
    my @quad;

    $quad[3] = $ip % 256;
    $ip = int($ip / 256);

    $quad[2] = $ip % 256;
    $ip = int($ip / 256);

    $quad[1] = $ip % 256;
    $ip = int($ip / 256);

    $quad[0] = $ip;

    return join '.', @quad;
}


######################################################################
## convert the elements of a dotted quad to a numeric IP address

sub
    quad2ip
{
    my @quad = split(/\./, shift);

    my $ip = $quad[0];

    $ip <<= 8;
    $ip += $quad[1];

    $ip <<= 8;
    $ip += $quad[2];

    $ip <<= 8;
    $ip += $quad[3];

    return $ip;
}
 
######################################################################
