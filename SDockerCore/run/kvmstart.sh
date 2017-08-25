nohup /usr/local/ant -f /home/aculearn/disk/deploy/bin/build.xml 'start_service' >/dev/null 1&> /home/chat/deploy/logs/startup.log &
echo 'start log web.'
echo '-------------------------- startup logs ---------------------'
tail -f /home/chat/deploy/logs/startup.log