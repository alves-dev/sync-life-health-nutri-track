package synclife.health.nutritrack.rabbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import synclife.health.nutritrack.event.v1.EventBaseV1;
import synclife.health.nutritrack.event.v1.EventTypeV1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
class ConsumerV1 implements Consumer {

    private static final Logger log = LoggerFactory.getLogger(ConsumerV1.class);

    private final ObjectMapper objectMapper;
    private final Event<EventBaseV1> eventPub;

    ConsumerV1(ObjectMapper objectMapper, Event<EventBaseV1> eventPub) {
        this.objectMapper = objectMapper;
        this.eventPub = eventPub;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        log.debug(consumerTag);
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        log.debug(consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        log.debug(consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        log.debug(consumerTag);
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        log.debug(consumerTag);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String messageString = new String(body, StandardCharsets.UTF_8);

        log.debug("Received: {}", messageString);

        try {
            JsonNode node = objectMapper.readTree(messageString);
            String typeValue = node.get("type").asText();

            Class<? extends EventBaseV1> eventClass = EventTypeV1.getEventClass(typeValue);
            EventBaseV1 event = objectMapper.readValue(messageString, eventClass);

            eventPub.fire(event);
        } catch (Exception e) {
            log.warn("[RabbitMqListener] Error parsing: {}", messageString);
            log.warn(e.toString());
        }
    }
}
