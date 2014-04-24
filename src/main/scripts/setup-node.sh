#!/bin/bash

export HADOOP_USER_CLASSPATH_FIRST=true;
echo "HADOOP_CLASSPATH=lib/*" >> /home/hadoop/conf/hadoop-user-env.sh

mkdir ~/.aws/

echo "[default]
aws_access_key_id = AKIAIITTCEMTF2AWMLUA
aws_secret_access_key = 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
region=eu-west-1" > ~/.aws/config

cd ~
aws s3 cp s3://sentimentalist/setup/merge-jars.py ~/

mkdir otherjars
aws s3 cp s3://sentimentalist/setup/jarlibs.tar ~/otherjars

cd otherjars
tar -xf ~/otherjars/jarlibs.tar

cd ~
sudo python merge-jars.py ~/otherjars/lib ~/lib