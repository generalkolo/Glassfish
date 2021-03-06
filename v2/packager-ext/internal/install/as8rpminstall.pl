#!/usr/bin/perl

#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#


#================================================================
#								=
# This script will install/uninstall the Solaris	 	=
# packages of Sun ONE Application Server. The packages names   	=
# are defined in bundled_rpm.txt. Configuration params should 	=
# be put in conf.txt.						=
#================================================================

use Getopt::Long;
use English;
use FileHandle;
require "ctime.pl";

&GetOptions("install!", "uninstall!", "help!");
$usage = "
Usage :
	$0 [options]
	Command-line options and arguments:
	-install | -uninstall | -help

	-help       	Print this usage message.
	-install 	Install the linux rpm build specified in conf.txt.
	-uninstall 	Uninstall the linux rpm app server.

";
die "\nAction is not defined \n $usage" unless defined($opt_install || $opt_uninstall || $opt_help);
if ($opt_help)
{
                print "$usage";
                exit 1;
}

# Definitions
use Env qw(HOST);
$rm = "rm -rf";
$s_HOME = `pwd`;
chomp $s_HOME;
$pkgDir = "$s_HOME/pkg";
$pkgaddLog = "/tmp/pkgaddLog.$$";
$asadmin = "/opt/sun/appserver/bin/asadmin";
$confRoot = "/opt/sun/appserver/config";
$sampleDir = "/opt/sun/appserver/samples";
$installDir = "/opt/sun/appserver";
$javaHome = "/usr/java/j2sdk1.4.2_05";
$basedir = "/opt/sun";

# Reading info.
sub read_inf($) {
    my ($file) = @_;
    open F, "< $file" or die "$!, reading from $file";
    # m/^\s*\[[^\[\]]+\]\s*$/;
    my @vals = map { m/^\s*([^= \t]+)\s*[= \t]\s*(.+)/ ? (lc $1,$2) : () } <F>;
    close F;
    return @vals;
}


$inf_file = "conf.linux.txt";
my %NVP = read_inf $inf_file;
$domainRoot = $NVP{domainroot};
$adminUser = $NVP{adminuser};
$adminPort = $NVP{adminport};
$adminPasswd = $NVP{adminpasswd};
$instanceport = $NVP{serverport};

# main.
print "\n";
print "__________________________________________________________\n";
print "Start processing at    ", &ctime(time), "\n";

chdir "$s_HOME";
$osver = `uname -r`;
chomp $osver;
	open(bp, "bundled_rpm.txt");

@runList = <bp>;
print "@runList\n";
@revList = reverse(@runList);

if ($opt_uninstall)
{
  $retCode = system("$asadmin stop-domain");
  
  for (my($i) = 0; $i <= $#revList; $i++)
  {
        chomp($revList[$i]);
        print "removing rpm $revList[$i] \n";
        $retCode = system("rpm -e $revList[$i]");
        print "removed rpm $revList[$i] \n";
	
  }
  $retCode = system("rpm -e j2sdk-1.4.2_05-fcs");
  $retCode = system("rpm -e sun-javahelp-2.0-fcs");
  $retCode = system("$rm $confRoot");
  $retCode = system("$rm $domainRoot");
  $retCode = system("$rm $installDir");
}

if ($opt_install)
{
  chdir "$pkgDir";
  $retCode = system("rpm -i j2sdk-1_4_2_05-linux-i586.rpm >> $pkgaddLog");
  if ($retCode ne "0")
  { 
  	print "Failed to add package j2sdk-1_4_2_05-linux.i586.rpm\n";
	  exit 1;
  }
  $retCode = system("rpm -i sun-javahelp-2_0-linux-i586.rpm >> $pkgaddLog");
  if ($retCode ne "0")
  { 
  	print "Failed to add package sun-javahelp-2_0-linux-i586.rpm\n";
	  exit 1;
  }
  for (my($i) = 0; $i <= $#runList; $i++)
  {
        chomp($runList[$i]);
	$retCode = system("rpm -i $runList[$i].i386.rpm >> $pkgaddLog");
	if ($retCode ne "0")
	{ 
	    print "Failed to add package $runList[$i].i386.rpm\n";
	    exit 1;
	}
	print "installed rpm $runList[$i].i386.rpm\n";
	
  }
  print "installed rpm $runList[$i].i386.rpm\n";
  $retCode = system("unzip ant.zip -d $installDir/lib/ant");
  if ($retCode ne "0")
  {
	print "Failed to unzip ant.zip.\n";
	exit 1;
  }
  $retCode = system("unzip SUNWjhrt.zip -d $installDir/");
  if ($retCode ne "0")
  {
	print "Failed to unzip SUNWjhrt.zip.\n";
	exit 1;
  }
  $retCode = system("mkdir -p $installDir/bin");
  $retCode = system("mkdir -p $installDir/config");
  $retCode = system("./appservenv $basedir");
  if ($retCode ne "0")
  {
	print "Failed to do token replacement.\n";
	exit 1;
  }
  $retCode = system("./asenv $basedir $installDir $javaHome");
  if ($retCode ne "0")
  {
	print "Failed to do token replacement.\n";
	exit 1;
  }



  rename("$sampleDir/common.properties", "$sampleDir/common.properties.orig");
  open(F1, "<$sampleDir/common.properties.orig");
  open(F2, ">$sampleDir/common.properties") || die ("common.properties cannot be opened\n");
  while (<F1>)
  {
     if (m/^com.sun.aas.pointbaseRoot=/) {s/=.*$/=$installDir\/pointbase/;}
     if (m/^com.sun.aas.webServicesLib=/) {s/=.*$/=$installDir\/lib/;}
     if (m/^com.sun.aas.imqLib=/) {s/=.*$/=\/opt\/sun\/mq\/lib/;}
     if (m/^com.sun.aas.installRoot=/) {s/=.*$/=$installDir/;}
     if (m/^com.sun.aas.javaRoot=/) {s/=.*$/=$javaHome/;}
	 if (m/^#admin.password=/) {s/#admin.password=.*$/admin.password=$adminPasswd/;}
     if (m/^admin.host=/) {s/=.*$/=localhost/;}
     if (m/^appserver.instance.port=/) {s/=.*$/=$instanceport/;}
     if (m/^admin.user=/) {s/=.*$/=$adminUser/;}
     if (m/^admin.port=/) {s/=.*$/=$adminPort/;}
	 if (m/^appserver.instance=/) {s/=.*$/=server/;}
     if (m/^pointbase.port=/) {s/=.*$/=9092/;}
     print F2 ("$_");
  }
  close(F1);
  close(F2);

  $SUN_acc = "$domainRoot/domain1/config/sun-acc.xml";
  rename("$confRoot/asenv.conf", "$confRoot/asenv.conf.orig");
  open(F3, "<$confRoot/asenv.conf.orig");
  open(F4, ">$confRoot/asenv.conf") || die ("asenv.conf cannot be opened\n");
  while (<F3>)
  {
     if (m/^AS_ACC_CONFIG=/) {s/=.*$/="$SUN_acc"/;}
     if (m/^AS_DEF_DOMAINS_PATH=/) {s/=.*$/="$domainRoot"/;}
     if (m/^AS_IMQ_LIB=/) {s/=.*$/="\/opt\/sun\/mq\/lib"/;}
     if (m/^AS_IMQ_BIN=/) {s/=.*$/="\/opt\/sun\/mq\/bin"/;}
     if (m/^AS_ANT=/) {s/=.*$/="$installDir\/lib\/ant"/;}
     if (m/^AS_ANT_LIB=/) {s/=.*$/="$installDir\/lib\/ant\/lib"/;}
     if (m/^AS_JHELP=/) {s/=.*$/="$installDir\/lib"/;}
     print F4 ("$_");
  }
  close(F3);
  close(F4);
  $retCode = system("$asadmin create-domain --domaindir $domainRoot --adminport $adminPort --adminuser $adminUser --adminpassword $adminPasswd --instanceport $instanceport domain1");
  if ($retCode ne "0")
  {
	print "Failed to create domain.\n";
	exit 1;
  }
  $retCode = system("$asadmin start-domain --user $adminUser --password $adminPasswd");
  if ($retCode ne "0")
  {
	print "Failed to start the domain1.\n";
	exit 1;
  }

  $docRoot = "$domainRoot/domain1/docroot";
  $retCode = system("cd $sampleDir; find . -name docs > docslist;");
  $retCode = system("./xcp $docRoot/samples $sampleDir/docslist $sampleDir");
  $retCode = system("cp -r $sampleDir/index.html $docRoot/samples/");
  $retCode = system("cp -r $sampleDir/indexSamplesDomain.html $docRoot/samples/");
  $retCode = system("rm $sampleDir/docslist");
  $retCode = system("cp $s_HOME/3RD-PARTY-LICENSE.txt $installDir/.");
  $retCode = system("cp $s_HOME/LICENSE.txt $installDir/.");
}

print "__________________________________________________________\n";
print "Finished processing at ", &ctime(time), "\n";  


