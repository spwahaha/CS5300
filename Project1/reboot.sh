#!/bin/bash
AMI_LAUNCH_INDEX=$(sed -E 's/^[^0-9]*([0-9]+).*[_]+[^0-9]*([0-9]+).*/\1/' ~tomcat/webapps/server_data.txt)
REBOOT_NUM=$(sed -E 's/^[^0-9]*[0-9]+.*[_]+[^0-9]*([0-9]+).*/\1/' ~tomcat/webapps/server_data.txt)
REBOOT_NUM=$((10#$REBOOT_NUM + 1))
echo $REBOOT_NUM
echo $AMI_LAUNCH_INDEX
SERVER_DATA=$"ami-launch-index:  $AMI_LAUNCH_INDEX __ RebootNum: $REBOOT_NUM"
$(sudo chmod 777 ~tomcat/webapps/server_data.txt)
$(sudo echo "$SERVER_DATA"> ~tomcat/webapps/server_data.txt)
echo $(sudo service tomcat8 start)
