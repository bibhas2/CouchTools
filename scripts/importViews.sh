INSTALL_DIR=`dirname $0`
export CLASSPATH=$INSTALL_DIR/couchtools.jar:$INSTALL_DIR/gson-1.7.1.jar
java com.mobiarch.tools.ImportViews $*