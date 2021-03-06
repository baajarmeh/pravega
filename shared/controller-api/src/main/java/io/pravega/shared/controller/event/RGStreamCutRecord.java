/**
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.shared.controller.event;

import com.google.common.collect.ImmutableMap;
import io.pravega.common.ObjectBuilder;
import io.pravega.common.io.serialization.RevisionDataInput;
import io.pravega.common.io.serialization.RevisionDataOutput;
import io.pravega.common.io.serialization.VersionedSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * This is data class for storing stream cuts (starting and ending) related to a ReaderGroup.
 */
@Data
@Slf4j
public class RGStreamCutRecord {
    public static final RGStreamCutRecordSerializer SERIALIZER = new RGStreamCutRecordSerializer();
    /**
     * Actual Stream cut.
     */
    final ImmutableMap<Long, Long> streamCut;

    @Builder
    public RGStreamCutRecord(@NonNull ImmutableMap<Long, Long> streamCut) {
        this.streamCut = streamCut;
    }

    public Map<Long, Long> getStreamCut() {
        return Collections.unmodifiableMap(streamCut);
    }

    private static class RGStreamCutRecordBuilder implements ObjectBuilder<RGStreamCutRecord> {

    }

    @SneakyThrows(IOException.class)
    public static RGStreamCutRecord fromBytes(final byte[] data) {
        return SERIALIZER.deserialize(data);
    }

    @SneakyThrows(IOException.class)
    public byte[] toBytes() {
        return SERIALIZER.serialize(this).getCopy();
    }

    public static class RGStreamCutRecordSerializer
            extends VersionedSerializer.WithBuilder<RGStreamCutRecord, RGStreamCutRecordBuilder> {
        @Override
        protected byte getWriteVersion() {
            return 0;
        }

        @Override
        protected void declareVersions() {
            version(0).revision(0, this::write00, this::read00);
        }

        private void read00(RevisionDataInput revisionDataInput, RGStreamCutRecordBuilder streamCutRecordBuilder)
                throws IOException {
            ImmutableMap.Builder<Long, Long> streamCutBuilder = ImmutableMap.builder();
            revisionDataInput.readMap(DataInput::readLong, DataInput::readLong, streamCutBuilder);
            streamCutRecordBuilder.streamCut(streamCutBuilder.build());
        }

        private void write00(RGStreamCutRecord streamCutRecord, RevisionDataOutput revisionDataOutput) throws IOException {
            revisionDataOutput.writeMap(streamCutRecord.getStreamCut(), DataOutput::writeLong, DataOutput::writeLong);
        }

        @Override
        protected RGStreamCutRecordBuilder newBuilder() {
            return RGStreamCutRecord.builder();
        }
    }

}
