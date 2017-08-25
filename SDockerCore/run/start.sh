#nohup ant -f build.xml 'start_service' &>/dev/null 2>&1 &
nohup ant -f build.xml 'start_service' &> ../logs/startup.log &
#echo 'start log web.'
#echo '-------------------------- startup logs ---------------------'
#tail -f ../logs/startup.log