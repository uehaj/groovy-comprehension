#!/bin/sh
# groovyc -d build/classes/main src/main/groovy/groovyx/comprehension/*/*.groovy
# mkdir -p build/libs
# jar cf build/libs/groovy-comprehension-0.1.jar -C build/classes/main groovyx -C src/main/resources/ META-INF

gradle -q build
