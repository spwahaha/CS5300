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

function initAWS(){
	echo $(aws configure set aws_access_key_id $ACCESS_KEY_ID)
	echo $(aws configure set aws_secret_access_key $SECRET_ACCESS_KEY)
	echo $(aws configure set default.region us-west-2)
	echo $(aws configure set preview.sdb true) 	
}

function initSdb(){
	echo $(aws sdb delete-domain --domain-name test1) # clear domain
	echo $(aws sdb create-domain --domain-name test1) # creat db
}

function initInstance(){
	INSTANCE_INFO=$(aws ec2 run-instances --image-id $IMAGE_ID --count 3 --instance-type $INSTANCE_TYPE --key-name $KEY_NAME --security-group-ids $SECURITY_GROUP  --user-data file://install-my-app.sh --associate-public-ip-address > "instanceInfo.txt")
	echo $INSTANCE_INFO
}


function transferFile(){
	echo $(aws s3 cp Project1.war s3://edu-cornell-cs-cs5300s16-zp55/Project1.war)
	echo $(aws s3 cp reboot.sh s3://edu-cornell-cs-cs5300s16-zp55/reboot.sh)
}

# scp -i "zp01-key-pair-uswest2.pem" -o StrictHostKeyChecking=no P1.war ec2-user@52.34.245.226:P1.war
initAWS
transferFile
initSdb
initInstance