#/bin/bash -f

INSTALL_PATH=`dirname $0`

java -cp $INSTALL_PATH/lib/CouchTools.jar com.mobiarch.tools.QueryDocs "$@"
