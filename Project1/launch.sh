#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="AKIAIOO6HTOHZF5LG65Q"
SECRET_ACCESS_KEY="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"
IMAGE_ID="ami-c229c0a2"
INSTANCE_TYPE="t2.micro"
KEY_NAME="zp01-key-pair-uswest2"
SECURITY_GROUP="sg-e3ce7584"
INSTANCE_INFO=""
N=1
S3_BUCKET="edu-cornell-cs-cs5300s16-zp55"
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
	INSTANCE_INFO=$(aws ec2 run-instances --image-id $IMAGE_ID --count $N --instance-type $INSTANCE_TYPE --key-name $KEY_NAME --security-group-ids $SECURITY_GROUP  --user-data file://install-my-app.sh --associate-public-ip-address > "instanceInfo.txt")
	echo $INSTANCE_INFO
}


function transferFile(){
	echo $(aws s3 cp Project1.war s3://$S3_BUCKET/Project1.war)
	echo $(aws s3 cp reboot.sh s3://$S3_BUCKET/reboot.sh)
	echo $(aws s3 cp install-my-app.sh s3://$S3_BUCKET/install-my-app.sh)
}

initAWS
transferFile
initSdb
initInstance