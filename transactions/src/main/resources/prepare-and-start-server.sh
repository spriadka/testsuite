#!/usr/bin/env bash
# Prepares server by copying journal containing prepared transactions to its data directory, unsecures it and starts it.

while getopts "u:t:d" opt; do
    case ${opt} in
        t)  TRANSACTIONS_JOURNAL_ZIP_PATH="$OPTARG"
            ;;
        u)
            EAP_HOME="$OPTARG"
            ;;
        d)  DOMAIN_MODE=false
            ;;
        \?)
            echo "Usage:"
            echo "$0 -t <PATH_TO_TRANSACTION_ZIP> -u <PATH_TO_UNZIPPED_EAP> [-d]"
            exit 1
            ;;
    esac
done

if [ ${DOMAIN_MODE} ]; then
    CONFIG_FILE="host.xml"
    CONFIG_PATH="${EAP_HOME}/domain/configuration/${CONFIG_FILE}"
    INSTALL_PATH="${EAP_HOME}/domain/servers/server-one/data"
    START_BIN_PATH="${EAP_HOME}/bin/domain.sh"
    START_OPTS=""
else
    CONFIG_FILE="standalone-full-ha.xml"
    CONFIG_PATH="${EAP_HOME}/standalone/configuration/${CONFIG_FILE}"
    INSTALL_PATH="${EAP_HOME}/standalone/data"
    START_BIN_PATH="${EAP_HOME}/bin/standalone.sh"
    START_OPTS="-c ${CONFIG_FILE}"
fi

# Unzip and overwrite existing files without prompting
unzip -o ${TRANSACTIONS_JOURNAL_ZIP_PATH} -d ${INSTALL_PATH}

# Unsecure specified config file
sed "s/http-interface security-realm=\"ManagementRealm\"/http-interface/g" -i "${CONFIG_PATH}"

# Start EAP in standalone mode with specified config file
sh ${START_BIN_PATH} ${START_OPTS}
