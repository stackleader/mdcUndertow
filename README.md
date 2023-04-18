Sure! Here's the response converted to Markdown format for a GitHub `README.md` file:

```markdown
## Installing mdc-undertow-ext-3.0.1.jar as a global module in JBoss EAP 7

Follow these steps to install the `mdc-undertow-ext-3.0.1.jar` as a global module in JBoss EAP 7 (WildFly 10) using the JBoss CLI:

### 1. Start the JBoss CLI

Navigate to the `bin` directory in your JBoss EAP 7 installation and run the `jboss-cli` script (use `jboss-cli.bat` on Windows or `jboss-cli.sh` on Linux/macOS).

### 2. Connect to the running JBoss instance

In the JBoss CLI, enter the following command to connect to the running JBoss instance:

```
connect
```

### 3. Add the module using the `module add` command

In the JBoss CLI, use the `module add` command to create the module and add the JAR file as a resource. Make sure to specify the required dependencies as well. Replace `/path/to/mdc-undertow-ext-3.0.1.jar` with the actual path to the JAR file:

```
module add --name=com.rhc.mdc --resources=/path/to/mdc-undertow-ext-3.0.1.jar --dependencies=org.slf4j,io.undertow.core,javax.api,org.jboss.logging,org.jboss.modules,com.google.guava
```

### 4. Register the module as a global module

In the JBoss CLI, enter the following command to register the module as a global module:

```
/subsystem=ee:write-attribute(name=global-modules,value=[{name=com.rhc.mdc}])
```

### 5. Modify the Undertow subsystem configuration

To make all applications benefit from the `CorrelationIdGenerator` and `WrappingMdcExecutor` classes, you need to modify the Undertow subsystem configuration. In the JBoss CLI, enter the following commands:

```
/subsystem=undertow/configuration=filter/custom-filter=correlation-id-generator:add(class-name=com.rhc.mdc.CorrelationIdGenerator,module=com.rhc.mdc)
/subsystem=undertow/server=default-server/host=default-host/filter-ref=correlation-id-generator:add
```

### 6. Reload the server

To apply the configuration changes, enter the following command in the JBoss CLI:

```
reload
```

After completing these steps, the `mdc-undertow-ext-3.0.1.jar` will be installed as a global module in JBoss EAP 7, and all applications deployed on the server will benefit from the `CorrelationIdGenerator` and `WrappingMdcExecutor` classes.
```
