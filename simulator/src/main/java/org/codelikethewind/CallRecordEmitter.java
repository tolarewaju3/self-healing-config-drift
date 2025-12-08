package org.codelikethewind;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.quarkus.scheduler.Scheduled;

import java.util.Random;

@ApplicationScoped
public class CallRecordEmitter {

    @Inject
    CallControlManager control;

    @Inject
    @Channel("callrecord-out")
    Emitter<String> emitter;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaUrl;

    @ConfigProperty(name = "mp.messaging.outgoing.callrecord-out.topic")
    String kafkaTopic;

    @ConfigProperty(name = "CITY_CODE", defaultValue = "ATX")
    String cityCode;

    Random rand = new Random();

    @Scheduled(every = "1s", delayed = "5s")
    void emit() {
        if (!control.isEmitterEnabled()) {
            return;
        }

        double dropRate = control.getDropRate();
        boolean isDropped = rand.nextDouble() < dropRate;

        String callRecord = CallRecordGenerator.getCallRecord(cityCode, isDropped);

        emitter.send(callRecord);

        System.out.println("[CallRecordEmitter] Emitted: " + callRecord);
    }

    void onStart(@Observes StartupEvent ev) {
        System.out.println("-----------------------------");
        System.out.println("Starting Call Record Emitter");
        System.out.println("Drop Rate: " + control.getDropRate());
        System.out.println("Kafka URL: " + kafkaUrl);
        System.out.println("Kafka Topic: " + kafkaTopic);

    }
}
