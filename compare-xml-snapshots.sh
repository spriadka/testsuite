#!/bin/bash

usage="$(basename "$0") [-h] [-r|-i FILE]  -- creates a user friendly report file from snapshots taken with enabled snapshots profile.

where:
    -h  show this help text
    -r  sets target path for generating report file
    -i  sets path to initial server configuration which will be used to create diff from first snapshot taken after first test class, otherwise the first taken snapshot will be omitted"


REPORT_FILE="snapshot_report.txt"
INITIAL_FILE=""
CANONICAL_FILE_SUFFIX="_canonical"
FILES=(??:??:??_*/*.xml);

while getopts 'hr:i:' flag; do
    case "${flag}" in
        h) echo "$usage"; exit 1;;
        r) REPORT_FILE="${OPTARG}" ;;
        i) INITIAL_FILE="${OPTARG}" ;;
        *) echo "$usage"; exit 1;;
    esac
done

# No parameters were passed
if [ $OPTIND -eq 1 ]; then echo "$usage"; exit; fi

saveDiffToReportFile () {
    PREVIOUS_PATH=$1
    CURRENT_PATH=$2
    IFS='_' read -ra split_path <<< "${CURRENT_PATH}" # split path to ['<time>', '<classname>/<file>.xml']
	IFS='/' read -ra classname <<< "${split_path[1]}" # extract classname
	timestamp=${split_path[0]}
	echo "================================================================================" >> ${REPORT_FILE}
	echo " $(printf "%03d" $i).  $timestamp  $classname" >> ${REPORT_FILE}
	diff "${PREVIOUS_PATH}" "${CURRENT_PATH}" >> ${REPORT_FILE}
}

createCanonicalXMLFile () {
    xmllint --exc-c14n ${1} > "${1}${CANONICAL_FILE_SUFFIX}"
}

# Erase contents of report file if created previously
> ${REPORT_FILE}

# Turn xml files into canonical XMl format to ensure seamless diff
for file in ${FILES[@]}; do
    createCanonicalXMLFile ${file}
done

echo " _____                       _   " >> ${REPORT_FILE}
echo "|  __ \                     | |  " >> ${REPORT_FILE}
echo "| |__) |___ _ __   ___  _ __| |_ " >> ${REPORT_FILE}
echo "|  _  // _ \ '_ \ / _ \| '__| __|" >> ${REPORT_FILE}
echo "| | \ \  __/ |_) | (_) | |  | |_ " >> ${REPORT_FILE}
echo "|_|  \_\___| .__/ \___/|_|   \__|" >> ${REPORT_FILE}
echo "           | |                   " >> ${REPORT_FILE}
echo "	         |_|                   " >> ${REPORT_FILE}
echo "" >> ${REPORT_FILE}

# If user supplies an initial file, compare first test with it
if [ -n "${INITIAL_FILE}" ] && [ -a "${INITIAL_FILE}" ]; then
    createCanonicalXMLFile ${INITIAL_FILE}
    saveDiffToReportFile "${INITIAL_FILE}${CANONICAL_FILE_SUFFIX}" "${FILES[0]}${CANONICAL_FILE_SUFFIX}"
    echo "Created comparation of first test xml snapshot with initial xml file!"
fi

for ((i=1; i < ${#FILES[@]}; i++)); do
    saveDiffToReportFile "${FILES[$i - 1]}${CANONICAL_FILE_SUFFIX}" "${FILES[$i]}${CANONICAL_FILE_SUFFIX}"
done
