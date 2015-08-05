#!/bin/bash

if [ -f "$TMPDIR/HAL_TS_WF.pid" ]; then
    export pid=$(cat $TMPDIR/HAL_TS_WF.pid)
    echo "PID is: $pid"
    for child in $(ps -o pid,ppid -ax | awk -v x=$pid '{ if ( $2 == x ) { print $1 }}')
    do
        echo "Killing child process $child because ppid = $pid"
        kill $child
    done
else
    echo "No PID file $TMPDIR/HAL_TS_WF.pid"
fi