@echo off
set script=%0
for %%F in (%script%) do set dirname=%%~dpF

set CLASSPATH=%dirname%couchtools.jar;%dirname%gson-1.7.1.jar;%dirname%couchbase-client-1.2.0.jar;%dirname%spymemcached-2.10.0.jar;%dirname%httpcore-4.1.1.jar;%dirname%httpcore-nio-4.1.1.jar;%dirname%jettison-1.1.jar;%dirname%netty-3.5.5.Final.jar;%dirname%commons-codec-1.5.jar
java com.mobiarch.tools.ManageDoc %*
