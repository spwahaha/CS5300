#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="XXX"
SECRET_ACCESS_KEY="XXX"
S3_BUCKET="edu-cornell-cs-cs5300s16-zp55"
DOMAIN_NAME="test1"
LOCAL_IP=0
PUBLIC_IP=0
AMI_LAUNCH_INDEX=0
ItemNum=0
N=3
F=1
SUDO=""

# check run by root user or not, add sudo for command for non-root user 
if [[ $EUID -ne 0 ]]; then
	#not root user
	SUDO="sudo "
fi
echo "sudo:   $SUDO"

# install tomcat
function initTomcat() {
	echo $($SUDO yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps)
}

# start tomcat
function startTomcat(){
	echo $($SUDO service tomcat8 start)
	echo "tomcat started"
}

# initialize simple db
function initSdb(){
	echo $($SUDO aws configure set aws_access_key_id $ACCESS_KEY_ID)
	echo $($SUDO aws configure set aws_secret_access_key $SECRET_ACCESS_KEY)
	echo $($SUDO aws configure set default.region us-west-2)
	echo $($SUDO aws configure set preview.sdb true)
	# create domain "aws sdb create-domain --domain-name test1" 
	# get the item count "aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed "
}

# insert data to simpledb
function insertData(){
	LOCAL_IP=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
	PUBLIC_IP=$(curl http://169.254.169.254/latest/meta-data/public-ipv4)
	AMI_LAUNCH_INDEX=$(curl http://169.254.169.254/latest/meta-data/ami-launch-index)
	SERVER_DATA=$"ami-launch-index:  $AMI_LAUNCH_INDEX __ RebootNum: 0"
	$SUDO chmod -R 777 ~tomcat/webapps
	# $SUDO chmod 777 ~tomcat/webapps/system_info.txt
	echo $SUDO "$SERVER_DATA">~tomcat/webapps/server_data.txt
	SYSTEM_INFO=$"N: $N __ F:$F"
	echo $SUDO "$SYSTEM_INFO">~tomcat/webapps/system_info.txt
	# echo $($SUDO mv /server_data.txt ~tomcat/webapps)
	# echo $($SUDO mv /system_info.txt ~tomcat/webapps)
	echo "ami-launch-index:  $AMI_LAUNCH_INDEX"
	echo "ip: $IP"
	# check whether the data has already been inserted to the simpledb
	Attr=$($SUDO aws sdb get-attributes --domain-name $DOMAIN_NAME --item-name item$AMI_LAUNCH_INDEX)
	size=${#Attr} 
	echo $Attr
	echo "size of the Attr:   $size"
	# insert data if data is not in the simpedb
	if [[ $size -eq 0 ]]; then
		ATTRIBUTE='[{"Name": "Index","Value": ''"'"$AMI_LAUNCH_INDEX"'"''},{"Name":"Private_ip","Value":''"'"$LOCAL_IP"'"''},{"Name":"Public_ip","Value":''"'"$PUBLIC_IP"'"''}]'
		echo $ATTRIBUTE
		echo $($SUDO aws sdb put-attributes --domain-name $DOMAIN_NAME --item-name "item$AMI_LAUNCH_INDEX" --attributes "$ATTRIBUTE")
	fi
	# echo $(aws sdb select --select-expression "select * from test1")
}

# save simpledb data to the file system
function saveData(){
	ItemNum=$($SUDO aws sdb domain-metadata --domain-name $DOMAIN_NAME | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	while [[ ! $ItemNum -eq $N ]]; do
		echo "ItemNum:    $ItemNum"
		sleep 2
		ItemNum=$($SUDO aws sdb domain-metadata --domain-name $DOMAIN_NAME | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	done
	# $SUDO chmod 777 ~tomcat/webapps/NodesDB.txt
	DATA=$($SUDO aws sdb select --select-expression "select * from $DOMAIN_NAME")
	$SUDO echo "$DATA">~tomcat/webapps/NodesDB.txt
}

# install the java8
function deployJava(){
	echo $($SUDO yum -y remove java-1.7.0-openjdk)
	echo $($SUDO yum -y install java-1.8.0)
}

# deploy the applocation and reboot script
function deployWar(){
	echo "deploy files"
	echo $($SUDO aws s3 cp s3://$S3_BUCKET/Project1.war ~tomcat/webapps/Project1.war)
	echo $($SUDO aws s3 cp s3://$S3_BUCKET/reboot.sh ~tomcat/webapps/reboot.sh)
	echo $($SUDO aws s3 cp s3://$S3_BUCKET/install-my-app.sh ~tomcat/webapps/install-my-app.sh)
	echo $($SUDO chmod 777 ~tomcat/webapps/install-my-app.sh)
	echo $($SUDO chmod 777 ~tomcat/webapps/reboot.sh)
}
# echo $(cd ~)


deployJava
initTomcat
initSdb
insertData
saveData
deployWar
startTomcat

