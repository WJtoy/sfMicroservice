package com.kkl.kklplus.b2b.sf.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class B2BCenterOrderMQConfig{

    @Bean
    public Queue b2bCenterOrderQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER, true);
    }

    @Bean
    DirectExchange b2bCenterOrderExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER);
    }

    @Bean
    Binding bindingB2BCenterOrderExchangeMessage(Queue b2bCenterOrderQueue, DirectExchange b2bCenterOrderExchange) {
        return BindingBuilder.bind(b2bCenterOrderQueue)
                .to(b2bCenterOrderExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER);
    }

}
