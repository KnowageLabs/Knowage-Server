# What is SpagoBI?

This is the official repository of SpagoBI. SpagoBI is the only entirely Open Source Business Intelligence suite. It covers all the analytical areas of Business Intelligence projects, with innovative themes and engines.

> [spagobi.org](https://www.spagobi.org)
 
# Supported tags and respective Dockerfile links

* ```latest```, ```fiware``` : [Dockerfile](https://github.com/SpagoBILabs/SpagoBI/blob/master/docker/5.2-fiware-all-in-one/Dockerfile)
* ```5.1-fiware-all-in-one``` : [Dockerfile](https://github.com/SpagoBILabs/SpagoBI/blob/master/docker/5.1-fiware-all-in-one/Dockerfile)
* ```5.1-all-in-one```, ```stable``` : [Dockerfile](https://github.com/SpagoBILabs/SpagoBI/blob/master/docker/5.1-all-in-one/Dockerfile)

# Run SpagoBI All In One

## Run SpagoBI with embedded Database

All in One image contains many demo examples with the DB included. SpagoBI is started in this way:

```console
$ docker run --name spagobi -d spagobilabs/spagobi:all-in-one
2656735e7ce42c30ae1b284d05e05565b3dbc62fa0118279b31c479b7b0e2cfa
```

## Run SpagoBI with MySQL Database

It's also possible to run SpagoBI connected to an external MySQL Docker container. It doesn't contain examples, because the examples are in the embedded database. It's like an empty installation.

Firstly run the MySQL Database container for SpagoBI:

```console
$ docker run --name spagobidb -e MYSQL_USER=spagobiuser -e MYSQL_PASSWORD=spagobipassword -e MYSQL_DATABASE=spagobidb -e MYSQL_ROOT_PASSWORD=spagobirootpassword -d mysql:5.5
3347e0fb5e0f8e0b4e9a7ee32f5f72b8009ab4bbc018647b96d5a451142d8e9e
```

You can use whatever you want for user and password properties. Then run SpagoBI and link it to the previous MySQL container:

```console
$ docker run --link spagobidb:db --name spagobi -d spagobilabs/spagobi:all-in-one
c24b00a79cfd05bf45e4b30fecb0857298f5bc3133b8f468a3be36a796f0c287
```

### Run SpagoBI with an external MySQL Database

You can connect SpagoBI with an external MySQL Database instead of running a MySQL container. It's usfficient to pass the MySQL properties (host, port, username, password, db) to SpagoBI:

```console
$ docker run --name spagobi -e DB_ENV_MYSQL_USER=spagobi_user -e DB_ENV_MYSQL_PASSWORD=pass -e DB_ENV_MYSQL_DATABASE=spagobi -e DB_PORT_3306_TCP_ADDR=192.168.93.1 -e DB_PORT_3306_TCP_PORT=3306 -d spagobilabs/spagobi:all-in-one
```

You need to use the host address of your database visible by the docker container. In the example it's ```192.168.93.1```, created by the host Virtual Machine network.

### Use docker-compose

It's also possible to use ```docker-compose``` for running SpagoBI with a MySQL container, within a single command. You can fine the compose file definition [here](https://github.com/SpagoBILabs/SpagoBI/tree/master/docker/5.1-all-in-one). Run this command inside the folder with ```docker-compose.yml``` file:

```console
$ docker-compose up
```

## Properties

The only one environment property used by SpagoBI is:

* ```PUBLIC_ADDRESS``` : *optional* - define the IP Host of SpagoBI visible from outside the container (eg. ```http://$PUBLIC_ADDRESS:8080/SpagoBI```),  the url's host part of SpagoBI url. If not present (like the above examples) the default value is the IP of container. You can use the IP of virtual machine (in OSX or Windows environment) or localhost if you map the container's port.

# Use SpagoBI

Get the IP of container :

```console
$ docker inspect --format '{{ .NetworkSettings.IPAddress }}' spagobi
172.17.0.43
```

Open SpagoBI on your browser at url (use your container-ip): 

> container-ip:8080/SpagoBI

If you run the host with a Virtual Machine (for example in a Mac environment) then you can route the traffic directly to the container from you localhost using route command:

```console
$ sudo route -n add 172.17.0.0/16 ip-of-host-Virtual-Machine
```

# License

View license information [here](https://www.spagobi.org/homepage/opensource/license/) for the software contained in this image.
