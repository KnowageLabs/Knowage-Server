#!/bin/bash
#Knowage VM installation script

#install chef 13 client
wget https://packages.chef.io/files/stable/chef/13.5.3/ubuntu/16.04/chef_13.5.3-1_amd64.deb
dpkg -i chef_13.5.3-1_amd64.deb
rm -f chef_13.5.3-1_amd64.deb

#download recipes
wget https://github.com/KnowageLabs/Knowage-Server/releases/download/6.1/chef-cookbooks.zip

#install zip
apt-get install unzip

unzip chef-cookbooks.zip

#install knowage
mkdir /etc/chef
chef-client -z -o 'recipe[knowage::1.0.4_install]'
rm -rf cookbooks
