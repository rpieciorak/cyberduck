#  Copyright (c) 2003 David Kocher. All rights reserved.
#  http://cyberduck.ch/
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  Bug fixes, suggestions and comments should be sent to:
#  dkocher@cyberduck.ch

#!/bin/bash
        
usage() {
	echo "Usage: udpateLocalization.sh -l <language> -n <nib>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       nib must be Preferences.nib, Main.nib, ..."
	echo "Call with no parameters to update all languages and all nib files"
}

init() {
    echo "Extracting strings from Java source files (genstrings)..."
    genstrings -j -q -o English.lproj source/ch/cyberduck/ui/cocoa/*.java
    echo "Extracting strings from Obj-C source files (genstrings)..."
    genstrings -a -q -o English.lproj source/ch/cyberduck/ui/cocoa/**/*.m
}

update() {
    updateNibFromStrings;
    udpateStringsFromNib;
}

status() {
	for lproj in `ls . | grep lproj`; do
		language=$lproj;
		if [ $language != "English.lproj" ]; then
			echo "*** Status of $language Localization...";
			polyglot -l `basename $language .lproj` .
		fi;
	done;
}

updateNibFromStrings() {
    rm -rf $language/$nibfile.bak
    mv $language/$nibfile $language/$nibfile.bak

    if($force == true); then
        {
            # force update
            echo "Updating $nib... (force)"
            nibtool --write $language/$nibfile --dictionary $language/$nib.strings English.lproj/$nibfile
        }
    else
        {
            # incremental update
            echo "Updating $nib... (incremental)"
            nibtool --write $language/$nibfile \
                    --incremental $language/$nibfile.bak \
                    --dictionary $language/$nib.strings English.lproj/$nibfile
        
        }
    fi;
    cp -R $language/$nibfile.bak/CVS $language/$nibfile/CVS
}

udpateStringsFromNib() {
    echo "Updating $nib.strings in $language..."
        
    rm $language/$nib.strings.bak
    mv $language/$nib.strings $language/$nib.strings.bak
    nibtool --previous English.lproj/$nibfile \
            --incremental $language/$nibfile \
            --localizable-strings English.lproj/$nibfile > $language/$nib.strings
}

language="all";
nibfile="all";
force=false;

while [ "$1" != "" ] # When there are arguments...
do
    case "$1" in 
		  -g | --genstrings)
				init;
				exit 0;
		  ;;
        -h | --help) 
            usage;
            exit 0;
        ;; 
        -s | --status)
        	status;
        	exit 0;
        ;; 
        -l | --language) 
            shift;
            language=$1
        ;;
        -n | --nib) 
            shift;
            nibfile=$1
        ;;
        -f | --force) 
            shift;
            force=true
        ;;
        *)  echo "Option [$1] not one of  [--language, --nib]"; # Error (!)
            exit 1
        ;; # Abort Script Now
    esac;
    shift;
done;

echo "Language:$language";
echo "Nib:$nibfile";

if [ "$language" = "all" ] ; then
    {
        echo "*** Updating all localizations...";
        for lproj in `ls . | grep lproj`; do
            language=$lproj;
			if [ $language != "English.lproj" ]; then
            {
                echo "*** Updating $language Localization...";
                if [ "$nibfile" = "all" ] ; then
                    echo "*** Updating all NIBs...";
                    for nibfile in `ls $language | grep .nib | grep -v ~.nib | grep -v .bak`; do
                        nib=`basename $nibfile .nib`
                        nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
                        update;
                    done;
                fi;
                if [ "$nibfile" != "all" ] ; then
                        nib=`basename $nibfile .nib`
                        nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
                        update;
                fi;
            }
            fi;
        done;
    }
    echo "*** DONE. ***";
    exit 0;
fi;


if [ "$language" != "all" ] ; then
    {
        echo "*** Updating $language Localization...";
        if [ "$nibfile" = "all" ] ; then
            echo "*** Updating all NIBs...";
            for nibfile in `ls $language | grep .nib | grep -v ~.nib | grep -v .bak`; do
                nib=`basename $nibfile .nib`;
                nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
                update;
            done;
        fi;
        if [ "$nibfile" != "all" ] ; then
        {
            nib=`basename $nibfile .nib`;
            nibtool --localizable-strings English.lproj/$nibfile > English.lproj/$nib.strings
            update;
        }
        fi;
    }
    echo "*** DONE. ***";
    exit 0;
fi;

exit 1;




