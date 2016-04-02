#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="AKIAIOO6HTOHZF5LG65Q"
SECRET_ACCESS_KEY="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"
LOCAL_IP=0
PUBLIC_IP=0
AMI_LAUNCH_INDEX=0
ItemNum=0
ServerNum=1
function initTomcat() {
	echo $(yum -y install tomcat7-webapps tomcat7-docs-webapp tomcat7-admin-webapps)
}

function startTomcat(){
	echo $(service tomcat7 start)
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
	LOCAL_IP=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
	PUBLIC_IP=$(curl http://169.254.169.254/latest/meta-data/public-ipv4)
	AMI_LAUNCH_INDEX=$(curl http://169.254.169.254/latest/meta-data/ami-launch-index)
	echo "ami-launch-index:  $AMI_LAUNCH_INDEX"
	echo "ip: $IP"
	ATTRIBUTE='[{"Name": "Index","Value": ''"'"$AMI_LAUNCH_INDEX"'"''},{"Name":"Private_ip","Value":''"'"$LOCAL_IP"'"''},{"Name":"Public_ip","Value":''"'"$PUBLIC_IP"'"''}]'
	echo $ATTRIBUTE
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

function deployJava(){
	echo $(yum -y remove java-1.7.0-openjdk)
	echo $(yum -y install java-1.8.0)
}

function deployWar(){
	while ! [[ -f ~/P1.war ]];
	do
		sleep 2
		echo "no file found"
	done
	echo "file found"
	echo $(mv ~/P1.war /usr/share/tomcat7/webapps)
	echo "move successfully"
}
# echo $(cd ~)
deployJava
initTomcat
initSdb
insertData
saveData
startTomcat
# deployWar