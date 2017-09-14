#!/bin/bash
set -e

#get the address of container
#example : default via 172.17.42.1 dev eth0 172.17.0.0/16 dev eth0 proto kernel scope link src 172.17.0.109
PUBLIC_ADDRESS=`ip route | grep src | awk '{print $9}'`

echo "Setting directory to ${KNOWAGE_DIRECTORY}"
sed -i "s|KNOWAGE_DIRECTORY|${KNOWAGE_DIRECTORY}|g" params.properties

echo "Setting public address to ${PUBLIC_ADDRESS}"
sed -i "s|PUBLIC_ADDRESS|${PUBLIC_ADDRESS}|g" params.properties

sed -i "s|DB_ENV_MYSQL_USER|${DB_ENV_MYSQL_USER}|g" params.properties
sed -i "s|DB_ENV_MYSQL_PASSWORD|${DB_ENV_MYSQL_PASSWORD}|g" params.properties
sed -i "s|DB_PORT_3306_TCP_ADDR|${DB_PORT_3306_TCP_ADDR}|g" params.properties
sed -i "s|DB_PORT_3306_TCP_PORT|${DB_PORT_3306_TCP_PORT}|g" params.properties


#wait for MySql if it's a compose image
if [ -n "$WAIT_MYSQL" ]; then
	while ! curl http://$DB_PORT_3306_TCP_ADDR:$DB_PORT_3306_TCP_PORT/
	do
	  echo "$(date) - still trying to connect to mysql"
	  sleep 1
	done
fi

exec "$@"
