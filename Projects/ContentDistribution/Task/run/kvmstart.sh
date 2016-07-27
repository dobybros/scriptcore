nohup /home/aculearn/tools/apache-ant-1.9.3/bin/ant -f /home/aculearn/disk/deploy/bin/build.xml 'start_service' >/dev/null 1&> /home/aculearn/disk/deploy/logs/startup.log &
echo 'start log web.'
echo '-------------------------- startup logs ---------------------'
tail -f /home/aculearn/disk/deploy/logs/startup.log