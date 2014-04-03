#!/bin/sh

# gen_steps.sh
# Create the json file that is requried for elastic-mapreduce to run. 
# You pass what you would like to call the job and the path of the main class
# after com.whereismydot.
#
# Usage:
#  src/main/scripts/gen_steps.sh test-job processing.CompanyStats

jobname=$1
mainclass=$2

mkdir -p src/main/jobflows

echo "[ 
      { 
         \"Name\": \"$jobname\", 
         \"ActionOnFailure\": \"CONTINUE\", 
         \"HadoopJarStep\": 
         { 
            \"MainClass\": \"com.whereismydot.$mainclass\", 
            \"Jar\": \"s3://sentimentalist/jars/$USER/Sentimentalist-1.0-SNAPSHOT.jar\", 
            \"Args\": 
            [ 
               \"s3://sentimentalist/test-data/mini_1.json\", 
               \"s3://sentimentalist/output/$USER/test\"
            ] 
         } 
      } 
]" > src/main/jobflows/$jobname.json