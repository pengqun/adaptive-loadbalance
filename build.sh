#!/bin/sh

if [[ "$1" = "consumer" || "$1" = "all" ]]; then
  echo "=== build consumer begin ==="
  cd debian-jdk8-adaptive-loadbalance/debian-jdk8-consumer
  rm -rf adaptive-loadbalance
  cp -R /Users/ch/code/race/tianchi2019/adaptive-loadbalance .
  docker build --build-arg user_code_address="https://code.aliyun.com/pengqun/adaptive-loadbalance.git" -t consumer .
  rm -rf adaptive-loadbalance
  cd ../../
  echo "=== build consumer end ==="
fi

if [[ "$1" = "provider" || "$1" = "all" ]]; then
  echo "=== build provider begin ==="
  cd debian-jdk8-adaptive-loadbalance/debian-jdk8-provider
  rm -rf adaptive-loadbalance
  cp -R /Users/ch/code/race/tianchi2019/adaptive-loadbalance .
  docker build --build-arg user_code_address="https://code.aliyun.com/pengqun/adaptive-loadbalance.git" -t provider .
  rm -rf adaptive-loadbalance
  cd ../../
  echo "=== build provider end ==="
fi

if [[ "$2" = "up" ]]; then
  docker-compose up
fi
