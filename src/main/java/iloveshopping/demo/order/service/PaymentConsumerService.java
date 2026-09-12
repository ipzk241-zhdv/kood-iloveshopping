package iloveshopping.demo.order.service;

import iloveshopping.demo.order.config.RabbitMQConfig;
import iloveshopping.demo.order.dto.OrderCreatedEvent;
import iloveshopping.demo.order.dto.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PaymentConsumerService {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumerService.class);

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void processPayment(OrderCreatedEvent event) {
        // Sandbox tokens deliberately model common gateway responses. No PAN, expiry, or CVV enters this service.
        String token = event.paymentMethodToken();
        boolean isSuccess = token != null && !token.isBlank()
                && !token.equalsIgnoreCase("tok_insufficient_funds")
                && !token.equalsIgnoreCase("tok_invalid_card")
                && !token.equalsIgnoreCase("tok_expired_card")
                && !token.equalsIgnoreCase("tok_gateway_timeout");
        String trackingNumber = isSuccess ? "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null;

        PaymentStatusEvent statusEvent = new PaymentStatusEvent(event.orderId(), isSuccess, trackingNumber);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, statusEvent);
        log.info("Payment notification queued for order {}: {}", event.orderId(), isSuccess ? "successful" : "failed");
    }
}
