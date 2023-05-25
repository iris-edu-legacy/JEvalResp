#!/bin/bash

# this script checks that the preconditions necessary for the build are
# ok - that required projects are in the correct locations.

# after this, run "ant build" from the JEvalResp directory.

# IMPORTANT - modifies the *parent* directory (adds other directories)

dir=`pwd`
dir=`basename "$dir"`
if [ "$dir" != "JEvalResp" ]; then
    echo "ERROR: This script expects the jevalresp code to be in a directory"
    echo "called JEvalResp, not $dir"
    exit 1
fi

function checkoutweb {
    dir="$1"
    url="$2"
    if [ ! -e "$dir" ]; then
	echo "Building $dir"
	wget "$url"
	return 1
    else
	echo "Found $dir"
	return 0
    fi
}

function checkoutsvn {
    dir="$1"
    url="$2"
    if [ ! -e "$dir" ]; then
	echo "Building $dir"
	yes yes | svn co --username=jevalresp --password=readonly "$url" "$dir"
	return 1
    else
	echo "Found $dir"
	return 0
    fi
}

pushd ..

checkoutweb gnu.regexp http://www.isti.com/libs/zips/gnu.regexp.zip
if [ $? -ne 0 ]; then
    unzip gnu.regexp.zip
fi

# this needs to be before isti.util
checkoutweb JDOM http://www.isti.com/libs/zips/JDOM_b9_rc1.zip
if [ $? -ne 0 ]; then
    unzip JDOM_b9_rc1.zip
fi

checkoutsvn isti.util svn://svn.isti.com/isti.util/trunk
if [ $? -ne 0 ]; then
    pushd isti.util
    ant jar
    popd
fi

checkoutweb FissuresLib http://www.isti.com/libs/zips/FissuresLib_20131022.zip
if [ $? -ne 0 ]; then
    unzip FissuresLib_20131022.zip
fi

checkoutweb ORBacus4.0.5Naming http://www.isti.com/libs/zips/ORBacus4.0.5Naming.zip
if [ $? -ne 0 ]; then
    unzip ORBacus4.0.5Naming.zip
fi

checkoutweb JEvalResp/jars/IRIS-WS-2.0.11.jar http://www.iris.edu/files/IRIS-WS/2/2.0.11/IRIS-WS-2.0.11.jar
if [ $? -ne 0 ]; then
    ln -s `pwd`/IRIS-WS-2.0.11.jar JEvalResp/jars/
fi

checkoutweb JEvalResp/jars/junit-4.12.jar http://central.maven.org/maven2/junit/junit/4.12/junit-4.12.jar
if [ $? -ne 0 ]; then
    ln -s `pwd`/junit-4.12.jar JEvalResp/jars/
fi

checkoutweb JEvalResp/jars/hamcrest-core-1.3.jar http://central.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
if [ $? -ne 0 ]; then
    ln -s `pwd`/hamcrest-core-1.3.jar JEvalResp/jars/
fi
