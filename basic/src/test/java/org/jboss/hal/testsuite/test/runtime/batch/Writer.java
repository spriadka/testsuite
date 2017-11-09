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

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;

/**
 * Batch {@link AbstractItemWriter} to be used in testing batch jobs in {@link BatchManagementTestCase}
 * @author pjelinek
 */
@Named("testWriter")
public class Writer extends AbstractItemWriter {

    private Logger log = Logger.getLogger(Writer.class.getName());

    private Long checkpoint;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        if (checkpoint == null) {
            log.info("Empty checkpoint!");
            this.checkpoint = 0L;
        } else {
            this.checkpoint = (Long) checkpoint;
        }
        log.info("Checkpoint " + checkpoint);
    }

    @Override
    public void writeItems(List<Object> items) throws Exception {
        items.forEach(it -> {
            log.info("Writing " + it);
            });
        checkpoint++;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return checkpoint;
    }

}
