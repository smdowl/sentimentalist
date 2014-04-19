#!/bin/sh

# start-job.sh - Start an EMR job to run a given main class. A job flow is created and stored in
# src/main/jobflows/. A most recent version of the jar file must but uploaded to s3 beforehand by calling `mvn package`.
#
# Usage:
# ./start-job.sh example-job preprocessing.CompanyStats

jobname=$1
mainclass=$2
input=$3

sh src/main/scripts/gen_steps.sh $jobname $mainclass $input

elastic-mapreduce --create --name $jobname --log-uri s3://sentimentalist/logs/$USER \
--num-instances 5 \
--instance-type m1.medium \
--json src/main/jobflows/$jobname.json \
--bootstrap-action s3://elasticmapreduce/bootstrap-actions/configure-hadoop --bootstrap-name hadoop-conf --args "-m,fs.s3.canned.acl=PublicRead" \
--bootstrap-action s3://sentimentalist/setup/setup-node.sh --bootstrap-name add-libs
