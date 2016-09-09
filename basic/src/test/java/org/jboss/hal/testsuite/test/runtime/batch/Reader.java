/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.hal.testsuite.test.runtime.batch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Logger;

import javax.batch.api.chunk.ItemReader;
import javax.inject.Named;

@Named("testReader")
public class Reader implements ItemReader {

    private BufferedReader br;

    private Checkpoint checkpoint;

    private Logger log = Logger.getLogger(Reader.class.getName());

    @Override
    public void open(Serializable previousCheckpoint) throws Exception {
     // Verify if we have an previous checkpoint
        if (previousCheckpoint == null) {
            this.checkpoint = new Checkpoint();
        }
        else {
            this.checkpoint = (Checkpoint) previousCheckpoint;
        }
        br = new BufferedReader(new InputStreamReader(Reader.class.getResourceAsStream("names.txt")));
        long lineNumber = checkpoint.getLineNumber();
        if (lineNumber > 0) {
            log.info("Skipping to line " + lineNumber + " as marked by previous checkpoint");
        }
        for (long i = 0; i < lineNumber; i++) {
            br.readLine();
        }
    }

    @Override
    public void close() throws Exception {
        br.close();
    }

    @Override
    public String readItem() throws Exception {
        String line = br.readLine();
        if (line != null) {
            log.info("Reading " + line);
            checkpoint.increase();
            return line;
        }
        return null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return checkpoint;
    }

}
