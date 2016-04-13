#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="AKIAIOO6HTOHZF5LG65Q"
SECRET_ACCESS_KEY="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"
LOCAL_IP=0
PUBLIC_IP=0
AMI_LAUNCH_INDEX=0
ItemNum=0
N=3
F=1
function initTomcat() {
	echo $(yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps)
}

function startTomcat(){
	echo $(service tomcat8 start)
	echo "tomcat started"
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
	SERVER_DATA=$"ami-launch-index:  $AMI_LAUNCH_INDEX __ RebootNum: 0"
	echo "$SERVER_DATA">server_data.txt
	SYSTEM_INFO=$"N: $N __ F:$F"
	echo "$SYSTEM_INFO">system_info.txt
	echo $(mv /server_data.txt ~tomcat/webapps)
	echo $(mv /system_info.txt ~tomcat/webapps)
	echo "ami-launch-index:  $AMI_LAUNCH_INDEX"
	echo "ip: $IP"
	ATTRIBUTE='[{"Name": "Index","Value": ''"'"$AMI_LAUNCH_INDEX"'"''},{"Name":"Private_ip","Value":''"'"$LOCAL_IP"'"''},{"Name":"Public_ip","Value":''"'"$PUBLIC_IP"'"''}]'
	echo $ATTRIBUTE
	echo $(aws sdb put-attributes --domain-name test1 --item-name "item$AMI_LAUNCH_INDEX" --attributes "$ATTRIBUTE")
	echo $(aws sdb select --select-expression "select * from test1")
}

function saveData(){
	ItemNum=$(aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	while [[ ! $ItemNum -eq $N ]]; do
		sleep 2
		ItemNum=$(aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	done
	echo $(aws sdb select --select-expression "select * from test1" > "NodesDB.txt")
	# echo $()
}

function deployJava(){
	echo $(yum -y remove java-1.7.0-openjdk)
	echo $(yum -y install java-1.8.0)
}


function deployWar(){
	echo "deploy files"
	echo $(mv /NodesDB.txt ~tomcat/webapps)
	echo $(aws s3 cp s3://edu-cornell-cs-cs5300s16-zp55/Project1.war ~tomcat/webapps/Project1.war)
	echo $(aws s3 cp s3://edu-cornell-cs-cs5300s16-zp55/reboot.sh ~tomcat/webapps/reboot.sh)
	echo $(chmod 777 ~tomcat/webapps/reboot.sh)
}
# echo $(cd ~)
deployJava
initTomcat
initSdb
insertData
saveData
deployWar
startTomcat
