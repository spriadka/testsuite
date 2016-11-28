# HAL testsuite – Transactions module

This module is intended for testing prepared transactions in HAL. Due to the fact, that it is difficult to obtain prepared transactions for testing, there is used prepared journal with transactions in it.

Because of its nature it is run *only* in standalone mode.

## How to run
See [main README](../README.md) first!

When running this module, the server has to have already replaced its journal by the one from attached zip `artemis-journal.zip`.

You can do this either by

* running `./prepare-and-start-server.sh -t <path to prepared journal> -u <path to unzipped EAP>`
* *manually*: Extract the archive to `$EAP_HOME/standalone/data`

All needed files can be found in `src/main/resources`