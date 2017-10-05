#!/bin/bash
#Knowage remote verification script

ssh ubuntu@$IP <<-'ENDSSH'
	set -e

	# check the service
	if ! service knowage status; then
	  exit 1
	fi

	#checking the process with ps
	echo -n "Verifying Knowage"
	start=`date +%s`
	while ( ! ps aux | grep -q [j]ava.*Knowage.* ) && [ $((`date +%s`-$start)) -lt 10 ]; do 
		echo -n "."
		sleep 2
	done
	if ! ps aux | grep -q [j]ava.*Knowage.*; then
	  echo 
	  echo "Process is not running"
	  exit 2
	fi

	URL=http://localhost:8080/knowage/servlet/AdapterHTTP?PAGE=LoginPage\&NEW_SESSION=TRUE

	#check the web application
	start=`date +%s`
	while ( ! curl -s -S --max-time 20 $URL &> /dev/null ) && [ $((`date +%s`-$start)) -lt 360 ]; do
		echo -n "."
		sleep 2
	done
	echo	
	if ! curl -s -S --max-time 5 $URL &> /dev/null; then
	  echo "Web application is not running"
	  exit 3
	fi
	echo "Knowage is correctly running"
ENDSSH
