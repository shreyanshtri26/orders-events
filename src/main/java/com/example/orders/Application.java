package com.example.orders;

import com.example.orders.ingestion.EventIngestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private EventIngestor eventIngestor;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Default file if not provided via args
        String path = args != null && args.length > 0 ? args : "src/main/resources/events.jsonl";
        eventIngestor.ingest(path);
    }
}
