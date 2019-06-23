#!/bin/sh

if [[ "$1" = "consumer" ]]; then
  echo "build $1"
  cd debian-jdk8-adaptive-loadbalance/debian-jdk8-consumer
  cp -R /Users/ch/code/race/tianchi2019/adaptive-loadbalance .
  docker build --build-arg user_code_address="https://code.aliyun.com/pengqun/adaptive-loadbalance.git" -t consumer .
  rm -rf adaptive-loadbalance
elif [[ "$1" = "provider" ]]; then
  echo "build $1"
  cd debian-jdk8-adaptive-loadbalance/debian-jdk8-provider
  cp -R /Users/ch/code/race/tianchi2019/adaptive-loadbalance .
  docker build --build-arg user_code_address="https://code.aliyun.com/pengqun/adaptive-loadbalance.git" -t provider .
  rm -rf adaptive-loadbalance
else
  echo "wrong target: $1"
  exit -1
fi
