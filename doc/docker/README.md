# What is Knowage?

This is the official repository of Knowage. Knowage is the professional open source suite for modern business analytics over traditional sources and big data systems.

> [knowage-suite.com](https://www.knowage-suite.com)
 
# Supported tags and respective Dockerfile links

* ```latest``` : [Dockerfile](https://github.com/KnowageLabs/Knowage-Server/master/docker/latest/Dockerfile)
* ```6.1-all-in-one```, ```stable``` : [Dockerfile](https://github.com/SKnowageLabs/Knowage-Server/master/docker/6.1-all-in-one/Dockerfile)

# Run Knowage All In One

## Run Knowage with embedded Database

All in One image contains many demo examples with the DB included. SpagoBI is started in this way:

```console
$ docker run --name knowage -d knowagelabs/knowage:all-in-one
2656735e7ce42c30ae1b284d05e05565b3dbc62fa0118279b31c479b7b0e2cfa
```

## Run Knowage with MySQL Database

It's also possible to run SpagoBI connected to an external MySQL Docker container. It doesn't contain examples, because the examples are in the embedded database. It's like an empty installation.

Firstly run the MySQL Database container for Knowage:

```console
$ docker run --name knowagedb -e MYSQL_USER=knowageuser -e MYSQL_PASSWORD=knowagepassword -e MYSQL_DATABASE=knowagedb -e MYSQL_ROOT_PASSWORD=knowagerootpassword -d mysql:5.5
3347e0fb5e0f8e0b4e9a7ee32f5f72b8009ab4bbc018647b96d5a451142d8e9e
```

You can use whatever you want for user and password properties. Then run Knowage and link it to the previous MySQL container:

```console
$ docker run --link knowagedb:db --name knowage -d knowagelabs/knowage:all-in-one
c24b00a79cfd05bf45e4b30fecb0857298f5bc3133b8f468a3be36a796f0c287
```

### Run Knowage with an external MySQL Database

You can connect Knowage with an external MySQL Database instead of running a MySQL container. It's sufficient to pass the MySQL properties (host, port, username, password, db) to Knowage:

```console
$ docker run --name knowage -e DB_ENV_MYSQL_USER=knowage_user -e DB_ENV_MYSQL_PASSWORD=pass -e DB_ENV_MYSQL_DATABASE=knowage -e DB_PORT_3306_TCP_ADDR=192.168.93.1 -e DB_PORT_3306_TCP_PORT=3306 -d knowagelabs/knowage:all-in-one
```

You need to use the host address of your database visible by the docker container. In the example it's ```192.168.93.1```, created by the host Virtual Machine network.

### Use docker-compose

It's also possible to use ```docker-compose``` for running Knowage with a MySQL container, within a single command. You can find the compose file definition [here](https://github.com/KnowageLabs/Knowage-Server/master/docker/6.1-all-in-one). Run this command inside the folder with ```docker-compose.yml``` file:

```console
$ docker-compose up
```

## Properties

The only one environment property used by Knowage is:

* ```PUBLIC_ADDRESS``` : *optional* - define the IP Host of Knowage visible from outside the container (eg. ```http://$PUBLIC_ADDRESS:8080/knowage```),  the url's host part of Knowage url. If not present (like the above examples) the default value is the IP of container. You can use the IP of virtual machine (in OSX or Windows environment) or localhost if you map the container's port.

# Use Knowage

Get the IP of container :

```console
$ docker inspect --format '{{ .NetworkSettings.IPAddress }}' knowage
172.17.0.43
```

Open Knowage on your browser at url (use your container-ip): 

> container-ip:8080/knowage

If you run the host with a Virtual Machine (for example in a Mac environment) then you can route the traffic directly to the container from you localhost using route command:

```console
$ sudo route -n add 172.17.0.0/16 ip-of-host-Virtual-Machine
```

# License

View license information [here](https://github.com/KnowageLabs/Knowage-Server/) for the software contained in this image.
