#!/usr/bin/perl -w

# This script is distributed along with the datasets for the
# shared translation task of the NAACL 2012 Workshop on Statistical Machine
# Translation.  We include it here for convenience.

use strict;

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

my ($language) = @ARGV;

while(<STDIN>) {
  s/\r//g;
  # remove extra spaces
  s/\(/ \(/g;
  s/\)/\) /g; s/ +/ /g;
  s/\) ([\.\!\:\?\;\,])/\)$1/g;
  s/\( /\(/g;
  s/ \)/\)/g;
  s/(\d) \%/$1\%/g;
  s/ :/:/g;
  s/ ;/;/g;
  # normalize unicode punctuation
  s/„/\"/g;
  s/“/\"/g;
  s/”/\"/g;
  s/–/-/g;
  s/—/ - /g; s/ +/ /g;
  s/´/\'/g;
  s/([a-z])‘([a-z])/$1\'$2/gi;
  s/([a-z])’([a-z])/$1\'$2/gi;
  s/‘/\"/g;
  s/‚/\"/g;
  s/’/\"/g;
  s/''/\"/g;
  s/´´/\"/g;
  s/…/.../g;
  # French quotes
  s/ « / \"/g;
  s/« /\"/g;
  s/«/\"/g;
  s/ » /\" /g;
  s/ »/\"/g;
  s/»/\"/g;
  # handle pseudo-spaces
  s/ \%/\%/g;
  s/nº /nº /g;
  s/ :/:/g;
  s/ ºC/ ºC/g;
  s/ cm/ cm/g;
  s/ \?/\?/g;
  s/ \!/\!/g;
  s/ ;/;/g;
  s/, /, /g; s/ +/ /g;

  # English "quotation," followed by comma, style
  if ($language eq "en") {
    s/\"([,\.]+)/$1\"/g;
  }
  # Czech is confused
  elsif ($language eq "cs" || $language eq "cz") {
  }
  # German/Spanish/French "quotation", followed by comma, style
  else {
    s/,\"/\",/g;  
    s/(\.+)\"(\s*[^<])/\"$1$2/g; # don't fix period at end of sentence
  }

  print STDERR $_ if /﻿/;

  if ($language eq "de" || $language eq "es" || $language eq "cz" || $language eq "cs" || $language eq "fr") {
    s/(\d) (\d)/$1,$2/g;
  }
  else {
    s/(\d) (\d)/$1.$2/g;
  }
  print $_;
}
