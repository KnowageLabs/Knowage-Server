#<a name="top"></a>Problem diagnosis procedures

* [Resource Availability](#resource-availability)
* [Remote Service Access](#remote-service-access)
* [Resource consumption](#resource-consumption)
* [I/O Flows](#io-flows)
  
The Diagnosis Procedures are the first steps that a System Administrator
will take to locate the source of an error in SpagoBI. Once the nature of
the error is identified with these tests, the system admin will often
have to resort to more concrete and specific testing to pinpoint the
exact point of failure and a possible solution. Such specific testing is
out of the scope of this section.

Please report any bug or problem with SpagoBI by [opening and issue in JIRA](https://spagobi.eng.it/jira/secure/Dashboard.jspa).

## Resource Availability

Although we haven't done yet a precise profiling on SpagoBI, tests done in our development and testing environment show that
a host with 2 CPU cores and 8 GB RAM is the minimum configuration to run SpagoBI with MySQL server.

[Top](#top)

## Remote Service Access

SpagoBI can run *standalone*, it's a Business Intelligence Application and it can grab the data from different data sources. However, when considering its use in the FIWARE platform, below is a list of the GEs that typically can be
connected to SpagoBI usage:

* [Identity Management - KeyRock](http://catalogue.fiware.org/enablers/identity-management-keyrock) : SpagoBI can use authentication and authorization mechanism provided by the Identity Manament. Check the [admin manual](https://github.com/SpagoBILabs/SpagoBI/tree/master/doc/admin/README.md)
* [Orion Context Broker](https://github.com/telefonicaid/fiware-orion) : SpagoBI can query and analyze the data coming from Orion and it can be notified if the data changes. Check the [NGSI manual](https://github.com/SpagoBILabs/SpagoBI/blob/master/doc/user/NGSI/README.md)
* CKAN: SpagoBI can query and analyze the data from CKAN portal. Check the [CKAN manual](https://github.com/SpagoBILabs/SpagoBI/blob/master/doc/user/CKAN/README.md)

[Top](#top)

## Resource consumption

The most usual problems that SpagoBI may have are related
to abnormal consumption of memory due to leaks.

Regarding abnormal consumption of memory, it can be detected by the the
following symptoms:

-   SpagoBI launches OutOfMemoryExceptions
-   SpagoBI doesn't crash but stops processing requests, i.e. new
    requests "hang" as they never receive a response. 

You can verify these situations also checking the log files of Tomcat.

The solution to this problem is [restarting SpagoBI](https://github.com/SpagoBILabs/SpagoBI/tree/master/doc/admin#how-to-start-and-stop-spagobi-server).

[Top](#top)

## I/O Flows

SpagoBI uses the following flows:

* Using TCP port 8080, default port of Tomcat Application Server
* From SpagoBI to MySQL database. In the case of running MySQL in the same host as the broker, this is an internal flow (i.e. using the loopback interface). The standard port in MySQL is 3306 although that can be changed in the configuration.
* From SpagoBI to external data sources. SpagoBI cqn query external data (REST services, external database connected with JDBC, CKAN portal)

[Top](#top)
