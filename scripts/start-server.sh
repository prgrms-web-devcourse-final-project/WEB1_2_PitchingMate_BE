#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop catchmi-server || true
docker rm catchmi-server || true
docker pull 533267244952.dkr.ecr.ap-northeast-2.amazonaws.com/catchmi-server:latest
docker run -d \
  --name catchmi-server \
  --restart unless-stopped \
  -p 8080:8080 \
  -e TZ=Asia/Seoul \
  -e JWT_SECRET_KEY="${JWT_SECRET_KEY}" \
  -e NAVER_CLIENT_ID="${NAVER_CLIENT_ID}" \
  -e NAVER_REDIRECT_URI="${NAVER_REDIRECT_URI}" \
  -e NAVER_CLIENT_SECRET="${NAVER_CLIENT_SECRET}" \
  -e OPENWEATHER_API_KEY="${OPENWEATHER_API_KEY}" \
  -e S3_BUCKET_NAME="${S3_BUCKET_NAME}" \
  -e IAM_ACCESS_KEY="${IAM_ACCESS_KEY}" \
  -e IAM_SECRET_KEY="${IAM_SECRET_KEY}" \
  533267244952.dkr.ecr.ap-northeast-2.amazonaws.com/catchmi-server:latest
echo "--------------- 서버 배포 끝 -----------------"