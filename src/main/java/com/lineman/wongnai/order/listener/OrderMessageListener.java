package com.lineman.wongnai.order.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.producer.SqsMessageProducer;
import com.lineman.wongnai.order.repository.DriverRepository;
import com.lineman.wongnai.order.service.OrderService;
import com.lineman.wongnai.order.service.dto.OrderDTO;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderMessageListener {

    private static final ObjectMapper OBJECT_MAPPER = Jackson2ObjectMapperBuilder.json().build();
    private final OrderService orderService;
    
    private final SqsMessageProducer producer;
    
    private final DriverRepository blockingDriverRepositoryy;

    public OrderMessageListener(OrderService orderService, DriverRepository blockingDriverRepositoryy, SqsMessageProducer producer) {
        this.orderService = orderService;
        this.blockingDriverRepositoryy = blockingDriverRepositoryy;
        this.producer = producer;
        
    }

    @SqsListener(value = "${orders.queue.name}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void processMessage(String message) throws Exception {
        
            log.debug("Received new SQS message: {}", message );
            OrderDTO orderDto = OBJECT_MAPPER.readValue(message, OrderDTO.class);
            
            if(orderDto.getStatus().equals(OrderStatus.CANCELLED)) {
            	
            	
            	this.orderService.cancelOrder(orderDto);
            } else if(orderDto.getStatus().equals(OrderStatus.DELIVERED)) {
            	
            
            	this.orderService.completeOrder(orderDto);
            }
            else {
            List<Driver> drivers = blockingDriverRepositoryy.findAll().stream().filter(d-> !d.getStatus().equals("ASSIGNED")).collect(Collectors.toList());
            if(drivers!= null && drivers.size()>0) {
            	
            this.orderService.processOrder(orderDto, drivers);
            } 
            }
        
    }
    
   
    @SqsListener(value = "dead-letter.fifo", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void processFailedMessage(String message, Acknowledgment ack) {
        try {
            log.debug("Received new Failed SQS message: {}", message );
            OrderDTO orderDto = OBJECT_MAPPER.readValue(message, OrderDTO.class);
            List<Driver> drivers = blockingDriverRepositoryy.findAll().stream().filter(d-> !d.getStatus().equals("ASSIGNED")).collect(Collectors.toList());
           
            
            if(drivers!= null && drivers.size()>0) {
            	 ack.acknowledge();
            	 this.orderService.processOrder(orderDto, drivers);
            	 
            } 
          

        } catch (Exception e) {
            throw new RuntimeException("Cannot process message from SQS", e);
        }
    }
}
