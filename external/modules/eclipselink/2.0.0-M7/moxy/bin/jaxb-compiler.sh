#!/bin/sh
. `dirname $0`/setenv.sh 

# User may increase Java memory setting(s) if desired:
JVM_ARGS="-Xmx256m -Djava.endorsed.dirs=/../jlib/moxy"

# If going through a proxy, set the proxy host and proxy port below, then uncomment the line
# JVM_ARGS="${JVM_ARGS} -DproxySet=true -Dhttp.proxyHost= -Dhttp.proxyPort="

# Please do not change any of the following lines:
CLASSPATH=`dirname $0`/../jlib/moxy/javax.xml.stream_1.0.1.v200903100845.jar:\
`dirname $0`/../jlib/moxy/javax.xml.bind_2.1.12.v20090708-1500.jar:\
`dirname $0`/../jlib/moxy/javax.activation_1.1.0.v200806101325.jar:\
`dirname $0`/../jlib/moxy/jaxb-impl.jar:\
`dirname $0`/../jlib/moxy/jaxb-xjc.jar:\
`dirname $0`/../jlib/eclipselink.jar
JAVA_ARGS="$@"

${JAVA_HOME}/bin/java ${JVM_ARGS} -cp ${CLASSPATH} \
    com.sun.tools.xjc.XJCFacade -eclipselink ${JAVA_ARGS}
