package com.kartik.EventAnalyzer.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartik.EventAnalyzer.model.Alert;
import com.kartik.EventAnalyzer.model.Event;
import com.kartik.EventAnalyzer.repository.EventRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Component
public class EventService {

    Map<String, Event> eventMap = new ConcurrentHashMap<>();

    Map<String, Event> tempMap = new ConcurrentHashMap<>();

    Map<String, Alert> alerts = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository alertRepository;

    public void process(String filePath){

        ExecutorService executorService= Executors.newFixedThreadPool(4);


        LOGGER.info("Reading from Logfile.");
        try (LineIterator li = FileUtils.lineIterator(new ClassPathResource(filePath).getFile())) {
            String line = null;

            while (li.hasNext()) {
                Event event;

                event = new ObjectMapper().readValue(li.nextLine(), Event.class);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute(event);
                    }
                });

                if (alerts.size() > 3000) {
                    save(alerts.values());
                    alerts = new HashMap<>();
                }
            }
            if (alerts.size() != 0) {
                save(alerts.values());
            }
        } catch (IOException e) {
            LOGGER.error("!!! Unable to access the file: {}", e.getMessage());
        }
        executorService.shutdown();

    }

    public void execute(Event event) {
        if (eventMap.containsKey(event.getId())) {
            Event e1 = eventMap.get(event.getId());
            long executionTime = calculateEventExecutionTime(event, e1);
            Alert alert = new Alert(event, Math.toIntExact(executionTime));
            if (executionTime > 4) {
                alert.setAlert(Boolean.TRUE);
                LOGGER.trace("!!! Execution time for the event {} is {}ms", event.getId(), executionTime);
            }
            alerts.put(event.getId(), alert);
            eventMap.remove(event.getId());
        } else {
            Event event2=eventMap.putIfAbsent(event.getId(), event);
            if(event2!=null)
            {
                execute(event);
            }
        }
    }

    private void save(Collection<Alert> alerts) {
        LOGGER.debug("Saving alerts to database", alerts.size());
        alertRepository.saveAll(alerts);
    }

    private long calculateEventExecutionTime(Event event1, Event event2) {
        Event endEvent = Stream.of(event1, event2).filter(e -> "FINISHED".equals(e.getState())).findFirst().orElse(null);
        Event startEvent = Stream.of(event1, event2).filter(e -> "STARTED".equals(e.getState())).findFirst().orElse(null);

        return Objects.requireNonNull(endEvent).getTimestamp() - Objects.requireNonNull(startEvent).getTimestamp();
    }

}
