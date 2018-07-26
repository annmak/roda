package org.roda.core.events;

import org.roda.core.plugins.orchestrate.akka.AkkaBaseActor;
import org.roda.core.plugins.orchestrate.akka.Messages.AbstractEventMessage;
import org.roda.core.plugins.orchestrate.akka.Messages.EventUserUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

public class AkkaEventsHandler extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEventsHandler.class);

  private EventsHandler eventsHandler;
  private String instanceSenderId;

  public AkkaEventsHandler(EventsHandler eventsHandler, String instanceSenderId) {
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    mediator.tell(new DistributedPubSubMediator.Subscribe(AkkaEventsManager.TOPIC_NAME, getSelf()), getSelf());
    this.eventsHandler = eventsHandler;
    this.instanceSenderId = instanceSenderId;
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    AbstractEventMessage eventMessage = (AbstractEventMessage) msg;
    if (!instanceSenderId.equals(eventMessage.getSenderId())) {
      if (msg instanceof EventUserUpdated) {
        EventUserUpdated userUpdatedMsg = (EventUserUpdated) msg;
        eventsHandler.handleUserUpdated(getModel(), userUpdatedMsg.getUser(), userUpdatedMsg.getPassword());
      } else {
        LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
        unhandled(msg);
      }
    }
  }

}
