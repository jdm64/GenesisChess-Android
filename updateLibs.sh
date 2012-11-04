#!/usr/bin/env bash

SDK=$1

# make sdk dir absolute
til="~"
home=$(cd ~; pwd)
notil=${SDK/$til/$home}
SDK_ABS=$(readlink -f $notil)

# create libs dir
mkdir -p libs

# remove old libs
rm -f libs/android-support*
rm -f libs/GoogleAdMobAdsSdk*
rm -f libs/httpmime*

# setup libs link files
ln -fs $SDK_ABS/extras/android/support/v4/android-support-v4.jar libs/
ln -fs $SDK_ABS/extras/google/admob_ads_sdk/GoogleAdMobAdsSdk* libs/

# download httpmime.jar
wget http://hc.apache.org/downloads.cgi
wget $(grep binary/httpcomponents-client downloads.cgi | head -n1 | cut -d'"' -f2)
tar -xf httpcomponents-client*bin.tar.gz
cp httpcomponents-client*/lib/httpmime*.jar libs/

# remove all downloaded files
rm -rf downloads.cgi httpcomponents-client*
