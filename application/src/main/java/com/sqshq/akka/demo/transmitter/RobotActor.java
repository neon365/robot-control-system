package com.sqshq.akka.demo.transmitter;

import akka.actor.AbstractActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.sqshq.akka.demo.config.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Actor
public class RobotActor extends AbstractActor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WebSocketSession session;
    private final Long robotId;

    public RobotActor(WebSocketSession session, Long robotId) {
        this.session = session;
        this.robotId = robotId;
        DistributedPubSub.get(getContext().system())
                .mediator()
                .tell(new DistributedPubSubMediator.Subscribe(
                        robotId.toString(), getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TextMessage.class, this::processMessageFromRobot)
                .match(String.class, this::sendMessageToRobot)
                .match(DistributedPubSubMediator.SubscribeAck.class, this::processSubscription)
                .matchAny(this::unhandled)
                .build();
    }

    private void processMessageFromRobot(TextMessage message) {
        log.info("received message from robot {}: {}", session.getId(), message);
    }

    private void sendMessageToRobot(String message) throws IOException {
        log.info("send message to robot {}: {}", robotId, message);
        session.sendMessage(new TextMessage(message));
    }

    private void processSubscription(DistributedPubSubMediator.SubscribeAck ack) {
        log.info("robot #{} sucessfully subscribed", robotId);
    }
}