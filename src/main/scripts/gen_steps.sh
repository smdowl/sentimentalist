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
input=$3

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
               \"$input\",
               \"s3n://sentimentalist/output/$USER/$jobname\"
            ] 
         } 
      } 
]" > src/main/jobflows/$jobname.json