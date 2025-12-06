#!/bin/bash
# Start backend with environment variables

cd "$(dirname "$0")"
source load-env.sh
mvn spring-boot:run -DskipTests
