#!/usr/bin/env bash

# Note this is a setup script for an initial Kubernetes cluster on AWS.
# It requires serveral manual steps better covered in the README.md
# The main thing to note is this is expected to run on a machine with a configured aws client.

# After starting the cluster, next steps are:
#   Setting up the unified file system https://github.com/pcuzner/docker-gluster-centos
#   Configuring client mounts of that file system https://github.com/kubernetes/kubernetes/tree/master/examples/glusterfs
#   Configure each node to support Gluster's hadoop layer https://forge.gluster.org/hadoop/pages/Configuration
#   One or more of:
#     Running Spark https://github.com/kubernetes/kubernetes/tree/master/examples/spark
#     Running Storm https://github.com/kubernetes/kubernetes/tree/master/examples/storm
#     Running Cassandra https://github.com/kubernetes/kubernetes/tree/master/examples/casssandra
#   Build software as docker images and configure pods with replication controllers to run it.
# Ultimately this is too much side-work for the available time-frame.
# lmod 2015-09-30 following http://kubernetes.io/v1.0/docs/getting-started-guides/aws.html

export KUBE_AWS_ZONE=us-east-1a
export NUM_MINIONS=4
export MINION_SIZE=m4.large
export AWS_S3_REGION=us-east-1
export AWS_S3_BUCKET=dlamblin-kubernetes-artifacts
export INSTANCE_PREFIX=dlamblin-k8s
export KUBERNETES_PROVIDER=aws

curl -sS https://get.k8s.io | bash
