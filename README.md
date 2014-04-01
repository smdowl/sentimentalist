sentimentalist
==============

Sentiment analysis to make monies on stocks.

Building
========

I have been using a combination of s3cmd and elastic-mapreduce both of which take a bit of setting up but are totally worth it.

At the moment, if you run `mvn package` a script will send the jar up to s3 in s3://sentimentalist/jars/$USER/ but uses s3cmd to do so. This means it'll break if you don't have that set up. You can comment out the relevant lines in the pom.xml if you don't want to use it.

If you want to use elastic-mapreduce, clone it from https://github.com/tc/elastic-mapreduce-ruby since the one on Amazon's site doesn't work. Once you've set this up you can start an Hadoop job by running `./start-job.sh $job-name $main-class` where $main-class is only the package path after com.whereismydot. At the moment this only runs on a single small instance and is just using a mini data set but we can change that as and when we need to.

