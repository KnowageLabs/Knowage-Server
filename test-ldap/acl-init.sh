#!/bin/bash
set -e
apt-get update -qq
apt-get install -y -qq ldap-utils > /dev/null 2>&1
echo "Applying ACL patch..."
ldapmodify -H ldap://openldap:389 -x \
  -D 'cn=admin,cn=config' -w 'configpass' \
  -f /tmp/acl.ldif
echo "ACL updated successfully"
