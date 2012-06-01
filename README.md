# GenesisChess for Android #
----------------------------

## Summary ##

GenesisChess is an Android chess application. The main focus is for playing the
chess variant genesis chess, but traditial chess is also supported. Both local
and online (non-realtime) game play are supported. For local play the user can
choose to play against the included chess engine.

## License ##

The source is dual licensed under the GPLv3 and Apache 2.0 licenses. The default
license is Apache 2.0, except for the engine package source files which are
licensed under the GPLv3. Because the GPL license is copyleft, any use of the
object code form (ex. APK package) of this work must comply with section 6 of the
GPLv3 if the GPLv3 chess engine is being used.

## Externel Requirements ##

There are several externel library requirements needed to build the apk:

1. Android API v7+ (2.1) Platform SDK
2. Android Support Package for v4
3. Google AdMob Ads SDK
4. HttpMime jar from Apache Http Components

The Android SDK location is set in the local.properties file. The other libraries
are jar files that need to be copied or linked into the libs folder in the root
directory. The included Makefile provides the config command to assist setting up
all required configuration files and setting up the libs folder.
