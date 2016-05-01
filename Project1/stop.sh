#!/bin/bash

# if this is executed when instance just boots, do not need "sudo"

ACCESS_KEY_ID="XXX"
SECRET_ACCESS_KEY="XXX"
ItemNum=0
IMAGE_ID="ami-c229c0a2"
INSTANCE_TYPE="t2.micro"
KEY_NAME="zp01-key-pair-uswest2"
SECURITY_GROUP="launch-wizard-1"

# IDS=$(aws ec2 describe-instances | grep "InstanceId" | sed -E 's/^[^i]*([i]+.*)["][,]/\1/' > "instances.txt")

IDS=$(aws ec2 describe-instances | grep "InstanceId" | sed -E 's/^[^i]*([i]+.*)["][,]/\1/')
echo $IDS
echo $(aws ec2 terminate-instances --instance-ids $IDS)

# initInstance
# http://docs.aws.amazon.com/cli/latest/reference/ec2/stop-instances.html
