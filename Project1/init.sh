#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="AKIAIOO6HTOHZF5LG65Q"
SECRET_ACCESS_KEY="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"
IP=0
AMI_LAUNCH_INDEX=0
ItemNum=0
ServerNum=1
function initTomcat() {
	echo $(yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps)
}

function startTomcat(){
	echo $(service tomcat8 start)
	# echo $(sudo service tomcat8 stop)
}

function initSdb(){
	echo $(aws configure set aws_access_key_id $ACCESS_KEY_ID)
	echo $(aws configure set aws_secret_access_key $SECRET_ACCESS_KEY)
	echo $(aws configure set default.region us-west-2)
	echo $(aws configure set preview.sdb true)
	# create domain "aws sdb create-domain --domain-name test1" 
	# get the item count "aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed "
}

function insertData(){
	# echo $(rm *)
	IP=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
	AMI_LAUNCH_INDEX=$(curl http://169.254.169.254/latest/meta-data/ami-launch-index)
	echo "ami-launch-index:  $AMI_LAUNCH_INDEX"
	echo "ip: $IP"
	ATTRIBUTE='[{"Name": "Index","Value": ''"'"$AMI_LAUNCH_INDEX"'"''},{"Name":"Ip","Value":''"'"$IP"'"''}]'
	# echo $ATTRIBUTE
	echo $(aws sdb put-attributes --domain-name test1 --item-name "item$AMI_LAUNCH_INDEX" --attributes "$ATTRIBUTE")
	echo $(aws sdb select --select-expression "select * from test1")
}

function saveData(){
	ItemNum=$(aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	while [[ ! $ItemNum -eq $ServerNum ]]; do
		sleep 2
		ItemNum=$(aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	done
	echo $(aws sdb select --select-expression "select * from test1" > "NodesDB.txt")
}
# echo $(cd ~)
initTomcat
initSdb
insertData
saveData
startTomcat