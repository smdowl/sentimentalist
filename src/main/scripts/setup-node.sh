#!/bin/bash

mkdir ~/.aws/

echo "[default]
aws_access_key_id = AKIAIITTCEMTF2AWMLUA
aws_secret_access_key = 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
region=eu-west-1" > ~/.aws/config

cd ~
aws s3 cp s3://sentimentalist/setup/jarlibs.tar ~/
tar -xf ~/jarlibs.tar