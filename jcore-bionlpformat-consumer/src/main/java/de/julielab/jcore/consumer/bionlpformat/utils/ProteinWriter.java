/**
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 */

package de.julielab.jcore.consumer.bionlpformat.utils;

import de.julielab.jcore.types.Gene;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class ProteinWriter {

    private Writer writer;
    private String documentText;
    private Set<String> writtenIds;

    public Writer getFileWriter() {
        return writer;
    }

    public void setFileWriter(Writer writer) {
        this.writer = writer;
    }

    public ProteinWriter(Writer writer, String documentText) {
        super();
        this.writer = writer;
        this.documentText = documentText;
        this.writtenIds = new HashSet<String>();
    }

    public void writeProtein(Gene protein) throws IOException {
        String id = protein.getId();
        if (!writtenIds.contains(id)) {
            writtenIds.add(id);
        }

        String line = protein.getId() + "\tProtein " + protein.getBegin() + " " + protein.getEnd() + "\t"
                + documentText.substring(protein.getBegin(), protein.getEnd()) + "\n";

        writer.write(line);
    }

    public void close() throws IOException {
        writer.close();
    }

    public boolean isWritten(Gene protein) {
        return writtenIds.contains(protein.getId());
    }

}
