package iloveshopping.demo.order.service;

import iloveshopping.demo.order.config.RabbitMQConfig;
import iloveshopping.demo.order.dto.OrderCreatedEvent;
import iloveshopping.demo.order.dto.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentConsumerService {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void processPayment(OrderCreatedEvent event) {
        boolean isSuccess = event.paymentMethodToken() != null && !event.paymentMethodToken().isBlank();
        String trackingNumber = isSuccess ? "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null;

        PaymentStatusEvent statusEvent = new PaymentStatusEvent(event.orderId(), isSuccess, trackingNumber);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, statusEvent);
    }
}