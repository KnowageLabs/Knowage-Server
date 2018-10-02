#<a name="top"></a>Sanity check procedures

* [End to End Testing](#end-to-end-testing)
* [List of Running Processes](#list-of-running-processes)
* [Network Interfaces Up and Open](#network-interfaces-up-and-open)
* [Databases](#databases)

The Sanity Check Procedures are the steps that a System Administrator will take to verify that an installation is
ready to be tested. This is therefore a preliminary set of tests to ensure that obvious or basic malfunctioning
is fixed.

## End to End Testing

*   Start Knowage on default port (8080)
*   Open Knowage on your browser at url

> [http://localhost:8080/knowage](http://localhost:8080/knowage)

*   Check that you see the login page:

![](media/Knowage_Login.png)



[Top](#top)

## List of Running Processes

A java Tomcat process should be up and running, e.g.:

```
$ ps aux | grep java
root        15  1.3 18.4 2034464 745708 ?      Sl   08:55   2:34 /usr/bin/java -Djava.util.logging.config.file=/usr/local/tomcat/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.endorsed.dirs=/usr/local/tomcat/endorsed -classpath /usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat-juli.jar -Dcatalina.base=/usr/local/tomcat -Dcatalina.home=/usr/local/tomcat -Djava.io.tmpdir=/usr/local/tomcat/temp org.apache.catalina.startup.Bootstrap start
```

[Top](#top)

## Network Interfaces Up and Open

Knowage uses Servlet Application Container (Tomcat), 8080 is the default port.

[Top](#top)

## Databases

Knowage uses a JDBC connection therefore it can uses every Database with a JDBC driver. The All In One version contains a [HSQL DB](http://hsqldb.org), inside a folder. It runs when Knowage runs. Other versions run normally with an external database, (e.g MySQL DB). To check if a MySQL instance is running with Knowage database run these commands:

Run mysql client:

```
$ mysql -u root -p 
```

Show the tables of knowage database:

```
mysql> use knowage; show tables;
+----------------------------------+
| Tables_in_knowage                |
+----------------------------------+
| SBI_ALERT                        |
| SBI_ALERT_ACTION                 |
| SBI_ALERT_LISTENER               |
...
```

[Top](#top)
