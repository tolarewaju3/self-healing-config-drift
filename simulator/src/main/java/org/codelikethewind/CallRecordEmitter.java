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

    @ConfigProperty(name = "MAX_ACTIVE_CALLS", defaultValue = "-1")
    int maxActiveCalls;

    Random rand = new Random();

    @Scheduled(every = "1s", delayed = "5s")
    void emit() {
        if (!control.isEmitterEnabled()) {
            return;
        }

        double dropRate = control.getDropRate();
        boolean isDropped = rand.nextDouble() < dropRate;

        // Check if we've exceeded max active calls (only if limit is enabled)
        int currentActiveCalls = control.getActiveCallCount();
        if (maxActiveCalls > 0 && currentActiveCalls >= maxActiveCalls) {
            isDropped = true;
            System.out.println("[CallRecordEmitter] Max active calls (" + maxActiveCalls + ") reached. Dropping call.");
        }

        String callRecord = CallRecordGenerator.getCallRecord(cityCode, isDropped);

        emitter.send(callRecord);

        // Only increment active calls for non-dropped calls
        if (!isDropped) {
            control.incrementActiveCalls();
        }

        System.out.println("[CallRecordEmitter] Emitted: " + callRecord + " (Active calls: " + control.getActiveCallCount() + "/" + maxActiveCalls + ")");
    }

    // Simulate calls ending - decrement active calls periodically
    // End 3 calls every 3s to balance with 1 call/second emit rate
    @Scheduled(every = "3s", delayed = "10s")
    void endCalls() {
        int ended = 0;
        for (int i = 0; i < 3; i++) {
            if (control.getActiveCallCount() > 0) {
                control.decrementActiveCalls();
                ended++;
            }
        }
        if (ended > 0) {
            System.out.println("[CallRecordEmitter] " + ended + " call(s) ended. Active calls: " + control.getActiveCallCount());
        }
    }

    void onStart(@Observes StartupEvent ev) {
        System.out.println("-----------------------------");
        System.out.println("Starting Call Record Emitter");
        System.out.println("Drop Rate: " + control.getDropRate());
        System.out.println("Max Active Calls: " + maxActiveCalls);
        System.out.println("Kafka URL: " + kafkaUrl);
        System.out.println("Kafka Topic: " + kafkaTopic);
    }
}
