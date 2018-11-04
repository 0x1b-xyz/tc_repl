#!/bin/sh

set -e

STACK=$(basename ${PWD})
URL="http://localhost:8080"
COOKIES="target/cookies"
CURL="curl -b ${COOKIES} -c ${COOKIES} -s ${URL}"

case $1 in
    rename)
        [[ -z "$2" ]] && echo "Must specify new name" && exit 1
        ${CURL}/?name=$2
        echo
        exit 0;;
    scale)
        [[ -z "$2" ]] && echo "Must specify number of instances" && exit 1
        docker service scale ${STACK}_app=$2
        exit 0;;
    watch)
        echo "Ok now we round robin ..."
        watch -d ${CURL}
        exit 0;;
    get)
        while true; do
            ${CURL}
            echo
            sleep 2
        done
        exit 0;;
    *)
        echo "Building an starting ${STACK} ...";;
esac


docker stack rm ${STACK} || :

echo "Gotta give the swarm a moment to recover ..."
sleep 5
docker rm -f $(docker ps -aq) || :

mvn clean package && docker-compose build && docker stack deploy -c docker-compose.yml ${STACK}

echo "Waiting for stack to come up ..."
until $(curl --output /dev/null --silent --head --fail ${URL}); do
    echo "Still waiting ..."
    sleep 2
done

echo "Resetting the cookies file ..."
rm ${COOKIES} || :

echo "Setting the initial name value into the session ..."
$0 rename Jason

echo "Ok now we round robin ..."
$0 watch