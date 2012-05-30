#!/usr/bin/env bash

writeFiles()
{
# Configfile
cat > Configfile <<_EOF_
## The path to the android SDK
SDK = $SDK_DIR

## Jarsigner keystore file
KEYSTORE_FILE = $KEYSTORE_FILE

## Jarsigner keystore password
KEYSTORE_PASS = $KEYSTORE_PASS

## The alias for the given keystore
KEYSTORE_ALIAS = $KEYSTORE_ALIAS

## An ssh server location to upload a build to
REMOTE_SERVER = $REMOTE_SERVER
_EOF_

# local.properties
cat > local.properties <<_EOF_
# This file is automatically generated by Android Tools.
# Do not modify this file -- YOUR CHANGES WILL BE ERASED!
#
# This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.

# location of the SDK. This is only used by Ant
# For customization when using a Version Control System, please read the
# header note.
sdk.dir=$SDK_DIR_ABS
_EOF_
}

cat <<_EOF_
GenesisChess for Android configuration setup

The following external libraries are required:
1) Android 2.1+ SDK (API 7)
2) AdMob Ads SDK
3) Android Support Package v4
4) httpmime.jar (from Apache HttpComponents)
_EOF_

echo
read -ei "yes" -p "Do you meet the requirements (yes/no)? " DO_CONFIG

if [[ $DO_CONFIG != "yes" ]]; then
	echo "Exiting"
	exit
fi

echo
read -ep "Android SDK dir: " SDK_DIR
read -ep "Jarsigner keystore file: " KEYSTORE_FILE
read -p  "Jarsigner keystore password: " KEYSTORE_PASS
read -p  "Jarsigner keystore alias: " KEYSTORE_ALIAS
read -p  "SCP server location (optional): " REMOTE_SERVER

# make relative path absolute
til="~"
home=$(cd ~; pwd)
notil=${SDK_DIR/$til/$home}
SDK_DIR_ABS=$(readlink -f $notil)

echo
read -ei "yes" -p "Do you want to overwrite: Configfile and local.properties (yes/no)? " WRITE_CONFIG

if [[ $WRITE_CONFIG != "yes" ]]; then
	echo "Not writing files"
	exit
fi

echo "Writting config files"
writeFiles
