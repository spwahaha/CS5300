#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="AKIAIOO6HTOHZF5LG65Q"
SECRET_ACCESS_KEY="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"
ItemNum=0
IMAGE_ID="ami-c229c0a2"
INSTANCE_TYPE="t2.micro"
KEY_NAME="zp01-key-pair-uswest2"
SECURITY_GROUP="sg-e3ce7584"
INSTANCE_INFO=""

function initSdb(){
	echo $(aws configure set aws_access_key_id $ACCESS_KEY_ID)
	echo $(aws configure set aws_secret_access_key $SECRET_ACCESS_KEY)
	echo $(aws configure set default.region us-west-2)
	echo $(aws configure set preview.sdb true) 
	echo $(aws sdb delete-domain --domain-name test1) # clear domain
	echo $(aws sdb create-domain --domain-name test1) # creat db
	# ItemNum=$(aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed -E 's/^[^0-9]*([0-9]+).*/\1/')
	# echo "ItemCount $ItemNum" # get tuple number in db
	# if $ItemNum != 0, we need to delete all the tuples in original db

	# insert tuple
	# echo $(aws sdb put-attributes --domain-name test1 --item-name item1 --attributes '[{"Name": "string","Value": "string","Replace": true}]')

	# create domain "aws sdb create-domain --domain-name test1" 
	# get the item count "aws sdb domain-metadata --domain-name test1 | grep "ItemCount" | sed "
}

function initInstance(){
	INSTANCE_INFO=$(aws ec2 run-instances --image-id $IMAGE_ID --count 1 --instance-type $INSTANCE_TYPE --key-name $KEY_NAME --security-group-ids $SECURITY_GROUP  --user-data file://init.sh --associate-public-ip-address > "instanceInfo.txt")
	echo $INSTANCE_INFO
}

function transferFile(){
	INFO=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/ec2-user@\1:P1.war/')
	# INFO=$(cat running.txt | grep "PublicIpAddress")
	# INFO=$(cat running.txt | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/\1/')

	echo $INFO  
	echo ${#INFO}
	while [[ ${#INFO} -eq 0 ]]; do
	 	sleep 2
	 	INFO=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/ec2-user@\1:P1.war/')
		echo $INFO  
	done 

	arr=$(echo $INFO | tr " " "\n")
	sleep 10
	for x in $arr
	do
		TRANS=$(scp -i -o StrictHostKeyChecking=no "zp01-key-pair-uswest2.pem" P1.war "$x" )
	    echo "$x"
	done
}

# scp -i "zp01-key-pair-uswest2.pem" -o StrictHostKeyChecking=no P1.war ec2-user@52.34.245.226:P1.war
initSdb
initInstance
# transferFile