# HAL testsuite – Transactions module

This module is intended for testing prepared transactions in HAL. Due to the fact, that it is difficult to obtain
prepared transactions, there is already prepared zip with journal which has broken transactions.

## How to run
See [main README](../README.md) first!

When running this module, the server has to have already replaced its journal by the one from attached zip 
`artemis-journal.zip`.

You can do this either by

* running `./prepare-and-start-server.sh -t <path to prepared journal> -u <path to unzipped EAP>` which also starts and
unsecures server. Use optional `-d` argument to prepare and start server in domain mode instead of standalone.
* *manually*: Extract the archive to `$EAP_HOME/standalone/data`

All needed files can be found in `src/main/resources`