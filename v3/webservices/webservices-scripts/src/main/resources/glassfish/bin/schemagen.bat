@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java -cp "%~dp0..\modules\jaxb-api-osgi.jar;%~dp0..\modules\webservices-osgi.jar;%~dp0..\modules\jaxb-osgi.jar;%~dp0..\modules\webservices-api-osgi.jar" com.sun.tools.jxc.SchemaGeneratorFacade "%*"
