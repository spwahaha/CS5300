#!/bin/bash

# INFO=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/ec2-user@\1:P1.war/')
# INFO=$(cat running.txt | grep "PublicIpAddress")
# INFO=$(cat running.txt | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/\1/')

# echo $INFO  
# echo ${#INFO}
# while [[ ${#INFO} -eq 0 ]]; do
#  	sleep 2
#  	INFO=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" | grep "PublicIpAddress" | sed -E 's/.*["]([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).*/ec2-user@\1:P1.war/')
# 	echo $INFO  
# done 

# arr=$(echo $INFO | tr " " "\n")
# for x in $arr
# do
# 	TRANS=$(scp -i "zp01-key-pair-uswest2.pem" onIns.sh 52.36.238.181:onIns.sh )
# 	# TRANS=$(scp -i "zp01-key-pair-uswest2.pem" P1.war "$x" )

#     echo "$x"
# done
# function deploy(){
# 	echo $(mv /home/ec2-user/P1.war /usr/share/tomcat7/webapps/)
# 	echo $(scp -i "zp01-key-pair-uswest2.pem" P1.war ec2-user@ec2-52-38-121-222.us-west-2.compute.amazonaws.com:P1.war)
	# sudo yum -y remove java-1.7.0-openjdk
	# sudo yum -y install java-1.8.0
# }
# TRANS=$(scp -i "zp01-key-pair-uswest2.pem" P1.war "$INFO" )
# echo $TRANS

AMI_LAUNCH_INDEX=$(sed -E 's/^[^0-9]*([0-9]+).*[T]+[^0-9]*([0-9]+).*/\1/' ~tomcat/webapps/server_data.txt)
REBOOT_NUM=$(sed -E 's/^[^0-9]*[0-9]+.*[T]+[^0-9]*([0-9]+).*/\1/' ~tomcat/webapps/server_data.txt)
REBOOT_NUM=$((10#$REBOOT_NUM + 1))
echo $REBOOT_NUM
echo $AMI_LAUNCH_INDEX
SERVER_DATA=$"ami-launch-index:  $AMI_LAUNCH_INDEX TT RebootNum: $REBOOT_NUM"
$(sudo chmod 777 ~tomcat/webapps/server_data.txt)
$(sudo echo "$SERVER_DATA"> ~tomcat/webapps/server_data.txt)
echo $(sudo service tomcat8 start)
