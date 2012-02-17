
include Configfile

#
# The Configfile stores the variables used in building
# and must contain at least the following variables:
# 
## The path to the android SDK 
# SDK =
#
## Jarsigner keystore file
# KEYSTORE_FILE =
#
## Jarsigner keystore password
# KEYSTORE_PASS =
#
## The alias for the given keystore
# KEYSTORE_ALIAS =
#
## An ssh server location to upload a build to
# REMOTE_SERVER =
#

apk : build sign zipalign

bar : apk
	$(SDK)/tools/apk2bar bin/GenesisChess.apk $(SDK)

build :
	ant release || exit

sign :
	jarsigner -verbose -keystore $(KEYSTORE_FILE) -storepass $(KEYSTORE_PASS) bin/GenesisChess-release-unsigned.apk $(KEYSTORE_ALIAS)

zipalign :
	$(SDK)/tools/zipalign -v -f 4 bin/GenesisChess-release-unsigned.apk bin/GenesisChess.apk

upload :
	scp bin/GenesisChess.apk $(REMOTE_SERVER)

install :
	$(SDK)/platform-tools/adb install -r bin/GenesisChess.apk

clean :
	rm -rf bin/classes/ bin/classes.dex bin/GenesisChess*

full-clean :
	rm -rf bin/
