#!/bin/bash
# Set Java 21 for this project

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Java version set to:"
java -version

# Run Maven with the provided arguments or default to spring-boot:run
if [ $# -eq 0 ]; then
    ./mvnw spring-boot:run
else
    ./mvnw "$@"
fi

