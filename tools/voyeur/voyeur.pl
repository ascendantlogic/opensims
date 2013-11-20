#!/usr/bin/perl

# $CVSId: voyeur.pl,v 1.3 2004/07/08 06:38:09 paco Exp $
# $Id$
# OpenSIMS - network traffic modeling (tcpdump)
# Paco NATHAN <paco@symbiot.com>

$, = "\t";    # set output field separator
$\ = "\n";    # set output record separator
$; = "\t";    # set key field separator

use strict;
use Getopt::Std;
use vars qw/ %opt /;

## parse command line options

my $opt_string = 'hd:c:xb';

getopts("$opt_string", \%opt)
    or usage(1);

&usage(0) if $opt{h};
&usage(1) if ($#ARGV != 1);

if (defined($opt{d})) {
    print STDERR "debugging mode is ON, set to level " . $opt{d};
}
else {
    $opt{d} = 0;
}

my $arg_exec = $ARGV[0];
my $arg_intf = $ARGV[1];

my $cidr_network = 0;
my $cidr_broadcast = 0;

my %host;
my %mac;

my $init_time = time();

if ($opt{c}) {
    $cidr_network = &cidr2network($opt{c});
    $cidr_broadcast = $cidr_network + &cidr2broadcast($opt{c});
}

if ($opt{x}) {
    $SIG{'INT'} = 'handle_sig_int';
}


######################################################################
## main entry point

open(TCPDUMP, $arg_exec . ' -i ' . $arg_intf . ' -l -e -n -v "(tcp or udp or icmp)" |') ||
    die "Cannot execute $arg_exec - $!";

while (<TCPDUMP>) {
    chomp;
    print if ($opt{d} > 0);

    # 00:48:29.469235 0:b0:d0:e9:4a:34 0:60:97:f:86:ca 0800 60: 172.16.1.8.80 > 57.125.72.186.48902: R [tcp sum ok] 0:0(0) win 0 (DF) [tos 0x10]  (ttl 64, id 22940, len 40)

    if (/^[\d\:\.]+\s+([a-f\d\:]+\s+)([a-f\d\:]+\s+)\d+\s+\d+\:\s+([\d\.]+)\s+\>\s+([\d\.]+)\:\s+(.*)\s+\(ttl\s+(\d+),\s+id\s+\d+,\s+len\s+(\d+)\)\s*$/i) {
	# save the address info

	my $src_mac = &fix_mac_addr($1);
	my $dst_mac = &fix_mac_addr($2);
	my $src_addr = $3;
	my $dst_addr = $4;
	my $src_port = "0";
	my $dst_port = "0";

	# parse the packet info

	my $txt = $5;
	my $ttl = $6;
	my $len = $7;

	# parse the protocol

	my $proto = "tcp";

	if ($txt =~ /^\icmp/) {
	    $proto = "icmp";
	}
	elsif ($txt =~ /^\[udp/) {
	    $proto = "udp";
	}

	# parse the address - in detail

	if ($proto ne "icmp") {
	    if ($src_addr =~ /^(\d+\.\d+\.\d+\.\d+)\.(\d+)$/) {
		$src_addr = $1;
		$src_port = $2;
	    }

	    if ($dst_addr =~ /^(\d+\.\d+\.\d+\.\d+)\.(\d+)$/) {
		$dst_addr = $1;
		$dst_port = $2;
	    }
	}

	$mac{$src_addr} = $src_mac;

	# CIDR filter

	if ($opt{c}) {
	    my $ip_addr = &quad2ip($src_addr);

	    print $cidr_network, $ip_addr, $cidr_broadcast if ($opt{d} > 1);

	    if (($ip_addr < $cidr_network) ||
		($ip_addr > $cidr_broadcast)
		) {
		next;
	    }
	}

	# output

	if ($opt{x}) {
	    # aggregate the output

	    my $key = $src_port . "/" . $proto;
	    $host{$src_addr, $key} += $len;

	    print $src_mac, $src_addr, $key, $len if ($opt{d} > 0);
	}
	else {
	    # output lines which pass the filter

	    print $src_mac, $src_addr, $src_port, $dst_mac, $dst_addr, $dst_port, $proto, $ttl, $len;
	}
    }
}

close(TCPDUMP);

exit 0;


######################################################################
## message about this program and how to use it

sub
    usage
{
    my $errno = shift;

    print STDERR << "EOF";

This script parses the output from "tcpdump" to model network traffic
on a given interface, for managing OpenSIMS and AgentSDK.

usage: $0 [ -h | -d N | -c <cidr> | -x | -b ] <exec> <intf>

  -h		this (help) message
  -d N		set debugging level to N (default 0)
  -c <cidr>	use the CIDR as a source address range filter
  -x		aggregate output into a summary XML format
  -b		"bogify" IPv4 into "unique" MAC address (pseudo hub)

  <exec>	path to "tcpdump" executable
  <intf>	interface name to listen in promiscious mode

example: $0 -c "192.168.1.0/24" /usr/sbin/tcpdump eth0
EOF

    exit $errno;
}


######################################################################
## message about this program and how to use it

sub
    handle_sig_int
{
    my $total_time = time() - $init_time;
    my $last_addr = "";

    print "
<NETWORK
 cidr='$opt{c}'
 sample_period='$total_time'
>";

    foreach my $key (sort keys(%host)) {
	my $sum_len = $host{$key};
	my $rate = sprintf("%0.3f", $sum_len / $total_time);

	my ($src_addr, $service) = split($;, $key);
	my ($src_port, $protocol) = split("/", $service);

	if ($last_addr ne $src_addr) {
	    my $mac_addr = ($opt{b}) ? sprintf("%012x", &quad2ip($src_addr)) : $mac{$src_addr};

	    print "</HOST>" if ($last_addr ne "");
	    print "
<HOST
 ip_addr='$src_addr'
 mac_addr='$mac_addr'
>";

	    $last_addr = $src_addr;
	}

	print "
<SERVICE
 protocol='$protocol'
 port='$src_port'
>
<TRAFFIC
 rate='$rate'
/>
</SERVICE>";
    }
    
    print "</HOST>" if ($last_addr ne "");
    print "</NETWORK>";
}


######################################################################
## fix the given MAC address, putting it into canonical form

sub
    fix_mac_addr
{
    my $wire = shift;
    my @list = split(/\:/, $wire);

    my $m_lo = hex($list[0]);

    $m_lo <<= 8;
    $m_lo += hex($list[1]);

    $m_lo <<= 8;
    $m_lo += hex($list[2]);

    my $m_hi = hex($list[3]);

    $m_hi <<= 8;
    $m_hi += hex($list[4]);

    $m_hi <<= 8;
    $m_hi += hex($list[5]);

    my $mac_addr = sprintf("%06X%06X", $m_lo, $m_hi);

    return $mac_addr;
}


######################################################################
## extract the network address from a CIDR

sub
    cidr2network
{
    my $ip = shift;
    my $one32 = 0b11111111111111111111111111111111;

    if ($ip eq "/0") {
        return 0;
    }

    $ip =~ /(.*)\/(\d+)/;

    my $addr = $1;
    my $bits = $2;

    my $n = $one32 << (32 - $bits);
    my @quad = split(/\./, $addr);

    my $m = $quad[0];

    $m <<= 8;
    $m += $quad[1];

    $m <<= 8;
    $m += $quad[2];

    $m <<= 8;
    $m += $quad[3];

    print $ip, $addr, $bits, $n, $m, ($m & $n), @quad if ($opt{d});

    return $m & $n;
}


######################################################################
## extract the broadcast address from a CIDR

sub
    cidr2broadcast
{
    my $ip = shift;

    if ($ip eq "/0") {
        return 0;
    }

    $ip =~ /(.*)\/(\d+)/;

    my $bits = $2;
    my $n = (2 << (31 - $bits)) - 1;

    return $n;
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
