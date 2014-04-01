#!/bin/sh

jobname=$1
mainclass=$2

sh src/main/scripts/gen_steps.sh $jobname $mainclass

elastic-mapreduce --create --name $jobname --log-uri s3://sentimentalist/logs/$USER --json src/main/jobflows/$jobname.json