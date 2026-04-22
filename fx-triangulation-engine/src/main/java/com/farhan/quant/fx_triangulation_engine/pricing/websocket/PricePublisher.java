package com.farhan.quant.fx_triangulation_engine.pricing.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PricePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public PricePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(String pair, Object price) {
        messagingTemplate.convertAndSend("/topic/prices/" + pair, price);
    }
}

