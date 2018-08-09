#!/bin/bash
cd "$( dirname "${BASH_SOURCE[0]}" )"
output=$(which RScript)
#echo $output
java -XstartOnFirstThread -jar DEC_Mac.jar "$output"
