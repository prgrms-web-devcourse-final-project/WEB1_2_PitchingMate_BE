#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop catchmi-server || true
docker rm catchmi-server || true
docker pull 533267244952.dkr.ecr.ap-northeast-2.amazonaws.com/catchmi-server/catchmi-server:latest
docker run -d --name catchmi-server -p 8080:8080 533267244952.dkr.ecr.ap-northeast-2.amazonaws.com/catchmi-server/catchmi-server:latest
echo "--------------- 서버 배포 끝 -----------------"