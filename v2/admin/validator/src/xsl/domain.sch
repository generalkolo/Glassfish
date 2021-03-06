<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
            xmlns:t="http:///xsl-tests">
  <sch:ns prefix="t" uri="http:///xsl-tests"/>

  <!--
    We use schematron keys for a few of the individual elements we're
    interested in, and for defining namespaces. For individual
    elements we name the key as the plural of the element name, so the
    key on the jdbc-resource element is called 'jdbc-resources'.

    However we don't use keys widely - we could use if for defining
    the primary key property, except that that is more easily and
    naturally done via abstract rules.
  -->

  <!-- Known bugs:
  Identifiers are space normalized - its not clear whether this is or
  is not correct behaviour. In other words we consider 'a name' to be
  identical to 'a    name', although different from 'aname'.

  We don't correctly handle lists where there are spaces
  after the separators. In other words we consider the list 'a , b' as
  not containing the identifier 'a'

  We don't properly tokenize identifiers, so that an identity list
  that contains 'v10 v12' (i.e. one identifier) is seen as containing
  the identities 'v12', '2', ''. (This is because we stick a comma on
  the end of an identity and then look for it in the list, rather than
  try to isolate the identity first)

  availability-service - not handling the inheritance of the
  availability-enabled attribute correctly with the children.
-->
  <!-- Ideas
       I've realized I could do some reverse lookups using the key
       mechanism - for example, looking up servers by their
       config-ref, or clusters by their config-ref.

       I can also use a node-set as the value to a key - I haven't
       found where this could be used yet, but it appears to be
       interesting functinoality!
  -->

  <!--
    To be done

    Detecting half-socket conflicts
    _______________________________
    We want to prevent half-sockets from being reused (ie. host/port
    pairs). Its OK, though a warning, if a half-socket is being shared
    by multiple resources, only one of which is enabled. Its an error
    if a half-socket is being shared by multiple resources but more
    than one of them is enabled.
    
    Ports are referenced thus:

    http-listener@enabled
    http-listener@external-port
    http-listener@port
    http-listener@redirect-port
    http-listener@server-name - use of host:port!
    
    iiop-listener@port
    iiop-listener@enabled

    jmx-connector@address - hostname
    jmx-connector@port

    jms-host@host
    jms-host@port

    failure-notification-service@udp-protocol-multicast-port

    Need to make sure that each and every one of the above ports is a
    listen port, and understand how to relate it to the hostname, so
    that true identification of the half-socket is achieved (after
    all, its only the half-sockets that cannot be shared, port numbers
    and host names can be duplicated many times!)

    http-listener addressing
    ________________________
    The address attribute of an http-listener says "Configuring a
    listen socket to listen on any is required if more than one
    http-listener is configured to it." - what does this mean? How can
    this be checked?
  -->
  <sch:pattern name="Abstract Rules">
    <sch:p>
      These rules are used by many different elements in different
      contexts. They are therefore abstract of their application
      context
    </sch:p>

    <!-- we use the name of these primary key rules to generate
    location printing templates. We get the name of the attribute by
    stripping off the suffix '-IsPrimaryKey'
    -->
    <sch:rule id="id-IsPrimaryKey" abstract="true">
      <sch:assert id="idPrimaryKey"
                  test="not(preceding-sibling::*[name()=name(current())][./@id=current()/@id])"
                  diagnostics="idPrimaryKey">
        Sibling elements of the same kind (<sch:name/>) are uniquely named by their <code>id</code> attribute.
      </sch:assert>
      <t:test ruleId="id-IsPrimaryKey" assertionId="idPrimaryKey"
              id="1" expectedNonAssertions="1">
        <element id="foo"/>
        <element id="fee"/>
      </t:test>
      <t:test ruleId="id-IsPrimaryKey" assertionId="idPrimaryKey"
              id="2" expectedAssertions="1">
        <element id="foo"/>
        <element id="foo"/>
      </t:test>
    </sch:rule>

    <sch:rule id="jndi-name-IsPrimaryKey" abstract="true">
      <sch:assert id="jndiPrimaryKey"
                  test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"
                  diagnostics="jndiPrimaryKey">
        Sibling elements of the same kind (<sch:name/>) are uniquely named by their <code>jndi-name</code> attribute.
      </sch:assert>
      <t:test ruleId="jndi-name-IsPrimaryKey"
              assertionId="jndiPrimaryKey" id="1" expectedNonAssertions="1">
        <element jndi-name="foo"/>
        <element jndi-name="fee"/>
      </t:test>
      <t:test ruleId='jndiPrimaryKey' assertionId="jndiPrimaryKey"
              id="2" expectedAssertions="1">
        <element jndi-name="foo"/>
        <element jndi-name="foo"/>
      </t:test>
    </sch:rule>

    <sch:rule id="name-IsPrimaryKey" abstract="true">
      <sch:assert id="namePrimaryKey"
                  test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"
                  diagnostics="namePrimaryKey">
        Sibling elements of the same kind (<sch:name/>) are uniquely named by their <code>name</code> attribute.
      </sch:assert>
      <t:test ruleId="name-IsPrimaryKey" id="1" assertionId="namePrimaryKey" expectedAssertions="1" expectedNonAssertions="2">
        <element name="a"/>
        <element name="b"/>
        <element name="a"/>
      </t:test>
      <t:test ruleId="name-IsPrimaryKey" id="2" assertionId="namePrimaryKey" expectedNonAssertions='2'>
        <element name="a"/>
        <element2 name="a"/>
      </t:test>
    </sch:rule>

    <sch:rule id='noPoolGreaterThanMaxPoolSize' abstract="true">
      <sch:assert id="poolSize" test="@steady-pool-size &lt;= @max-pool-size">
        Steady pool size is not greater than the maximum pool size
      </sch:assert>
      <t:test ruleId="noPoolGreaterThanMaxPoolSize" assertionId="poolSize" id="1" expectedAssertions="1">
        <pool steady-pool-size="10" max-pool-size="5"/>
      </t:test>
      <t:test ruleId="noPoolGreaterThanMaxPoolSize" assertionId="poolSize" id="1" expectedNonAssertions="1">
        <pool steady-pool-size="10" max-pool-size="20"/>
      </t:test>
    </sch:rule>

    <sch:rule id='noRefsToDefaultConfig' abstract='true'>
      <sch:assert id="noRefsToDefaultConfig"
                  test="not(@config-ref='default-config')">
        No reference is made to the <code>default-config</code>
        <code>config</code> element.
      </sch:assert>
      <t:test ruleId="noRefsToDefaultConfig"
              assertionId="noRefsToDefaultConfig" id="1" expectedAssertions="1">
        <foo config-ref='default-config'/>
      </t:test>
      <t:test ruleId="noRefsToDefaultConfig"
              assertionId="noRefsToDefaultConfig" id="2" expectedNonAssertions="1">
        <foo config-ref="config1"/>
      </t:test>
    </sch:rule>

    <sch:rule id="referencedConfigsMustExist" abstract="true">
      <!-- we use an implied form for the test so we can use this rule
      for server elements where the config-ref is optional -->
      <sch:assert id="referencedConfigsMustExist"
                  test="not (@config-ref) or key('configs',@config-ref)">
        <sch:name/> elements <code>config-ref</code> attribute names
        an existing <code>config</code> element.
      </sch:assert>
                  
    </sch:rule>
    <sch:rule id="ref-IsPrimaryKey" abstract="true">
      <sch:assert id="refPrimaryKey"
                  test="not(preceding-sibling::*[name()=name(current())][./@ref=current()/@ref])"
                  diagnostics="refPrimaryKey">
        Sibling elements of the same kind (<sch:name/>) are uniquely named by their <code>ref</code> attribute.
      </sch:assert>
      <t:test ruleId="ref-IsPrimaryKey" assertionId="refPrimaryKey"
              id="1" expectedAssertions="1">
        <object ref="foo"/>
        <object ref="foo"/>
      </t:test>
      <t:test ruleId="ref-IsPrimaryKey" assertionId="refPrimaryKey"
              id="2" expectedNonAssertions="1">
        <object ref="foo"/>
        <object ref="fee"/>
      </t:test>
    </sch:rule>

  </sch:pattern>

  <sch:pattern name="Rules for Individual Elements">
    <sch:p>
      These are rules whose context is an individual named
      element. Most rules fall into this category.
    </sch:p>

    <sch:rule id="r3" context="admin-object-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
      <t:test ruleId="r3"  id="1" expectedAssertions="0" expectedNonAssertions="1" assertionId="jndiPrimaryKey">
        <resources>
          <admin-object-resource jndi-name="foo"/>
        </resources>
      </t:test>
      <t:test ruleId="r3"  id="2" expectedNonAssertions="1" expectedAssertions="1" assertionId="jndiPrimaryKey">
        <admin-object-resource jndi-name="foo"/>
        <admin-object-resource jndi-name="foo"></admin-object-resource>
      </t:test>
      <t:test ruleId="r3"  id="3" expectedAssertions="0" expectedNonAssertions="1" assertionId="jndiPrimaryKey">
        <connector-resource jndi-name="boo"/>
        <admin-object-resource jndi-name="boo"/>
      </t:test>
      <t:test ruleId="r3"  id="4" expectedAssertions="0" expectedNonAssertions="2" assertionId="jndiPrimaryKey">
        <connector-resource jndi-name="boo"/>
        <admin-object-resource jndi-name="boo"/>
        <admin-object-resource id="boo"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r4" context="admin-service">
      <sch:assert
       id="namedChildExists"
       test="@system-jmx-connector-name and jmx-connector[@name=current()/@system-jmx-connector-name]">
        An <sch:name/> element which has a
        <code>system-jmx-connector-name</code> attribute uses that
        attribute to refer to an existing <code>jmx-connector</code>
        child element.
      </sch:assert>
      <t:test ruleId="r4" assertionId="namedChildExists" id="1" expectedAssertions="1">
        <admin-service system-jmx-connector-name="foo"/>
      </t:test>
      <t:test ruleId="r4" assertionId="namedChildExists" id="2" expectedNonAssertions="1">
        <admin-service system-jmx-connector-name="foo">
          <jmx-connector name="foo"/>
        </admin-service>
      </t:test>
                  
    </sch:rule>
    
    <sch:rule id="r5"  context="appclient-module">
      <sch:extends rule="name-IsPrimaryKey"/>
      <t:test ruleId="r5" id="1" assertionId="namePrimaryKey"
              expectedAssertions="1" expectedNonAssertions="1">
        <appclient-module name="a"/>
        <appclient-module name="a"/>
      </t:test>
      <t:test ruleId="r5" id="2" assertionId="namePrimaryKey" expectedNonAssertions='1'>
        <appclient-module name="a"/>
        <cluster name="a"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r6"  context="application-ref">
      <sch:extends rule="ref-IsPrimaryKey"/>
      <t:test ruleId="r6" id="1" assertionId="refPrimaryKey" expectedNonAssertions="1">
        <application-ref ref="foo"/>
      </t:test>
      <t:test ruleId="r6" id="2" assertionId="refPrimaryKey" expectedNonAssertions="1" expectedAssertions="1">
        <application-ref ref="foo"/>
        <application-ref ref="foo"/>
      </t:test>
      <t:test ruleId="r6" id="3" assertionId="refPrimaryKey" expectedNonAssertions="1">
        <cluster-ref ref="foo"/>
        <application-ref ref="foo"/>
      </t:test>

      <sch:assert
       id="applicationRef"
       test="not(contains('1yestrueon', @enabled)) or key('applications',@ref)"
       diagnostics="invalid-reference">
        <sch:name/> elements which are enabled must refer to existing
        applications
      </sch:assert>
      <t:test ruleId="r6" id="1" assertionId="applicationRef" expectedAssertions="0" expectedNonAssertions="1">
        <domain>
          <applications>
            <web-module name="adminapp" >
            </web-module>
          </applications>
          <servers>
            <server>
              <application-ref enabled="true" ref="adminapp"/>
            </server>
          </servers>
        </domain>
      </t:test>
      <t:test ruleId="r6" id="2" assertionId="applicationRef" expectedAssertions="1" expectedNonAssertions="0">
        <domain>
          <applications>
            <web-module name="adminapp">
            </web-module>
          </applications>
          <servers>
            <server>
              <application-ref enabled="true" ref="foobar"/>
            </server>
          </servers>
        </domain>
      </t:test>
      <t:test ruleId="r6" id="3" assertionId="applicationRef" expectedAssertions="0" expectedNonAssertions="1">
        <domain>
          <applications>
            <web-module name="adminapp" >
            </web-module>
          </applications>
          <servers>
            <server>
              <application-ref enabled="false" ref="adminapp"/>
            </server>
          </servers>
        </domain>
      </t:test>
      <t:test ruleId="r6" id="4" assertionId="applicationRef" expectedAssertions="0" expectedNonAssertions="1">
        <domain>
          <applications>
            <web-module name="foo" >
            </web-module>
          </applications>
          <servers>
            <server>
              <application-ref enabled="false" ref="adminapp"/>
            </server>
          </servers>
        </domain>
      </t:test>
      <p>
        Applications which have been deployed to a server must match
        the kind of server they have been deployed to as explained in
        the section <a href="#deployedObjects">Relationship between
        deployed objects and their servers</a>
      </p>
      <sch:assert id="adminAppsOnDAS"
                  test="not(key('applications',@ref)[@object-type='system-admin'])
                  or
                  key('configs',current()/../@config-ref)/admin-service[@type!='server']
                  ">
        Administrative applications must only be deployed to a DAS server
      </sch:assert>
      <t:test ruleId="r6" id="1" assertionId="adminAppsOnDAS" expectedNonAssertions="1">
        <servers>
          <server config-ref="config1">
            <application-ref ref="app1"/>
          </server>
        </servers>
        <applications>
          <application object-type="system-admin" name="app1"/>
        </applications>
        <configs>
          <config name="config1">
            <admin-service type="das"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r6" id="2" assertionId="adminAppsOnDAS" expectedAssertions="1">
        <servers>
          <server config-ref="config2">
            <application-ref ref="app2"/>
          </server>
        </servers>
        <applications>
          <application object-type="system-admin" name="app2"/>
        </applications>
        <configs>
          <config name="config2">
            <admin-service type="server"/>
          </config>
        </configs>
      </t:test>

      <sch:assert id="instanceAppsOnServer"
                  test="not(key('applications',@ref)[@object-type='system-instance'])
                  or
                  key('configs',current()/../@config-ref)/admin-service[@type = 'server']
                  ">
        Instance applications must only be deployed to non-DAS servers
      </sch:assert>
      <t:test ruleId="r6" id="1" assertionId="instanceAppsOnServer" expectedNonAssertions="1">
        <servers>
          <server config-ref="iaos-config1">
            <application-ref ref="iaos-app1"/>
          </server>
        </servers>
        <applications>
          <application object-type="system-instance" name="iaos-app1"/>
        </applications>
        <configs>
          <config name="iaos-config1">
            <admin-service type="server"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r6" id="2" assertionId="instanceAppsOnServer" expectedAssertions="1">
        <servers>
          <server config-ref="iaos-config2">
            <application-ref ref="iaos-app2"/>
          </server>
        </servers>
        <applications>
          <application object-type="system-instance" name="iaos-app2"/>
        </applications>
        <configs>
          <config name="iaos-config2">
            <admin-service type="das-and-server"/>
          </config>
        </configs>
      </t:test>
      <p>
        The virtual servers which are referenced from an
        <code>application-ref</code> must exist within the
        containing server parent's <code>config</code>.
      </p>
      <p>
        If the server parent is not in a cluster then the config here
        is the one referenced by the server's <code>config-ref</code>
        attribute. If the server parent is in a cluster then its the
        config of the containing cluster.
      </p>
      <!--
      The technique used in the test is to first check that the list
      of virtual-servers is empty (either doesn't exist, null string,
      or spaces only). If that's not the case then count the number of
      virtual servers who have ids in the list, and see if that's the
      same number as the length of the list.

      The virtual servers to be considered are only those in the
      correct config. The correct config is one of two possibilities -
      either the server (which is our parent) is not referenced by a
      cluster, in which case its the config that is referenced by that
      server, or the server is referenced by a cluster, in which case
      its the config referenced by that cluster.

      Each virtual-server id and the list of virtual servers has a
      comma appended to it - this ensures that only ids which fully
      match a corresponding id in the virtual server list are reported
      - without this an id of 'v1' would match a list that contained
      'v10'.

      the length of the list is the number of separators in the list,
      plus 1.
      -->
      <sch:assert id="virtualServersExist"
                  test="not(string(normalize-space(@virtual-servers)))
                  or
                  count(//virtual-server[ancestor::config[@name=current()/parent::server[not(@name=//cluster/server-ref/@ref)]/@config-ref]][contains(concat(normalize-space(current()/@virtual-servers),','),
                           concat(normalize-space(@id), ','))] |
                  //virtual-server[ancestor::config[@name=//cluster[server-ref/@ref=current()/../@name]/@config-ref]][contains(current()/@virtual-servers, @id)])=
                  string-length(translate(normalize-space(@virtual-servers),
                  translate(normalize-space(@virtual-servers), ',', ''),
                  ''))+1">
        All virtual servers in the
        <code>virtual-servers</code> list exist as children of the
        <code>config</code> associated with the parent
        <code>server</code> element.
      </sch:assert>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="1" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="2" expectedAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs2"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="3" expectedNonAssertions="1">
        <server name="server1">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
        <cluster config-ref="config1">
          <server-ref ref="server1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="4" expectedAssertions="1">
        <server name="server1">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
        <cluster config-ref="config1">
          <server-ref ref="server2"/>
        </cluster>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="5" expectedNonAssertions="1">
        <server name="server1" config-ref="config2">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
        <config name="config2">
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
        </config>
        <cluster config-ref="config1">
          <server-ref ref="server1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="6" expectedAssertions="1">
        <server name="server1" config-ref="config1">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
        <config name="config2">
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
        </config>
        <cluster config-ref="config2">
          <server-ref ref="server1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="7" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs1 with spaces in name"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1 with spaces in name"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="8a" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="8" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers=" "/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="9" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs1, vs2"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="10" expectedNonAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs1, vs2, vs3"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
          <http-listener>
            <virtual-server id="vs2"/>
          </http-listener>
          <http-listener>
            <virtual-server id="vs3"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="11" expectedAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs1"/>
        </server>
        <config name="config2">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
      </t:test>
      <t:test ruleId="r6" assertionId="virtualServersExist" id="12" expectedAssertions="1">
        <server config-ref="config1">
          <application-ref virtual-servers="vs10"/>
        </server>
        <config name="config1">
          <http-listener>
            <virtual-server id="vs1"/>
          </http-listener>
        </config>
      </t:test>
    </sch:rule>

    <sch:rule id="r7" context="applications">
      <sch:assert test="true()"/>
    </sch:rule>
    
    <sch:rule id="r100" context="audit-module">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r101" context="auth-realm">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r102" context="availability-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r103" context="backend-principal">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r104" context="cluster">
      <sch:extends rule="name-IsPrimaryKey"/>
      <sch:extends rule="noRefsToDefaultConfig"/>
      <sch:extends rule="referencedConfigsMustExist"/>
    </sch:rule>

    <sch:rule id="r104-1" context="clusters">
      <sch:assert test="true()"/>
    </sch:rule>
    
    <sch:rule id="r105" context="config">
      <sch:key name="configs" path="@name"/>
      <sch:extends rule='name-IsPrimaryKey'/>
    </sch:rule>

    <sch:rule id="r105-1" context="configs">
      <sch:assert id="adminServiceMustExist"
                  test="config/admin-service[@type='das-and-server'
                  or @type='das']"
                  diagnostics="admin-service-diagnostic">
        There must be an <code>admin-service</code> element configured for the DAS
      </sch:assert>
      <t:test ruleId="r105-1" id="1" assertionId="adminServiceMustExist" expectedNonAssertions="1">
        <configs>
          <config>
            <admin-service type="das-and-server"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r105-1" id="2" assertionId="adminServiceMustExist" expectedNonAssertions="1">
        <configs>
          <config>
            <admin-service type="das"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r105-1" id="3" assertionId="adminServiceMustExist"
              expectedAssertions="1" expectedNonAssertions="0">
        <configs>
          <config>
            <admin-service/>
          </config>
        </configs>
      </t:test>

      <sch:assert id="onlyOneAdminService" test="count(config/admin-service[@type='das-and-server'
                  or @type='das']) &lt; 2" diagnostics="admin-service-diagnostic">
        There must be only one <code>admin-service</code> element configured for the DAS
      </sch:assert>
      <t:test ruleId="r105-1" id="1" assertionId="onlyOneAdminService" expectedNonAssertions="1" expectedAssertions="0">
        <configs>
          <config>
            <admin-service type="das"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r105-1" id="2" assertionId="onlyOneAdminService" expectedNonAssertions="0" expectedAssertions="1">
        <configs>
          <config>
            <admin-service type="das"/>
          </config>
          <config>
            <admin-service type="das-and-server"/>
          </config>
        </configs>
      </t:test>

      <sch:assert id="defaultConfigMustExist" test="config[@name='default-config']">
        There must be a <code>config</code> element named "default-config"
      </sch:assert>
      <t:test ruleId="r105-1" id="1" assertionId="defaultConfigMustExist"
              expectedNonAssertions="1">
        <configs>
          <config name="default-config"/>
          <config name="config-2"/>
        </configs>
      </t:test>
      <t:test ruleId="r105-1" id="2" assertionId="defaultConfigMustExist"
              expectedAssertions="1" expectedNonAssertions="0">
        <configs>
          <config name="config-1"/>
          <config name="config-2"/>
        </configs>
      </t:test>

      <sch:assert id="onlyOneDefaultConfig" test="count(config[@name='default-config']) &lt; 2">
        There must be only one <code>config</code> element named "default-config"
      </sch:assert>
      <t:test ruleId="r105-1" id="1" assertionId="onlyOneDefaultConfig" expectedNonAssertions="1">
        <configs>
          <config name="default-config"/>
          <config name="config-2"/>
        </configs>
      </t:test>
      <t:test ruleId="r105-1" id="2" assertionId="onlyOneDefaultConfig" expectedAssertions="1">
        <configs>
          <config name="default-config"/>
          <config name="default-config"/>
        </configs>
      </t:test>
    </sch:rule>


    <sch:rule id="r106" context="connection-pool">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r107" context="connector-connection-pool">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r108" context="connector-module">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r109" context="connector-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
      <t:test ruleId="r109" id="cr-1" expectedAssertions="0" expectedNonAssertions="1" assertionId="jndiPrimaryKey">
        <resources>
          <connector-resource jndi-name="foo"/>
        </resources>
      </t:test>
      <t:test ruleId="r109" id="cr-2" expectedNonAssertions="1" expectedAssertions="1" assertionId="jndiPrimaryKey">
        <connector-resource jndi-name="foo"/>
        <connector-resource jndi-name="foo"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r110" context="connector-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r112" context="custom-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r113" context="das-config">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r114" context="description">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r115" context="domain">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r116" context="ejb-container">
      <sch:extends rule="noPoolGreaterThanMaxPoolSize"/>
      <t:test ruleId="r116"  id="1" expectedAssertions="1" assertionId="poolSize">
        <ejb-container steady-pool-size="1" max-pool-size="0"/>
      </t:test>
      <t:test ruleId="r116"  id="4" expectedNonAssertions="1" assertionId="poolSize">
        <ejb-container steady-pool-size="10" max-pool-size="45"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r117" context="ejb-container-availability">
      <sch:assert
       id="parentPoolExists"
       test="not(contains('1yestrueon', ../@availability-enabled) and
                 (not(string(@availability-enabled)) or
                 contains('1yestrueon', @availability-enabled)) and
                 not(string(normalize-space(@sfsb-store-pool-name))))
             or key('jdbc-resources', ../@store-pool-name)">
        The <code>store-pool-name</code> attribute of the parent of
        an <sch:name/> element must
        reference an existing <code>jdbc-resource</code> when the
        <sch:name/> element is enabled but
        doesn't have an <code>sjsb-store-pool-name</code> attribute.
      </sch:assert>
      <t:test ruleId="r117" assertionId="parentPoolExists" id="1"
              expectedAssertions="1">
        <availability-service availability-enabled='1'>
          <ejb-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r117" assertionId="parentPoolExists" id="2"
              expectedNonAssertions="1">
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='1' store-pool-name='foo'>
          <ejb-container-availability/>
        </availability-service>
      </t:test>
      
      <sch:assert
       id="sfsbStorePoolExists"
        test="not(contains('1yestrueon', ../@availability-enabled) and 
                  (not(string(@availability-enabled)) or contains('1yestrueon', @availability-enabled))
                  and string(normalize-space(@sfsb-store-pool-name)))
              or key('jdbc-resources',@sfsb-store-pool-name)">
        An <sch:name/> element whose parent
        is enabled, which is enabled itself, and which has a non-empty
        <code>sfsb-store-pool-name</code> attribute references
        an existing <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r117" assertionId="sfsbStorePoolExists" id="1"
              expectedNonAssertions="1">
        <availability-service availability-enabled='1'>
          <ejb-container-availability availability-enabled='yes' sfsb-store-pool-name="foo"/>
        </availability-service>
        <jdbc-resource jndi-name="foo"/>
      </t:test>
      <t:test ruleId="r117" assertionId="sfsbStorePoolExists" id="2"
              expectedNonAssertions="0" expectedAssertions="1">
        <availability-service availability-enabled='yes'>
          <ejb-container-availability availability-enabled='1'
                                      sfsb-store-pool-name="foo"/>
        </availability-service>
        <jdbc-resource jndi-name="fee"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r118" context="ejb-module">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r119" context="ejb-timer-service">
      <sch:assert id="timerDatasourceExists"
                  test="not(@timer-datasource) or key('jdbc-resources',@timer-datasource)">
        If an <sch:name/> element has a <code>timer-datasource</code>
        attribute then the value of that attribute must be the
        <code>jndi-name</code> of a <code>jdbc-resource</code> element.
      </sch:assert>
      <t:test ruleId="r119" assertionId="timerDatasourceExists" id="1" expectedNonAssertions="1">
        <ejb-timer-service timer-datasource="foo"/>
        <jdbc-resource jndi-name="foo"/>
      </t:test>
      <t:test ruleId="r119" assertionId="timerDatasourceExists" id="2" expectedAssertions="1">
        <ejb-timer-service timer-datasource="foo"/>
        <jdbc-resource jndi-name="fee"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r120" context="external-jndi-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r228"	context="health-checker">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r229"	context="http-access-log">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r230"	context="http-file-cache">
      <sch:assert id="mediumSizeNoGreaterThanSpace"
                  test="@medium-file-size-limit-in-bytes &lt;= @medium-file-space-in-bytes">
        The medium file size limit is not greater than the medium file space
      </sch:assert>
      <t:test ruleId="r230" assertionId="mediumSizeNoGreaterThanSpace"
              id="1" expectedNonAssertions="1">
        <http-file-cache medium-file-size-limit-in-bytes="500" medium-file-space-in-bytes="1000" />
      </t:test>
      <t:test ruleId="r230" assertionId="mediumSizeNoGreaterThanSpace"
              id="2" expectedAssertions="1">
        <http-file-cache medium-file-size-limit-in-bytes="1001" medium-file-space-in-bytes="1000" />
      </t:test>

      <sch:assert id="smallSizeNoGreaterThanSpace"
                  test="@small-file-size-limit-in-bytes &lt;= @small-file-space-in-bytes">
        The small file size limit is not greater than the small file space
      </sch:assert>
      <t:test ruleId="r230" assertionId="smallSizeNoGreaterThanSpace"
              id="1" expectedNonAssertions="1">
        <http-file-cache small-file-size-limit-in-bytes="500" small-file-space-in-bytes="1000" />
      </t:test>
      <t:test ruleId="r230" assertionId="smallSizeNoGreaterThanSpace"
              id="2" expectedAssertions="1">
        <http-file-cache small-file-size-limit-in-bytes="1001" small-file-space-in-bytes="1000" />
      </t:test>
    </sch:rule>

    <sch:rule id="r231"	context="http-listener">
      <sch:extends rule="id-IsPrimaryKey"/>
      <sch:assert id="defaultVSPeer"
                  test="not(@default-virtual-server) or ../virtual-server[@id = current()/@default-virtual-server]" diagnostics="non-existant-virtual-server-ref">
        If an <sch:name/> element references a default virtual server then that virtual server must exist as a peer of the referencing <sch:name/>
      </sch:assert>
      <t:test ruleId="r231" id="1" assertionId="defaultVSPeer" expectedNonAssertions="1">
        <http-listener default-virtual-server="foo"/>
        <virtual-server id="foo"/>
      </t:test>
      <t:test ruleId="r231" id="2" assertionId="defaultVSPeer" expectedAssertions="1">
        <config name="c1">
          <http-listener default-virtual-server="foo"/>
        </config>
        <config>
          <virtual-server id="foo"/>
        </config>
      </t:test>
    </sch:rule>

    <sch:rule id="r232"	context="http-protocol">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r233"	context="http-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r234"	context="idempotent-url-pattern">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r235"	context="iiop-listener">
      <sch:extends rule="id-IsPrimaryKey"/>
      <t:test ruleId="r235" id="1" assertionId="idPrimaryKey" expectedNonAssertions="1">
        <iiop-listener id="1"/>
      </t:test>
      <t:test ruleId="r235" id="2" assertionId="idPrimaryKey" expectedNonAssertions="1" expectedAssertions="1">
        <iiop-listener id="1"/>
        <iiop-listener id="1"/>
      </t:test>
      <t:test ruleId="r235" id="3" assertionId="idPrimaryKey" expectedNonAssertions="2">
        <iiop-listener id="1"/>
        <iiop-listener id="2"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r236"	context="iiop-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r237"	context="j2ee-application">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r238"	context="jacc-provider">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r239"	context="java-config">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r240"	context="jdbc-connection-pool">
      <sch:extends rule="name-IsPrimaryKey"/>
      <sch:extends rule="noPoolGreaterThanMaxPoolSize"/>
      <t:test ruleId="r240" assertionId="poolSize" id="1" expectedAssertions="1">
        <jdbc-connection-pool steady-pool-size="10" max-pool-size="5"/>
      </t:test>
      <t:test ruleId="r240" assertionId="poolSize" id="2" expectedNonAssertions="1">
        <jdbc-connection-pool steady-pool-size="10" max-pool-size="20"/>
      </t:test>

      <sch:assert id="validationMethod"
                  test="not(@connection-validation-method='table') or @validation-table-name">
        When the <code>connection-validation-method</code> attribute
        has the value 'table' then the
        <code>validation-table-name</code> attribute must be supplied.
      </sch:assert>
    </sch:rule>

    <sch:rule id="r241"	context="jdbc-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
      <sch:key name="jdbc-resources" path="@jndi-name"/>
      <t:test ruleId="r241" assertionId="jndiPrimaryKey" id='1'
              expectedAssertions='1' expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <jdbc-resource jndi-name="foo"/>
      </t:test>
      <t:test ruleId="r241" assertionId="jndiPrimaryKey" id='2' expectedNonAssertions='2'>
        <jdbc-resource jndi-name="foo"/>
        <jdbc-resource jndi-name="fee"/>
      </t:test>
      <sch:assert id="namedPoolExists"
                  test="not(contains('1yestrueon', @enabled)) or ../jdbc-connection-pool[@name=current()/@pool-name]">
        When the <sch:name/> is enabled then the <code>pool-name</code>
        attribute names an existing <code>jdbc-connection-pool</code>
      </sch:assert>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='1' expectedAssertions='1'>
        <jdbc-resource enabled="true" pool-name="pool"/>
        <jdbc-connection-pool name="not this one"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='2' expectedNonAssertions='1'>
        <jdbc-resource enabled="no" pool-name="pool"/>
        <jdbc-connection-pool name="pool"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='3' expectedAssertions='1'>
        <jdbc-resource enabled="1" pool-name="pool"/>
        <jdbc-connection-pool name="not this one"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='4' expectedAssertions='1'>
        <jdbc-resource enabled="yes" pool-name="pool"/>
        <jdbc-connection-pool name="not this one"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='5' expectedAssertions='1'>
        <jdbc-resource enabled="on" pool-name="pool"/>
        <jdbc-connection-pool name="not this one"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='6' expectedNonAssertions='1'>
        <jdbc-resource enabled="off" pool-name="pool"/>
        <jdbc-connection-pool name="pool"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='7' expectedNonAssertions='1'>
        <jdbc-resource enabled="0" pool-name="pool"/>
        <jdbc-connection-pool name="pool"/>
      </t:test>
      <t:test ruleId="r241" assertionId="namedPoolExists" id='8' expectedNonAssertions='1'>
        <jdbc-resource enabled="false" pool-name="pool"/>
        <jdbc-connection-pool name="pool"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r242"	context="jms-host">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r243"	context="jms-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r244"	context="jmx-connector">
      <sch:extends rule="name-IsPrimaryKey"/>
      <sch:assert id="authRealmExists"
                  test="../../security-service/auth-realm[@name=current()/@auth-realm-name]">
        The <code>auth-realm</code> element named in the
        <code>auth-realm-name</code> attribute exists.
      </sch:assert>
      <t:test ruleId="r244" assertionId="authRealmExists" id="1"
              expectedAssertions="1">
        <config>
          <admin-service>
            <jmx-connector auth-realm-name="foo"/>
          </admin-service>
          <security-service>
            <auth-realm name="not this one"/>
          </security-service>
        </config>
      </t:test>
      <t:test ruleId="r244" assertionId="authRealmExists" id="2" expectedNonAssertions="1">
        <config>
          <admin-service>
            <jmx-connector auth-realm-name="foo"/>
          </admin-service>
          <security-service>
            <auth-realm name="foo"/>
          </security-service>
        </config>
      </t:test>
    </sch:rule>

    <sch:rule id="r245"	context="jvm-options">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r246"	context="keep-alive">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r247"	context="lb-cluster">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r248"	context="lb-instance">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r249"	context="lb-web-module">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r250"	context="lifecycle-module">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r251"	context="loadbalancer-config">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r252"	context="loadbalancers">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r253"	context="log-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r254"	context="mail-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r255"	context="manager-properties">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r256"	context="mdb-container">
      <sch:extends rule="noPoolGreaterThanMaxPoolSize"/>
    </sch:rule>

    <sch:rule id="r257"	context="module-log-levels">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r258"	context="module-monitoring-levels">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r259"	context="monitoring-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r260"	context="node-agent">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r261"	context="node-agents">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r262"	context="orb">
      <!-- This appears quite tricky, but it isn't really. What we
           want to do is to find out if all the elements of a list refer to
           other elements. What we do is to compare the number of elements whose ids
           match parts of the list to the number of separators in the list,
           +1.

           The bit to pull the list apart is an XSLT trick - you translate
           a string twice - first time you translate it to all the
           characters EXCEPT those you're interested in, and then use this
           as the pattern to be removed from the original string, leaving
           only the characters you were interested in!
      -->
      <sch:assert id="useThreadPoolsExist"
                  test="count(ancestor::config//thread-pool[contains(concat(normalize-space(current()/@use-thread-pool-ids),','),
                                                     concat(normalize-space(@thread-pool-id),','))])
                  =
                  string-length(translate(normalize-space(current()/@use-thread-pool-ids),
                  translate(normalize-space(current()/@use-thread-pool-ids), ',',''),
                  '')) + 1" diagnostics="orbLocation">
        Within an <sch:name/> element all the thread pools referenced by the
        <code>use-thread-pool-ids</code> attribute exist.
      </sch:assert>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="1" expectedAssertions="1">
        <orb use-thread-pool-ids="1"/>
        <thread-pool thread-pool-id="4"/>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="2" expectedNonAssertions="1">
        <config>
          <orb use-thread-pool-ids="4"/>
          <thread-pool thread-pool-id="4"/>
        </config>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="3" expectedAssertions="1">
        <config>
          <orb use-thread-pool-ids="1, 2"/>
          <thread-pool thread-pool-id="4"/>
        </config>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="4" expectedNonAssertions="1">
        <config>
          <orb use-thread-pool-ids="1, 2"/>
          <thread-pool thread-pool-id="1"/>
          <thread-pool thread-pool-id="2"/>
        </config>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="5" expectedAssertions="1">
        <config>
          <orb use-thread-pool-ids="1, 2"/>
          <thread-pool thread-pool-id="1"/>
        </config>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="6" expectedAssertions="1">
        <config name='default-config'>
          <orb use-thread-pool-ids="1"/>
        </config>
        <config name='config-two'>
          <thread-pool thread-pool-id='1'/>
        </config>
      </t:test>
      <t:test ruleId="r262" assertionId="useThreadPoolsExist" id="7" expectedNonAssertions="1">
        <config name='config-two'>
          <orb use-thread-pool-ids="1"/>
          <thread-pool thread-pool-id='1'/>
        </config>
      </t:test>
    </sch:rule>


    <sch:rule id="r264"	context="principal">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r265"	context="profiler">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r266"	context="property">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r267"	context="quorum-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r268"	context="request-processing">
      <sch:assert id="threadCountIsMax"
                  test="@initial-thread-count &lt;= @thread-count">
        The <code>initial-thread-count</code> is no greater than the <code>thread-count</code>
      </sch:assert>
      <t:test ruleId="r268" assertionId="threadCountIsMax" id="1" expectedAssertions="1">
        <request-processing initial-thread-count="2" thread-count="1"/>
      </t:test>
      <t:test ruleId="r268" assertionId="threadCountIsMax" id="2" expectedNonAssertions="1">
        <request-processing initial-thread-count="1" thread-count="1"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r269"	context="resource-adapter-config">
      <sch:assert id="resource-adapter-name-IsPrimaryKey"
                  test="not(preceding-sibling::resource-adapter-config[@resource-adapter-name=current()/@resource-adapter-name])">
        Sibling <sch:name/> elements are uniquely named by their
        <code>resource-adapter-name</code> attribute.
      </sch:assert>
      <p>
        If the <code>resource-adapter-config</code> is referencing any
        thread pools then those thread pools must exist in each
        <code>config</code> to which this
        <code>resource-adapter-config</code> is (implicitly) bound.
      </p>
      <p>
        Both <code>servers</code> and <code>clusters</code> bind a
        <code>resource-adapter-config</code> to a <code>config</code>
        by referring to each of them. The <code>config</code> is
        referenced by the <code>config-ref</code> attribute; the
        <code>resource-adapter-config</code> is referenced in a
        <code>resource-ref</code>. 
      </p>
      <p>
        When a cluster references a server then the cluster's bindings
        override any bindings made in the server.
      </p>
      <!--
        thread-pool-ids implies that there's not a config which is
        related to this resource-adapter-config through a server or
        cluster binding which doesn't have the thread-pools that are in
        the list.

 |
             (//config[@name=//cluster[resource-ref/@ref=current()/@resource-adapter-name]/@config-ref]
                         [count(thread-pool[contains(concat(normalize-space(current()/@thread-pool-ids),','),
                                                     concat(normalize-space(@thread-pool-id),','))])
                          &lt;
                          string-length(translate(normalize-space(current()/@thread-pool-ids),
                                                  translate(normalize-space(current()/@thread-pool-ids), ',',''),
                                        '')) + 1])
      -->
      <sch:assert
       id="threadPoolsExist"
       test="not(string(normalize-space(@thread-pool-ids)))
             or
             not(//config[@name=//server[not(@name=//server-ref/@ref)][resource-ref/@ref=current()/@resource-adapter-name]/@config-ref] [count(thread-pool[contains(concat(normalize-space(current()/@thread-pool-ids),','),
                                                     concat(normalize-space(@thread-pool-id),','))])
                          &lt;
                          string-length(translate(normalize-space(current()/@thread-pool-ids),
                                                  translate(normalize-space(current()/@thread-pool-ids), ',',''),
                                        '')) + 1] | //config[@name=//cluster[resource-ref/@ref=current()/@resource-adapter-name]/@config-ref]
                         [count(thread-pool[contains(concat(normalize-space(current()/@thread-pool-ids),','),
                                                     concat(normalize-space(@thread-pool-id),','))])
                          &lt;
                          string-length(translate(normalize-space(current()/@thread-pool-ids),
                                                  translate(normalize-space(current()/@thread-pool-ids), ',',''),
                                        '')) + 1] )">
        All the thread pools referenced by the
        <code>thread-pool-ids</code> attribute exist in the related config.
      </sch:assert>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="13"
              expectedAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t2"/>
          </config>
        </configs>
        <clusters>
          <cluster config-ref="config1">
            <resource-ref ref="ra1"/>
          </cluster>
        </clusters>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="12"
              expectedNonAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t1"/>
          </config>
        </configs>
        <clusters>
          <cluster config-ref="config1">
            <resource-ref ref="ra1"/>
          </cluster>
        </clusters>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="11"
              expectedAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t1"/>
          </config>
          <config name="config2">
            <thread-pool thread-pool-id="t2"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
          <server config-ref="config2">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="10"
              expectedAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t1"/>
          </config>
          <config name="config2">
            <thread-pool thread-pool-id="t2"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config2">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>

      <t:test ruleId="r269" assertionId="threadPoolsExist" id="1"
              expectedAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t10"/> 
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t1"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="2"
              expectedNonAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t10"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t10"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="3"
              expectedNonAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t10,t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t10"/>
            <thread-pool thread-pool-id="t1"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
      <!-- This test result is invalid - we should distinguish this
      case, but don't!!! -->
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="4"
              expectedNonAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"
                                   thread-pool-ids="t10 t1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t10"/>
            <thread-pool thread-pool-id="t1"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
      <t:test ruleId="r269" assertionId="threadPoolsExist" id="5"
              expectedNonAssertions="1"> 
        <resources>
          <resource-adapter-config resource-adapter-name="ra1"/>
        </resources>
        <configs>
          <config name="config1">
            <thread-pool thread-pool-id="t10"/>
          </config>
        </configs>
        <servers>
          <server config-ref="config1">
            <resource-ref ref="ra1"/>
          </server>
        </servers>
      </t:test>
    </sch:rule>

    <sch:rule id="r270"	context="resource-ref">
      <sch:extends rule="ref-IsPrimaryKey"/>
      <sch:assert
       id="resourceRef"
       test="not(contains('1yestrueon', @enabled)) or key('resources', @ref)"
       diagnostics="invalid-reference">
        <code>resource-ref</code> elements which are enabled must refer to existing resources
      </sch:assert>
      <t:test ruleId="r270" id="1" assertionId="resourceRef" expectedNonAssertions="1">
        <domain>
          <server>
            <resource-ref ref="foo" enabled='yes'/>
          </server>
          <resources>
            <resource jndi-name="foo"/>
          </resources>
        </domain>
      </t:test>
      <t:test ruleId="r270" id="2" assertionId="resourceRef" expectedAssertions="1">
        <domain>
          <server>
            <resource-ref ref="far" enabled='yes'/>
          </server>
          <resources>
            <resource jndi-name="foo"/>
          </resources>
        </domain>
      </t:test>
      <p>
        Resources which have been deployed to a server must match the
        kind of server they have been deployed to as explained in the
        section <a href="#deployedObjects">Relationship between
        deployed objects and their servers</a>
      </p>
      <sch:assert id="adminResourcesOnDAS"
                  test="not(key('resources',@ref)[@object-type='system-admin'])
                  or
                  key('configs',current()/../@config-ref)/admin-service[@type!='server']
                  ">
        Administrative resources must only be deployed to a DAS server
      </sch:assert>
      <t:test ruleId="r270" id="1" assertionId="adminResourcesOnDAS" expectedNonAssertions="1">
        <servers>
          <server config-ref="config1">
            <resource-ref ref="app1"/>
          </server>
        </servers>
        <resources>
          <resource object-type="system-admin" name="app1"/>
        </resources>
        <configs>
          <config name="config1">
            <admin-service type="das"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r270" id="2" assertionId="adminResourcesOnDAS" expectedAssertions="1">
        <servers>
          <server config-ref="config2">
            <resource-ref ref="app2"/>
          </server>
        </servers>
        <resources>
          <resource object-type="system-admin" jndi-name="app2"/>
        </resources>
        <configs>
          <config name="config2">
            <admin-service type="server"/>
          </config>
        </configs>
      </t:test>

      <sch:assert id="instanceResourcesOnServer"
                  test="not(key('resources',@ref)[@object-type='system-instance'])
                  or
                  key('configs',current()/../@config-ref)/admin-service[@type = 'server']
                  ">
        Instance resources must only be deployed to non-DAS servers
      </sch:assert>
      <t:test ruleId="r270" id="1" assertionId="instanceResourcesOnServer" expectedNonAssertions="1">
        <servers>
          <server config-ref="iaos-config1">
            <resource-ref ref="iaos-app1"/>
          </server>
        </servers>
        <resources>
          <resource object-type="system-instance" name="iaos-app1"/>
        </resources>
        <configs>
          <config name="iaos-config1">
            <admin-service type="server"/>
          </config>
        </configs>
      </t:test>
      <t:test ruleId="r270" id="2" assertionId="instanceResourcesOnServer" expectedAssertions="1">
        <servers>
          <server config-ref="iaos-config2">
            <resource-ref ref="iaos-app2"/>
          </server>
        </servers>
        <resources>
          <resource object-type="system-instance" name="iaos-app2"/>
        </resources>
        <configs>
          <config name="iaos-config2">
            <admin-service type="das-and-server"/>
          </config>
        </configs>
      </t:test>
    </sch:rule>

    <sch:rule id="r271"	context="resources">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r272"	context="security-map">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r273"	context="security-service">
      <sch:assert id="jaccProviderExists"
                  test="//jacc-provider[@name=current()/@jacc]">
        The referenced <code>jacc-provider</code> exists
      </sch:assert>
    </sch:rule>

    <sch:rule id="r274"	context="server">
      <sch:extends rule="name-IsPrimaryKey"/>
      <sch:extends rule="noRefsToDefaultConfig"/>
      <sch:extends rule='referencedConfigsMustExist'/>
      <t:test ruleId="r274" id="1" assertionId="referencedConfigsMustExist" expectedNonAssertions="1">
        <server config-ref="foo"/>
        <config name="foo"/>
      </t:test>
      <t:test ruleId="r274" id="2" assertionId="referencedConfigsMustExist" expectedAssertions="1">
        <server config-ref="foo"/>
        <config name="far"/>
      </t:test>
      <t:test ruleId="r274" id="3" assertionId="referencedConfigsMustExist" expectedNonAssertions="1">
        <server/>
        <config name="far"/>
      </t:test>

      <p>
        A server which is running the DAS cannot also run a node agent
      </p>
      <!--
        we've used a not(x[a!=b]) form here, rather than the simpler
        x[a=b] simply because the internal expression more closely
        corresponds to the concept "a server running the DAS" (i.e. a
        server not running as a server)
      -->
      <sch:assert id="dasServerRefsNoNodeAgents"
                  test="
                  not(//config[@name=//server[@name=current()/@name][not(//server-ref/@ref=current()/@name)]/@config-ref]/admin-service[@type!='server'] |
                      //config[@name=//cluster[server-ref/@ref=current()/@name]/@config-ref]/admin-service[@type!='server']) 
                       or not(@node-agent-ref)">
        <sch:name/> elements which are configured to run the das do
        not also run a node agent
      </sch:assert>
      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="7" expectedNonAssertions="1">
        <domain>
          <cluster config-ref="config12">
            <server-ref ref="server1"/>
          </cluster>
          <servers>
            <server name="server1" config-ref="config2"
                    node-agent-ref="na1"/>
          </servers>
          <configs>
            <config name="config1">
              <admin-service type="das-and-server"/>
            </config>
            <config name="config2">
              <admin-service type="server"/>
            </config>
          </configs>
        </domain>
      </t:test>
      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="6" expectedNonAssertions="1">
        <domain>
          <servers>
            <server name="server1" config-ref="config32"
                    node-agent-ref="na1"/>
          </servers>
        </domain>
      </t:test>

      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="5" expectedNonAssertions="1">
        <domain>
          <cluster config-ref="config2">
            <server-ref ref="server1"/>
          </cluster>
          <servers>
            <server name="server1" config-ref="config1"
                    node-agent-ref="na1"/>
          </servers>
          <configs>
            <config name="config1">
              <admin-service type="das-and-server"/>
            </config>
            <config name="config2">
              <admin-service type="server"/>
            </config>
          </configs>
        </domain>
      </t:test>
      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="4" expectedAssertions="1">
        <domain>
          <cluster config-ref="config1">
            <server-ref ref="server1"/>
          </cluster>
          <servers>
            <server name="server1" config-ref="config2"
                    node-agent-ref="na1"/>
          </servers>
          <configs>
            <config name="config1">
              <admin-service type="das-and-server"/>
            </config>
            <config name="config2">
              <admin-service type="server"/>
            </config>
          </configs>
        </domain>
      </t:test>

      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="3" expectedNonAssertions="1">
        <domain>
          <server name='server1' config-ref="foo" node-agent-ref="na1"/>
          <config name="foo">
            <admin-service type="server"/>
          </config>
        </domain>
      </t:test>
      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="2" expectedNonAssertions="1">
        <domain>
          <server name='server1' config-ref="foo"/>
          <config name="foo">
            <admin-service type="das-and-server"/>
          </config>
        </domain>
      </t:test>
      <t:test ruleId="r274" assertionId="dasServerRefsNoNodeAgents"
              id="1" expectedAssertions="1">
        <domain>
          <server name='server1' config-ref="foo" node-agent-ref="na1"/>
          <config name="foo">
            <admin-service type="das-and-server"/>
          </config>
        </domain>
      </t:test>
      <sch:assert id="nodeAgentsExist"
                  test="not(@node-agent-ref) or //node-agent[@name=current()/@node-agent-ref]" diagnostics="non-existant-node-agent-ref">
        <sch:name/> elements which refer to a <code>node-agent</code> must reference an existing <code>node-agent</code>
      </sch:assert>
      <sch:assert id="notReferencedByTwoClusters"
                  test="count(//cluster[server-ref/@ref=current()/@name]) &lt; 2">
        <sch:name/> elements are not referenced by more than one
        <code>cluster</code> element.
      </sch:assert>
    </sch:rule>


    <sch:rule id="r275"	context="server-ref">
      <sch:extends rule="ref-IsPrimaryKey"/>
      <t:test ruleId="r275" assertionId="refPrimaryKey" id='1'
              expectedAssertions='1' expectedNonAssertions="1">
        <server-ref ref="foo"/>
        <server-ref ref="foo"/>
      </t:test>
      <t:test ruleId="r275" assertionId="refPrimaryKey" id='2' expectedNonAssertions='2'>
        <server-ref ref="foo"/>
        <server-ref ref="fee"/>
      </t:test>
      <sch:assert id="serverRefsExist"
                  test="//server[@name=current()/@ref]">
        <sch:name/> elements refer to existing
        <code>server</code> elements
      </sch:assert>
      <t:test ruleId="r275" assertionId="serverRefsExist" id="1" expectedNonAssertions="1">
        <server-ref ref="1"/>
        <server name="1"/>
      </t:test>
      <t:test ruleId="r275" assertionId="serverRefsExist" id="2" expectedAssertions="1">
        <server-ref ref="1"/>
        <server name="3"/>
      </t:test>
      <t:test ruleId="r275" assertionId="serverRefsExist" id="3" expectedAssertions="1">
        <server-ref ref="1"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r276"	context="servers">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r277"	context="session-config">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r278"	context="session-manager">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r279"	context="session-properties">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r280"	context="ssl">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r281"	context="ssl-client-config">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r282"	context="store-properties">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r283"	context="system-property">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

    <sch:rule id="r284"	context="thread-pool">
      <sch:assert id="thread-pool-id-IsPrimaryKey"
                  test="not(preceding-sibling::thread-pool[@thread-pool-id=current()/@thread-pool-id])"
                  diagnostics="threadPoolPrimaryKey">
        Sibling <sch:name/> elements are uniquely named by their <code>thread-pool-id</code> attribute.
      </sch:assert>
      <t:test ruleId="r284" id='1' assertionId="thread-pool-id-IsPrimaryKey" expectedNonAssertions="2">
        <thread-pool thread-pool-id="foo"/>
        <other-element thread-pool-id="far"/>
        <thread-pool thread-pool-id="far"/>
      </t:test>
      <t:test ruleId="r284" id='2' assertionId="thread-pool-id-IsPrimaryKey" expectedAssertions="1" expectedNonAssertions='1'>
        <thread-pool thread-pool-id="foo"/>
        <thread-pool thread-pool-id="foo"/>
      </t:test>

      <sch:assert id="minLessThanMax"
                  test="@min-thread-pool-size &lt; @max-thread-pool-size">
        Within a <sch:name/> element the
        <code>min-thread-pool-size</code> is less than the <code>max-thread-pool-size</code>
      </sch:assert>
    </sch:rule>

    <sch:rule id="r285"	context="thread-pools">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r286"	context="transaction-service">
      <sch:assert test="true()"/>
    </sch:rule>

    <sch:rule id="r287"	context="user-group">
      <sch:assert test="true()"/>
    </sch:rule>


    <sch:rule id="r288"	context="virtual-server">
      <sch:extends rule="id-IsPrimaryKey"/>
      <sch:assert id="httpListenersExist"
                  test="count(../http-listener[contains(concat(normalize-space(current()/@http-listeners),','),
                                                        concat(normalize-space(@id),','))])
                  =
                  string-length(translate(normalize-space(current()/@http-listeners),
                  translate(normalize-space(current()/@http-listeners), ',', ''),
                  '')) + 1">
        Every <code>http-listener</code>
        within the list of http listeners exists as a sibling of the
        <sch:name/> element
      </sch:assert>
      <t:test ruleId="r288" assertionId="httpListenersExist" id="1" expectedAssertions='1'>
        <virtual-server http-listeners='1'/>
        <http-listener id='2'/>
      </t:test>
      <t:test ruleId="r288" assertionId="httpListenersExist" id="2" expectedNonAssertions='1'>
        <virtual-server http-listeners='1'/>
        <http-listener id='1'/>
      </t:test>
      <t:test ruleId="r288" assertionId="httpListenersExist" id="3" expectedAssertions='1'>
        <virtual-server http-listeners='1, 2'/>
        <http-listener id='1'/>
      </t:test>
      <t:test ruleId="r288" assertionId="httpListenersExist" id="4" expectedNonAssertions='1'>
        <virtual-server http-listeners='1, 2'/>
        <http-listener id='1'/>
        <http-listener id='2'/>
        <http-listener id='3'/>
      </t:test>

      <sch:assert id="webModuleExists"
                  test="not(@default-web-module) or //web-module[@name=current()/@default-web-module]">
        Within a <sch:name/> elment if the <code>default-web-module</code> attribute has a value
        then that value must be the name of an existing <code>web-module</code>
      </sch:assert>

      <sch:assert id="webModuleCorrectlyDeployed"
                  test="not(string(normalize-space(@default-web-module))) or
                  //cluster[@config-ref=current()/ancestor::config/@name]/application-ref[@ref=current()/@default-web-module] |
                  //server[not(@name=//server-ref/@ref)][@config-ref=current()/ancestor::config/@name]/application-ref[@ref=current()/@default-web-module]" diagnostics="virtualServerLocation webModuleCorrectlyDeployed">
        The <code>default-web-module</code> has been deployed to every
        server and cluster which uses the containing
        <code>config</code>
      </sch:assert>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="1" expectedNonAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server default-web-module='wm1'/>
          </http-service>
        </config>
        <cluster config-ref="config">
          <application-ref ref="wm1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="2" expectedNonAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server default-web-module='wm1'/>
          </http-service>
        </config>
        <server config-ref="config">
          <application-ref ref="wm1"/>
        </server>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="3" expectedAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server default-web-module='wm2'/>
          </http-service>
        </config>
        <server config-ref="config">
          <application-ref ref="wm1"/>
        </server>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="4" expectedAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server default-web-module='wm2'/>
          </http-service>
        </config>
        <cluster config-ref="config">
          <application-ref ref="wm1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="5" expectedAssertions="1">
        <config name="config1">
          <http-service>
            <virtual-server default-web-module='wm1'/>
          </http-service>
        </config>
        <cluster config-ref="config">
          <application-ref ref="wm1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="6" expectedAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server default-web-module='wm1'/>
          </http-service>
        </config>
        <cluster config-ref="config1">
          <server-ref ref="server1"/>
          <application-ref ref="wm1"/>
        </cluster>
        <server name='server1' config-ref="config">
          <application-ref ref="wm1"/>
        </server>
      </t:test>
      <t:test ruleId="r288" assertionId="webModuleCorrectlyDeployed"
              id="7" expectedNonAssertions="1">
        <config name="config">
          <http-service>
            <virtual-server/>
          </http-service>
        </config>
        <cluster config-ref="config1">
          <server-ref ref="server1"/>
          <application-ref ref="wm1"/>
        </cluster>
        <server name='server1' config-ref="config">
          <application-ref ref="wm1"/>
        </server>
      </t:test>
    </sch:rule>


    <sch:rule id="r289"	context="web-container">
      <sch:assert test="true()"/>
    </sch:rule>
    
    <sch:rule id="r290" context="web-container-availability">
      <sch:assert
       id="parentPoolExists"
       test="not(contains('1yestrueon', ../@availability-enabled) and
                 (not(string(@availability-enabled)) or
                 contains('1yestrueon', @availability-enabled)) and
                 not(string(normalize-space(@http-session-store-pool-name))))
             or key('jdbc-resources', ../@store-pool-name)">
        The <code>store-pool-name</code> attribute of the parent of an
        <sch:name/> element must reference an existing
        <code>jdbc-resource</code> when the <sch:name/> element is
        enabled but doesn't have a <code>sjsb-store-pool-name</code>
        attribute.
      </sch:assert>
      <t:test ruleId="r290" assertionId="parentPoolExists" id="1"
              expectedAssertions="1">
        <availability-service availability-enabled='1'>
          <web-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r290" assertionId="parentPoolExists" id="2"
              expectedNonAssertions="1">
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='1' store-pool-name='foo'>
          <web-container-availability/>
        </availability-service>
      </t:test>
      <sch:assert
        id="poolMustExist"
        test="not(contains('1yestrueon', ../@availability-enabled) and 
                  (not(string(@availability-enabled)) or contains('1yestrueon', @availability-enabled))
                  and string(normalize-space(@http-session-store-pool-name)))
              or key('jdbc-resources',@http-session-store-pool-name)">
        A <sch:name/> element whose parent is enabled, which is
        enabled itself, and which has a non-empty
        <code>http-session-store-pool-name</code> attribute references
        an existing <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r290" assertionId="poolMustExist" id='1'
              expectedAssertions="1">
        <availability-service availability-enabled='1'>
          <web-container-availability
           http-session-store-pool-name="foo" availability-enabled='on'/>
        </availability-service>
        <jdbc-resource jndi-name="it ain't me, babe"/>
      </t:test>
      <t:test ruleId="r290" assertionId="poolMustExist" id='2'
              expectedNonAssertions="1">
        <availability-service availability-enabled='yes'>
            <web-container-availability availability-enabled='1' http-session-store-pool-name="foo"/>
        </availability-service>
        <jdbc-resource jndi-name="foo"/>
      </t:test>
      <t:test ruleId="r290" assertionId="poolMustExist" id='3'
              expectedNonAssertions="1">
        <availability-service availability-enabled='0'>
            <web-container-availability availability-enabled='1' http-session-store-pool-name="foo"/>
        </availability-service>
        <jdbc-resource jndi-name="bar"/>
      </t:test>
      
    </sch:rule>

    <sch:rule id="r291"	context="web-module">
      <sch:extends rule="name-IsPrimaryKey"/>
    </sch:rule>

  </sch:pattern>

  <sch:pattern name="Namespaces">
    <sch:p>
      <p>
        These rules establish collections of element kinds which are
        related simply by the fact that within the collection the
        elements must be uniquely named. Such a collection is known as a
        <code>namespace</code>. A kind of element can only be a
        member of a single namespace.
      </p>
      <p>
        There are three namespaces:
        <table cellpadding="2" cellspacing="2" border="1" style="width: 100%;">
          <tbody>
            <tr>
              <th style="vertical-align: top;">Namespace<br/>
              </th>
              <th style="vertical-align: top;">Description<br/>
              </th>
            </tr>
            <tr>
              <td style="vertical-align: top;">Applications<br/>
              </td>
              <td style="vertical-align: top;">All the children of the
              <tt>applications </tt>element, named by their <tt>name
              </tt>attribute.<br/>
              </td>
            </tr>
            <tr>
              <td style="vertical-align: top;">Resources<br/>
              </td>
              <td style="vertical-align: top;">All the children of the
              <tt>resources </tt>element, named by their <tt>name</tt>,
              <tt>jndi-name</tt> or <tt>resource-adapter-name</tt>
              attributes.<br/>
              </td>
            </tr>
            <tr>
              <td style="vertical-align: top;">System<br/>
              </td>
              <td style="vertical-align: top;">All the <tt>cluster</tt>,
              <tt>config</tt>, <tt>lb-config</tt>, <tt>node-agent</tt>
              and <tt>server</tt> elements, named by their <tt>name
              </tt>attribute.<br/>
              </td>
            </tr>
          </tbody>
        </table>
      </p>
    </sch:p>
    <sch:rule id="r11" context="applications/*">
      <sch:key name="applications" path="@name"/>
      <sch:assert id="applicationNamespace"
                  test="count(key('applications', @name)) = 1" diagnostics="applicationNamespace">
        Applications are uniquely named by their <code>name</code> attribute.
      </sch:assert>
      <t:test ruleId="r11" id="1" assertionId="applicationNamespace" expectedNonAssertions="1">
        <applications>
          <foobar name="1"/>
        </applications>
      </t:test>
      <t:test ruleId="r11" id="2" assertionId="applicationNamespace"
              expectedNonAssertions="0" expectedAssertions="2">
        <applications>
          <f name="1"/>
          <f name="1"/>
        </applications>
      </t:test>
      <t:test ruleId="r11" id="3" assertionId="applicationNamespace" expectedNonAssertions="2">
        <applications>
          <f name="1"/>
          <f name="2"/>
        </applications>
      </t:test>
      <t:test ruleId="r11" id="4" assertionId="applicationNamespace"
              expectedNonAssertions="0" expectedAssertions="2">
        <applications>
          <f name='1'/>
          <x name='1'/>
        </applications>
      </t:test>
    </sch:rule>
    <!-- resources are named using any of the jndi-name, name
         or resource-adapter-name attributes.

         We exclude resources of the same kind because that's caught
         by other patterns.

    -->
    <sch:rule id="r12" context="resources/*">
      <sch:key name="resources" path="@jndi-name |
               @resource-adapter-name | @name"/>

      <sch:assert id="resourceNamespace"
                  test="count(key('resources', @name) |
                  key('resources', @jndi-name) | key('resources',
                  @resource-adapter-name)) = 1">
        Resources are uniquely named by either their <code>name</code>, <code>jndi-name</code> or <code>resource-adapter-name</code> attribute.
      </sch:assert>
      <t:test ruleId="r12" id="1" assertionId="resourceNamespace" expectedNonAssertions="3">
        <resources>
          <a name="1"/>
          <b jndi-name="2"/>
          <c resource-adapter-name="3"/>
        </resources>
      </t:test>
      <t:test ruleId="r12" id="2" assertionId="resourceNamespace"
              expectedNonAssertions="0" expectedAssertions="3">
        <resources>
          <a name="1"/>
          <b jndi-name="1"/>
          <c resource-adapter-name="1"/>
        </resources>
      </t:test>
      <t:test ruleId="r12" id="3" assertionId="resourceNamespace" expectedAssertions="2">
        <resources>
          <a name='1'/>
          <a name='1'/>
        </resources>
      </t:test>
    </sch:rule>

    
    <sch:rule id="r13" context="configs/* | servers/* |
              clusters/* | node-agents/* | lb-configs/*">
      <sch:key name="system-objects" path="@name"/>
      <sch:assert id="systemNamespace"
                  test="count(key('system-objects', @name)) = 1"
                  diagnostics="systemNamespace">
        System objects are uniquely named by their <code>name</code> attribute.
      </sch:assert>
      <t:test ruleId="r13" id="1" assertionId="systemNamespace" expectedNonAssertions="1">
        <doc>
          <clusters>
            <foo name="1f"/>
          </clusters>
        </doc>
      </t:test>
      <t:test ruleId="r13" id="2" assertionId="systemNamespace" expectedNonAssertions="2">
        <doc>
          <clusters>
            <foo name="1f"/>
            <bar name="2f"/>
          </clusters>
        </doc>
      </t:test>
      <t:test ruleId="r13" id="3" assertionId="systemNamespace"
              expectedNonAssertions="0" expectedAssertions="2">
        <doc>
          <clusters>
            <foo name="4f"/>
            <bar name="4f"/>
          </clusters>
        </doc>
      </t:test>
      <t:test ruleId="r13" id="4" assertionId="systemNamespace"
              expectedAssertions="0" expectedNonAssertions="2">
        <doc>
          <clusters>
            <foo name="5f"/>
          </clusters>
          <configs>
            <bar name="6f"/>
          </configs>
        </doc>
      </t:test>
      <t:test ruleId="r13" id="5" assertionId="systemNamespace"
              expectedNonAssertions="0" expectedAssertions="2">
        <doc>
          <clusters>
            <foo name="7f"/>
          </clusters>
          <configs>
            <bar name="7f"/>
          </configs>
        </doc>
      </t:test>
      <t:test ruleId="r13" id="6" assertionId="systemNamespace"
              expectedNonAssertions="0" expectedAssertions="2">
        <doc>
          <clusters>
            <foo name="7f"/>
          </clusters>
          <configs>
            <foo name="7f"/>
          </configs>
        </doc>
      </t:test>
    </sch:rule>
  </sch:pattern>

  <sch:pattern name="Warnings">
    <p>Warnings are emitted when the system can work as configured,
    but it would appear that the configuration might be incorrect. For
    example, if an application is not referenced from anywhere, then
    perhaps it should've been and there's been an act of omission.
    </p>
    <sch:rule id="r300" context="applications/*">
      <sch:assert id="everyApplicationIsReferenced"
                  test="//application-ref[@ref=current()/@name]">
        Every application is referenced by some <code>application-ref</code>
      </sch:assert>
      <t:test ruleId="r300" assertionId="everyApplicationIsReferenced"
              id="1" expectedAssertions='1'>
        <applications>
          <application name="foo"/>
        </applications>
      </t:test>
      <t:test ruleId="r300" assertionId="everyApplicationIsReferenced"
              id="2" expectedNonAssertions='1'>
        <applications>
          <application name="foo"/>
        </applications>
        <application-ref ref="foo"/>
      </t:test>
    </sch:rule>

    <sch:rule id="as-warnings" context="availability-service">
      <p>
        Although permitted by the DTD it is likely to be a
        configuration error if the availability-service has been
        enabled but doesn't have any availability children.
      </p>
      <sch:assert id="asHasNoChildren"
                  test="not(contains('1yestrueon', @availability-enabled))
                  or (ejb-container-availability | web-container-availability)">
        <sch:name/> elements which are enabled
        have children
      </sch:assert>
      <t:test ruleId="as-warnings" assertionId="asHasNoChildren"
              id="1" expectedNonAssertions='1'>
        <availability-service availability-enabled='false'/>
      </t:test>
      <t:test ruleId="as-warnings" assertionId="asHasNoChildren"
              id="3" expectedAssertions='1'>
        <availability-service availability-enabled='true'/>
      </t:test>
      <t:test ruleId="as-warnings" assertionId="asHasNoChildren"
              id="4" expectedAssertions='1'>
        <availability-service availability-enabled='true'/>
      </t:test>
      <t:test ruleId="as-warnings" assertionId="asHasNoChildren"
              id="5" expectedNonAssertions='1'>
        <availability-service availability-enabled='true'>
          <ejb-container-availability/>
        </availability-service>
      </t:test>
    </sch:rule>

    <sch:rule id="r301" context="config[not(@name='default-config')]">
      <sch:assert id="everyConfigIsReferenced"
                  test="//server[not(@name=//server-ref/@ref)][@config-ref=current()/@name] | //cluster[@config-ref=current()/@name]">
        Every <sch:name/> is referenced by either a
        <code>cluster</code>, or a <code>server</code> not in a
        <code>cluster</code>.
      </sch:assert>
      <t:test ruleId="r301" assertionId="everyConfigIsReferenced"
              id="1" expectedAssertions="1">
        <config name="foo"/>
        <cluster config-ref="fee"/>
      </t:test>
      <t:test ruleId="r301" assertionId="everyConfigIsReferenced"
              id="2" expectedNonAssertions="1">
        <config name="foo"/>
        <cluster config-ref="foo"/>
      </t:test>
      <t:test ruleId="r301" assertionId="everyConfigIsReferenced"
              id="3" expectedNonAssertions="1">
        <config name="foo"/>
        <server config-ref="foo"/>
      </t:test>
      <t:test ruleId="r301" assertionId="everyConfigIsReferenced"
              id="4" expectedAssertions="1">
        <config name="foo"/>
        <server name="server1" config-ref="foo"/>
        <cluster config-ref="fee">
          <server-ref ref="server1"/>
        </cluster>
      </t:test>
      <t:test ruleId="r301" assertionId="everyConfigIsReferenced"
              id="5" expectedNonAssertions="1">
        <config name="foo"/>
        <server name="server1" config-ref="fee"/>
        <cluster config-ref="foo">
          <server-ref ref="server1"/>
        </cluster>
      </t:test>
    </sch:rule>
    
    <sch:rule id="r302" context="ejb-container-availability">
      <p>
        Although an <code>ejb-container-availability</code> is
        disabled and doesn't have a value for its 
        <code>sfsb-store-pool-name</code> attfibute, if its parent
        element has a <code>store-pool-name</code> then the
        <code>store-pool-name</code> should reference an existing
        <code>jdbc-resource</code>
      </p>
      <sch:assert
       id="disabledParentPoolExists"
       test="not((contains('0noofffalse', ../@availability-enabled) or
                   (string(@availability-enabled) and
                    contains('0noofffalse', @availability-enabled))) 
                  and
                  (string(normalize-space(../@store-pool-name)) and
                   not(string(normalize-space(@sfsb-store-pool-name)))))
             or key('jdbc-resources', normalize-space(../@store-pool-name))">
        A disabled <sch:name/> element's
        parent's <code>store-pool-name</code> attribute should
        reference an existing <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r302" assertionId="disabledParentPoolExists" id="1" expectedAssertions='1'>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <ejb-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledParentPoolExists" id="2"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <ejb-container-availability />
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledParentPoolExists" id="3"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <ejb-container-availability availability-enabled='1' />
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledParentPoolExists" id="4"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <ejb-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledParentPoolExists" id="5"
              expectedAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="fee">
          <ejb-container-availability />
        </availability-service>
      </t:test>

      <p>
        Although an <code>ejb-container-availability</code> is
        disabled we want to warn if the
        <code>sfsb-store-pool-name</code> attfibute doesn't reference
        an existing <code>jdbc-resource</code>
      </p>
      <sch:assert
       id="disabledPoolExists"
       test="not((contains('0noofffalse', ../@availability-enabled) or
                   (string(@availability-enabled) and
                    contains('0noofffalse', @availability-enabled))) 
                  and
                 string(normalize-space(@sfsb-store-pool-name)))
             or key('jdbc-resources', normalize-space(@sfsb-store-pool-name))">
        A disabled <sch:name/> element's
        <code>sfsb-store-pool-name</code> references a known
        <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r302" assertionId="disabledPoolExists" id="1" expectedAssertions='1'>
        <availability-service availability-enabled='0'>
          <ejb-container-availability sfsb-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledPoolExists" id="2"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <ejb-container-availability sfsb-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledPoolExists" id="3"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <ejb-container-availability availability-enabled='1' sfsb-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledPoolExists" id="4"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <ejb-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r302" assertionId="disabledPoolExists" id="5"
              expectedAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <ejb-container-availability sfsb-store-pool-name="fee"/>
        </availability-service>
      </t:test>
    </sch:rule>
    
    <sch:rule id="r263"
              context="persistence-manager-factory-resource">
      <sch:extends rule="jndi-name-IsPrimaryKey"/>
      <p>
        It is only a warning if the referenced jdbc-resource doesn't
        exist since the application server runs, but any associated
        application will fail.
      </p>
      <sch:assert id="jdbcResourceExists"
                  test="not(@jdbc-resource-jndi-name) or key('jdbc-resources',@jdbc-resource-jndi-name)">
        If a <sch:name/> element references a jdbc resource then that jdbc resource must exist
      </sch:assert>
      <t:test ruleId="r263" assertionId="jdbcResourceExists" id="1" expectedAssertions="1">
        <persistence-manager-factory-resource
         jdbc-resource-jndi-name="foo"/>
        <jdbc-resource jndi-name="not me!"/>
      </t:test>
      <t:test ruleId="r263" assertionId="jdbcResourceExists" id="2" expectedNonAssertions="1">
        <persistence-manager-factory-resource
         jdbc-resource-jndi-name="foo"/>
        <jdbc-resource jndi-name="foo"/>
      </t:test>
      <t:test ruleId="r263" assertionId="jdbcResourceExists" id="3" expectedNonAssertions="1">
        <persistence-manager-factory-resource/>
        <jdbc-resource jndi-name="who cares"/>
      </t:test>
    </sch:rule>

    <sch:rule id="r303" context="web-container-availability">
      <p>
        Although an <code>web-container-availability</code> is
        disabled and doesn't have a value for its 
        <code>http-session-store-pool-name</code> attfibute, if its parent
        element has a <code>store-pool-name</code> then the
        <code>store-pool-name</code> should reference an existing
        <code>jdbc-resource</code>
      </p>
      <sch:assert
       id="disabledParentPoolExists"
       test="not((contains('0noofffalse', ../@availability-enabled) or
                   (string(@availability-enabled) and
                    contains('0noofffalse', @availability-enabled))) 
                  and
                  (string(normalize-space(../@store-pool-name)) and
                   not(string(normalize-space(@http-session-store-pool-name)))))
             or key('jdbc-resources', normalize-space(../@store-pool-name))">
        A disabled <sch:name/> element's parent's
        <code>store-pool-name</code> attribute should reference an
        existing <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r303" assertionId="disabledParentPoolExists" id="1" expectedAssertions='1'>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <web-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledParentPoolExists" id="2"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <web-container-availability />
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledParentPoolExists" id="3"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="foo">
          <web-container-availability availability-enabled='1' />
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledParentPoolExists" id="4"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <web-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledParentPoolExists" id="5"
              expectedAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0' store-pool-name="fee">
          <web-container-availability />
        </availability-service>
      </t:test>

      <p>
        Although an <code>web-container-availability</code> is
        disabled we want to warn if the
        <code>http-session-store-pool-name</code> attfibute doesn't reference
        an existing <code>jdbc-resource</code>
      </p>
      <sch:assert
       id="disabledPoolExists"
       test="not((contains('0noofffalse', ../@availability-enabled) or
                   (string(@availability-enabled) and
                    contains('0noofffalse', @availability-enabled))) 
                  and
                 string(normalize-space(@http-session-store-pool-name)))
             or key('jdbc-resources', normalize-space(@http-session-store-pool-name))">
        A disabled <sch:name/> element's
        <code>http-session-store-pool-name</code> references a known
        <code>jdbc-resource</code>
      </sch:assert>
      <t:test ruleId="r303" assertionId="disabledPoolExists" id="1" expectedAssertions='1'>
        <availability-service availability-enabled='0'>
          <web-container-availability http-session-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledPoolExists" id="2"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <web-container-availability http-session-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledPoolExists" id="3"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <web-container-availability availability-enabled='1' http-session-store-pool-name="foo"/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledPoolExists" id="4"
              expectedNonAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <web-container-availability/>
        </availability-service>
      </t:test>
      <t:test ruleId="r303" assertionId="disabledPoolExists" id="5"
              expectedAssertions='1'>
        <jdbc-resource jndi-name="foo"/>
        <availability-service availability-enabled='0'>
          <web-container-availability http-session-store-pool-name="fee"/>
        </availability-service>
      </t:test>
    </sch:rule>
      
    <sch:rule id="r304" context="resources/*">
      <sch:assert id="everyResourceIsReferenced"
                  test="//resource-ref[@ref=(current()/@name |
                  current()/@jndi-name | current()/resource-adapter-name)]">
        Every resource is referenced by some <code>resource-ref</code>
      </sch:assert>
      <t:test ruleId="r304" assertionId="everyResourceIsReferenced"
              id="1" expectedAssertions='1'>
        <resources>
          <resource name="foo"/>
        </resources>
      </t:test>
      <t:test ruleId="r304" assertionId="everyResourceIsReferenced"
              id="2" expectedNonAssertions='1'>
        <resources>
          <resource jndi-name="foo"/>
        </resources>
        <resource-ref ref="foo"/>
      </t:test>
    </sch:rule>

    <p>
      To be implemented:
      <ul>
        <li>availability - when the service is disabled and the
        resource doesn't exist</li>
        <li>for any referencing element that can be enabled - its an
        error when enabled and the referenced item doesn't exist. Its
        a warning when not enabled and the referenced item doesn't exist</li>
        <li>resource-adapter-config - the resource-adapter-name
        attribute must be the name of an existing
        connector-module</li>
        <li>persistence-manager-factory-resource - if there's a
        jdbc-resource-jndi-name defined then that resource should be
        bound by the binding server/cluster</li>
      </ul>
    </p>
  </sch:pattern>


  <sch:diagnostics>
    <sch:diagnostic id="orbLocation">
      Location: config name=<sch:value-of select="ancestor::config/@name"/> orb @use-thread-pool-ids= <sch:value-of select="@use-thread-pool-ids"/>
    </sch:diagnostic>
    <sch:diagnostic id="virtualServerLocation">
      Location: config name=<sch:value-of select="ancestor::config/@name"/> virtual-server name=<sch:value-of select="@id"/>
    </sch:diagnostic>
    <sch:diagnostic id="webModuleCorrectlyDeployed">
      default-web-module=<sch:value-of select="@default-web-module"/> has not been deployed to any server or cluster referenced by the containing config.
    </sch:diagnostic>
    <sch:diagnostic id="namePrimaryKey">
      Duplicate name: <sch:value-of select="@name"/>
    </sch:diagnostic>
    <sch:diagnostic id="jndiPrimaryKey">
      Duplicate name: <sch:value-of select="@jndi-name"/>
    </sch:diagnostic>
    <sch:diagnostic id="refPrimaryKey">
      Duplicate name: <sch:value-of select="@ref"/>
    </sch:diagnostic>
    <sch:diagnostic id="idPrimaryKey">
      Duplicate name: <sch:value-of select="@id"/>
    </sch:diagnostic>
    <sch:diagnostic id="resourceAdapterNamePrimaryKey">
      Duplicate name: <sch:value-of select="@resource-adapter-name"/>
    </sch:diagnostic>
    <sch:diagnostic id="threadPoolPrimaryKey">
      Duplicate name: <sch:value-of select="@thread-pool-id"/>
    </sch:diagnostic>
    <sch:diagnostic id="invalid-reference">
      Reference "<sch:value-of select="@ref"/>" not found.
    </sch:diagnostic>
    <sch:diagnostic id="applicationNamespace">
      Two or more applications under the &lt;applications&gt; element have the same name:<sch:value-of select="@name"/>      
    </sch:diagnostic>
    <sch:diagnostic id="resourceNamespace">
      Two or more resources under the &lt;resources&gt; element have the same name:<sch:value-of select="@name|jndi-name|resource-adapter-name"/>  
    </sch:diagnostic>
    <sch:diagnostic id="systemNamespace">
      Two or more system objects (clusters, configs, lb-configs, node-agents servers) have the same name:<sch:value-of select="@name"/>
    </sch:diagnostic>
    <sch:diagnostic id="non-existant-virtual-server-ref">
      This http-listener (id=<sch:value-of select="@id"/>) refers to a
      default-virtual-server (<sch:value-of select="@default-virtual-server"/>)
      but this server is not part of this config (<sch:value-of select="../../@name"/>) 
    </sch:diagnostic>
    <sch:diagnostic id="ejb-attrs1">
      The steady-pool-size is: <sch:value-of select="@steady-pool-size"/>. &#xa; It should be no
      greater than the max-pool-size, which is: <sch:value-of select="@max-pool-size"/> 
    </sch:diagnostic>
    <sch:diagnostic id="uniqueNames">
      At least one other <sch:value-of select="local-name(.)"/>
      element has an identical name: <sch:value-of select="@name"/>.
      Within the <sch:value-of select="local-name(..)"/> element names
      must be unique.
    </sch:diagnostic>
    <sch:diagnostic id="uniquekey-diagnostic">
      The name <sch:value-of select="./@name | ./@jndi-name |
      ./@resource-adapter-name"/> is not unique. Within the
      resource elements the attributes <code>jndi-name, name &amp;
      resource-adapter-name</code> form a key, and these keys must
      have unique values.
    </sch:diagnostic>
    <sch:diagnostic id="configNamespace">
      The name "<sch:value-of select="@name"/>" is being used by at
      least two elements from the set {config, cluster, server, node-agent,
      lb-config}.
    </sch:diagnostic>
    <sch:diagnostic id="admin-service-diagnostic">
      There must be one (and only one) admin-service for the DAS. An
      admin-service for the DAS is an admin-service element whose
      type attribute has the value <code>"das"</code> or <code>"das-and-server"</code>
    </sch:diagnostic>
    <sch:diagnostic id="non-existant-node-agent-ref">
      This server ("<sch:value-of select="@name"/>") is referring to an
      unknown node-agent "<sch:value-of select="@node-agent-ref"/>"
    </sch:diagnostic>
    <sch:diagnostic id="non-existant-config-ref">
      This server ("<sch:value-of select="@name"/>") is referring to an
      unknown config "<sch:value-of select="@config"/>"
    </sch:diagnostic>
  </sch:diagnostics>

  <a name="deployedObjects"/>
  <h2>Relationship between deployed objects and their servers</h2>
  <p>
    Deployed objects are of two types - applications and
    resources. However, independent of this division, deployed objects
    can also be divided into whether they're designed for
    administrative use only, end-user use only, both administrative and
    end-user uses, or for some user-defined use. These are encoded as
    one of the following object-types, respectively:
  </p>
  <dl>
    <dt><code>system-all</code></dt>
    <dd>type suitable for both servers and DAS</dd>
    <dt><code>system-admin</code></dt>
    <dd>type suitable only for the DAS</dd>
    <dt><code>system-instance</code></dt>
    <dd>type suitable only for instances (and not DAS)</dd>
    <dt><code>user</code></dt>
    <dd>user type</dd>
  </dl>
  <p>
    Servers can be configured to support different uses. Either a
    server is only to support DAS functionality, or its a normal
    server supporting end-user functionality only, or it can support
    both. These different uses are encoded as one of the following
    types, respectively:
  </p>
  <dl>
    <dt><code>das</code></dt><dd>DAS use only</dd>
    <dt><code>server</code></dt><dd>End-user use only</dd>
    <dt><code>das-and-server</code></dt><dd>Both DAS and end-user use</dd>
  </dl>
  <p>
    Only certain combinations of deployed object type and server type
    are permitted, as shown in the following table:
  </p>
  <table cellpadding="2" cellspacing="2" border="1" width="100%">
    <tbody>
      <tr>
        <td valign="top"><br/>
        </td>
        <th valign="top" colspan="3">type<br/>
        </th>
      </tr>
      <tr>
        <th valign="top">object-type<br/>
        </th>
        <th valign="top">das<br/>
        </th>
        <th valign="top">das-and-server<br/>
        </th>
        <th valign="top">server<br/>
        </th>
      </tr>
      <tr>
        <th valign="top">system-all<br/>
        </th>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
      </tr>
      <tr>
        <th valign="top">system-instance<br/>
        </th>
        <td valign="top">Not OK<br/>
        </td>
        <td valign="top">Not OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
      </tr>
      <tr>
        <th valign="top">system-admin<br/>
        </th>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">Not OK<br/>
        </td>
      </tr>
      <tr>
        <th valign="top">user<br/>
        </th>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
        <td valign="top">OK<br/>
        </td>
      </tr>
    </tbody>
  </table>
  <p>
    This table can be simplified thus:
  </p>
  <p>
    <code>object-type = system-instance => type = server</code> <br/>
    <code>object-type = system-admin => not(type = server)</code>
  </p>
</sch:schema>


