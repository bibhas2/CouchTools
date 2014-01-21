INSTALL_DIR=`dirname $0`
export CLASSPATH=$INSTALL_DIR/couchtools.jar:$INSTALL_DIR/gson-1.7.1.jar:$INSTALL_DIR/couchbase-client-1.2.0.jar:$INSTALL_DIR/spymemcached-2.10.0.jar:$INSTALL_DIR/httpcore-4.1.1.jar:$INSTALL_DIR/httpcore-nio-4.1.1.jar:$INSTALL_DIR/jettison-1.1.jar:$INSTALL_DIR/netty-3.5.5.Final.jar:$INSTALL_DIR/commons-codec-1.5.jar
java com.mobiarch.tools.ManageDoc $*
