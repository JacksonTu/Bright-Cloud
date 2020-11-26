#!/bin/bash
echo 'begin docker'
# 查看正在运行的容器
echo 'view container'
docker ps
#停止容器
echo 'stop container'
docker stop bright-server-test
#删除容器
echo 'delete container'
docker rm -f bright-server-test
#docker images
#删除镜像
echo 'delete images'
docker rmi -f bright-server-test
#创建镜像
echo 'create images'
docker build -t bright-server-test .
#运行容器
echo 'run container'
docker run -it -d --name bright-server-test -p8007:8007 -v /home/output:/output bright-server-test
echo 'end docker'
