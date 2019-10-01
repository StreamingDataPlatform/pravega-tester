/*
 * Copyright (c) 2019 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.pravega.example.tester;

import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A simple application to test basic functionality of Pravega.
 */
public class Tester implements Runnable {
    private static Logger log = LoggerFactory.getLogger(Tester.class);

    private final AppConfiguration config;

    public static void main(String... args) {
        AppConfiguration config = new AppConfiguration(args);
        log.info("config: {}", config);
        Runnable app = new Tester(config);
        app.run();
    }

    public Tester(AppConfiguration appConfiguration) {
        config = appConfiguration;
    }

    public AppConfiguration getConfig() {
        return config;
    }

    public void run() {
        try {
            final int numEvents = 10;
            String streamName = "pravega-tester-" + UUID.randomUUID().toString();
            log.info("streamName={}", streamName);
            // Open stream manager, client factory, reader group manager
            try (StreamManager streamManager = StreamManager.create(getConfig().getClientConfig())) {
                if (getConfig().isCreateScope()) {
                    boolean scopeCreated = streamManager.createScope(getConfig().getDefaultScope());
                    log.info("scopeCreated={}", scopeCreated);
                }
                try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(
                             getConfig().getDefaultScope(),
                             getConfig().getClientConfig());
                     ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(
                             getConfig().getDefaultScope(),
                             getConfig().getClientConfig())) {
                    // Create stream
                    streamManager.createStream(
                            getConfig().getDefaultScope(),
                            streamName,
                            StreamConfiguration.builder().build());
                    // Create writer
                    try (EventStreamWriter<String> pravegaWriter = clientFactory.createEventWriter(
                            streamName,
                            new UTF8StringSerializer(),
                            EventWriterConfig.builder().build())) {
                        // Write events to stream
                        for (int i = 0; i < numEvents; i++) {
                            log.info("Writing event {}", i);
                            CompletableFuture<Void> future = pravegaWriter.writeEvent(Integer.toString(i));
                            future.get();
                        }
                    }
                    // Create reader group
                    final String readerGroup = UUID.randomUUID().toString().replace("-", "");
                    final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                            .stream(Stream.of(getConfig().getDefaultScope(), streamName))
                            .build();
                    readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
                    // Create reader
                    try (EventStreamReader<String> reader = clientFactory.createReader("reader",
                            readerGroup,
                            new UTF8StringSerializer(),
                            ReaderConfig.builder().build())) {
                        // Read events from stream
                        int numEventsRead = 0;
                        while (numEventsRead < numEvents) {
                            EventRead<String> event = reader.readNextEvent(1000);
                            if (event.getEvent() != null) {
                                log.info("Read event {}", event.getEvent());
                                numEventsRead++;
                            }
                        }
                    }
                    // Delete reader group
                    readerGroupManager.deleteReaderGroup(readerGroup);
                    // Delete stream
                    streamManager.sealStream(getConfig().getDefaultScope(), streamName);
                    streamManager.deleteStream(getConfig().getDefaultScope(), streamName);
                }
            }
            log.info("PRAVEGA TESTER COMPLETED SUCCESSFULLY.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
