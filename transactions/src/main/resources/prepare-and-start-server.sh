#!/usr/bin/env bash

CONFIG_FILE="standalone-full-ha.xml"

while getopts "u:t:" opt; do
    case ${opt} in
        t)  TRANSACTIONS_JOURNAL_ZIP_PATH="$OPTARG"
            ;;
        u)
            EAP_HOME="$OPTARG"
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            ;;
    esac
done

INSTALL_PATH="${EAP_HOME}/standalone/data"

# Unzip and overwrite existing files without prompting
unzip -o ${TRANSACTIONS_JOURNAL_ZIP_PATH} -d ${INSTALL_PATH}

# Unsecure specified config file
sed "s/http-interface security-realm=\"ManagementRealm\"/http-interface/g" -i "${EAP_HOME}/standalone/configuration/${CONFIG_FILE}"

# Start EAP in standalone mode with specified config file
sh "${EAP_HOME}/bin/standalone.sh" -c ${CONFIG_FILE}

