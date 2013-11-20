#!/usr/bin/perl

# $CVSId: parse_snortrules.pl,v 1.8 2006/10/01 23:56:07 mikee Exp $
# $Id$
# OpenSIMS - Snort rule parsing
# Mike W. ERWIN <mikee@symbiot.com>
# Paco NATHAN <paco@symbiot.com>
# Jim NASBY <jnasby@symbiot.com>

$, = "\t";    # set output field separator
$\ = "\n";    # set output record separator

use strict;
use HTML::Entities;
use Getopt::Std;
use vars qw/ %opt /;

## parse command line options

my $opt_string = 'hd:';

getopts("$opt_string", \%opt)
    or usage(1);

&usage(0) if $opt{h};
&usage(1) if ($#ARGV != 2);

if (defined($opt{d})) {
    print STDERR "debugging mode is ON, set to level " . $opt{d};
}
else {
    $opt{d} = 0;
}

my $arg_path_conf = $ARGV[0];
$arg_path_conf =~ s/^(\S+)\/$/$1/;

my $arg_path_rules = $ARGV[1];
$arg_path_rules =~ s/^(\S+)\/$/$1/;

my $arg_guess = $ARGV[2];


######################################################################
## main entry point

my %snort_ref;
my %snort_class;

&parse_snort_references("$arg_path_conf/reference.config");
&parse_snort_classes("$arg_path_conf/classification.config");

#???? move to XQuery
my %snort_types;
&parse_snort_types($arg_guess);

print STDOUT '<?xml version="1.0" encoding="UTF-8"?>
<ALERT_DEFS
 tick="' . time() . '000"
>';

&parse_snort_gen_msg("$arg_path_conf/gen-msg.map");
&parse_snort_rules("$arg_path_rules");

# Add Symbiot's special rules
&create_special_symbiot_rules();

print STDOUT '</ALERT_DEFS>';

exit 0;


######################################################################
## message about this program and how to use it

sub
    usage
{
    my $errno = shift;

    print STDERR << "EOF";

This program parses the rules for an installed Snort, generating
output as an XML file.

usage: $0 [ -h | -d N ] path_conf path_rules guess

  -h	: this (help) message
  -d N 	: set debugging level to N (default 0)

  path_conf	Path to the Snort config directory (probably "/etc/snort")
  path_rules	Path to the Snort rules directory (probably "/etc/snort/rules")
  guess		File path to alert type guesses

example: $0 -d 2 /etc/snort /etc/snort/rules /tmp/guess
EOF

    exit $errno;
}


######################################################################

sub
    parse_snort_references
{
    my $file = shift;

    print STDOUT "Parsing snort references $file" if ($opt{d} > 4);

    open(CFG, "< $file");

    while (<CFG>) {
	chomp;

	s/^\s+//;
	s/\s+$//;

	if (/^config reference:\s+(\w+)\s+(\S.*)$/) {
	    my $reference = $1;
	    $reference =~ y/A-Z/a-z/;
	    $reference =~ s/\s+$//;

	    my $url = $2;
	    $url =~ s/\s+$//;

	    # build the reference/url mapping

	    $snort_ref{$reference} = $url;
	}
    }

    close(CFG);
}


######################################################################

sub
    parse_snort_classes
{
    my $file = shift;
    print STDOUT "Parsing snort classes $file" if ($opt{d} > 4);

    # config classification: rpc-portmap-decode,Decode of an RPC Query,3

    open(CFG, "< $file");

    while (<CFG>) {
	chomp;
	s/^\s+//;
	s/\s+$//;

	next if (/^\#/);
	next if (/^$/);

	if (/^config classification\:\s+(.*)$/) {
	    my ($abbrev, $class, $priority) = split(/,/, $1);

	    $abbrev =~ y/A-Z/a-z/;
	    $class =~ y/A-Z/a-z/;

	    $snort_class{$abbrev} = $class;

	    print STDOUT "snort_class", $abbrev, $class, $priority if ($opt{d} > 1);
	}
    }

    close(CFG);
}


######################################################################

sub
    parse_snort_types
{
    my $file = shift;
    print STDOUT "Parsing snort types $file" if ($opt{d} > 4);

    # Attempted Information Leak,probe

    open(CFG, "< $file");

    while (<CFG>) {
	chomp;
	s/^\s+//;
	s/\s+$//;

	my ($type, $field, $class) = split(/,/);

	$class =~ y/A-Z/a-z/;
	$type =~ y/A-Z/a-z/;

	$snort_types{$class} = $type;

	print STDOUT "snort_type", $type, $field, $class if ($opt{d} > 1);
    }

    close(CFG);
}


######################################################################

sub
    guess_type
{
    my $file = shift;
    my $unique_id = shift;
    my $class = shift;
    my $description = shift;

    my $type;

    if (($description =~ /virus/i) || 
	($description =~ /trojan/i) ||
	($description =~ /worm/i) || 
	($description =~ /codered/i)
	) {
	return "malware";
    }
    else {
	foreach my $key (keys(%snort_types)) {
	    if (index($class, $key) >= 0) {
		return $snort_types{$key};
	    }
	}
    }

    print STDOUT "<!-- NEEDS_TYPE", $file, $unique_id, $description, "-->";

    return "misc";
}


######################################################################

sub
    parse_snort_gen_msg
{
    my $file = shift;
    print STDOUT "Parsing snort generator messages $file" if ($opt{d} > 4);
    open(MAP, "< $file");

    while (<MAP>) {
	chomp;
	s/^\s+//;
	s/\s+$//;

	next if (/^\#/);
	next if (/^$/);

	print STDOUT "gen_msg", $_ if ($opt{d} > 0);

	# Format: generatorid || alertid || MSG

	my ($generator, $alert_id, $description) = split(/ \|\| /);

	my $unique_id = "snort:$generator:$alert_id";
	$unique_id =~ s/\s+$//;

	my $class = $description;
	$class =~ y/A-Z/a-z/;

	$description =~ y/\'\%\`/_/;
	$description =~ s/\"//g;

	my $type = guess_type($file, $unique_id, $class, $description);

	my $def_xml = '<ALERT_DEF
 unique_id="' . $unique_id . '"
 file="' . $file . '"
 type="' . $type . '"
 msg="' . HTML::Entities::encode($description) . '"
/>';

	print STDOUT $def_xml;
    }
}


######################################################################

sub
    parse_snort_rules
{
    my $dir = shift;
    print STDOUT "Parsing snort rules from $dir" if ($opt{d} > 4);

    # scan the target directory

    opendir(DIR, $dir)
        || exit -1;

    foreach (readdir(DIR)) {
        if (/^(\S+\.rules)$/) {
	    my $file = $1;

	    print STDOUT $dir, $file if ($opt{d} > 4);

	    open(FILE, "< $dir/$file");

	    while (<FILE>) {
		chomp;
		s/^\s+//;
		s/\s+$//;

		next if (/^\#/);
		next if (/^$/);

		parse_snort_rule($dir, $file, $_);
		print STDOUT "---------" if ($opt{d} > 4);
	    }

	    close(FILE);
	}
    }

    closedir(DIR);
}


######################################################################

sub
    parse_snort_rule
{
    my $dir = shift;
    my $file = shift;
    my $rule = shift;

    print STDOUT $file, $rule if ($opt{d} > 0);

    my $unique_id = "";
    my $abbrev = "";
    my $description = "";
    my @refs = ();

    # (msg:"RPC portmap request ypserv"; content:"|01 86 A4 00 00|";offset:40;depth:8; reference:arachnids,12; classtype:rpc-portmap-decode; flow:to_server,established; sid:1276;  rev:4;)

    if ($rule =~ /^(.*)\s\((msg.*)\s*\)$/) {

	# collect the fields specially, given Snort's capricious
	# formatting rules...

	my $rule_stuff = $1;
	my $last_ch = '';
	my $field = '';
	my @fields = ();

	foreach my $this_ch (split(//, $2)) {
	    if (($this_ch eq ";") && ($last_ch ne "\\")) {
		$field =~ s/^\s+//;
		$field =~ s/\s+$//;
		$field =~ s/\\;/;/g;

		push(@fields, $field);
		$field = '';
	    }
	    else {
		$field .= $this_ch;
	    }

	    $last_ch = $this_ch;
	}

	$field =~ s/^\s+//;
	$field =~ s/\s+$//;
	$field =~ s/\\;/;/g;

	push(@fields, $field);

	# now, parse the fields

	foreach (@fields) {
	    if (/^\s*(\w+)\:\s*(.*)$/) {
		my $tag = $1;
		my $value = $2;

		$value =~ s/^(.*)\;$/$1/;
		$value =~ s/\s+$//;

		if ($tag eq "msg") {
		    $description = $value;
		    $description =~ y/\'\%\`/_/;
		    $description =~ s/\"//g;

		    print STDOUT $tag, $value if ($opt{d} > 4);
		}
		elsif ($tag eq "reference") {
		    push(@refs, $value);

		    print STDOUT $tag, $value if ($opt{d} > 4);
		}
		elsif ($tag eq "sid") {
		    $unique_id = "snort:1:$value";

		    print STDOUT $tag, $value if ($opt{d} > 4);
		}
		elsif ($tag eq "classtype") {
		    $abbrev = $value;

		    print STDOUT $tag, $value if ($opt{d} > 4);
		}
#		elsif ($tag eq "rev") {
#		    $unique_id .= ":$value";
#
#		    print STDOUT $tag, $value if ($opt{d} > 4);
#		}
	    }
	}

	my $class = $snort_class{$abbrev};
	my $type = guess_type("$dir/$file", $unique_id, $class, "$abbrev: $description");

	print STDOUT $abbrev if ($opt{d} > 2);
	print STDOUT $class if ($opt{d} > 2);
	print STDOUT $type if ($opt{d} > 2);

	if (length($description) < 1) {
	    print STDOUT "<!-- NEEDS_DESCRIPTION", $file, $unique_id, " -->";
	    $description = "this page left intentionally blank";
	}

	print STDOUT $file, $unique_id, $type, $description, @refs if ($opt{d} > 3);

	my $def_xml = '<ALERT_DEF
 unique_id="' . $unique_id . '"
 enabled="' . &is_rule_enabled($unique_id) . '"
 file="' . $dir . "/" . $file . '"
 class="' . $class . '"
 rule="' . HTML::Entities::encode($rule_stuff) . '"
 type="' . $type . '"
 msg="' . HTML::Entities::encode($description) . '"
>';

	print STDOUT $def_xml;

	# iterate over each reference

	foreach (@refs) {
	    my ($reference, $path) = split(/,/);
	    $reference =~ y/A-Z/a-z/;
	    $reference =~ s/\s+$//;

	    $path =~ s/^\s+//;
	    $path =~ s/\s+$//;

	    if ($snort_ref{$reference} ne "") {
		my $ref_xml = '<REF
 ref="' . $reference . '"
 url="' . HTML::Entities::encode($snort_ref{$reference}) . '"
 path="' . HTML::Entities::encode($path) . '"
/>';
		print STDOUT $ref_xml;
	    }
	}

	print STDOUT '</ALERT_DEF>';
    }
}


######################################################################

sub
    create_special_symbiot_rules
{
	# Traffic blocked by PIX firewall
	print STDOUT '<ALERT_DEF unique_id="pix:1:1" file="" type="bad_traffic" msg="Traffic stopped by PIX firewall rule"/>';
}


######################################################################

sub
    is_rule_enabled
{
	my $sid = shift;
	my $commented_out = "";
#	$sid =~ s/snort\:1/sid/;
	$sid .= "\$";
	
	if ( -e "/var/lib/opensims/removed.sids" ) {
		$commented_out = `/bin/grep $sid /var/lib/opensims/removed.sids`;
	}
	
	if ($commented_out ne "") {
		return ("false");
	}
	return("true");
}

######################################################################

