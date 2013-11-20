#!/usr/bin/perl

# $CVSId: dependencies.pl,v 1.3 2004/08/16 02:28:01 lsimon Exp $
# $Id$
# OpenSIMS - dependencies checking script
# Lindsey SIMON <lsimon@symbiot.com>

$, = "\t";    # set output field separator
$\ = "\n";    # set output record separator

#use strict;

$file = "dependencies.xml";

print;
print "OpenSIMS Dependencies checking script.";
print "######################################";

#$parser->parsefile($file);

open (DEP, $file);

while (<DEP>) {
	$line = $_;	
	if (!($line =~ m/DEPENDENCIES/ || $line =~ m/xml/)) {
		$line =~ s/[\/]//;
		$line =~ s/[<]//;
		$line =~ s/[>]//;
		$line =~ s/\t//;
		chomp($line);

		#($crap,$dependency,$version,$test) = split(/\s/, $line);

	
		if ($line =~ m/^.+\'(.*)\'.+\'(.*)\'.+\'(.*)\'.+/) {
			$dependency = $1;
			$version = $2;
			$test = $3;


	
			print "$dependency >= $version";	
			print "test: $test";
			system($test);
		
			print "------------------------------------------------";

		}
		
	}

}

