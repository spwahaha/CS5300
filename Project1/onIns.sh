#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"
function deployWar(){
	while [[ ! -f ~/P1.war ]]; do
		sleep 2
		echo "no file found"
	done
	echo "file found"
	echo $(mv ~/P1.war /usr/share/tomcat7/webapps)
	echo "move successfully"
}
# echo $(cd ~)
deployWar