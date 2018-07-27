package org.roda.core.events.akka;

import org.roda.core.common.akka.AkkaBaseActor;
import org.roda.core.common.akka.Messages.AbstractEventMessage;
import org.roda.core.common.akka.Messages.EventGroupCreated;
import org.roda.core.common.akka.Messages.EventGroupDeleted;
import org.roda.core.common.akka.Messages.EventGroupUpdated;
import org.roda.core.common.akka.Messages.EventUserCreated;
import org.roda.core.common.akka.Messages.EventUserDeleted;
import org.roda.core.common.akka.Messages.EventUserUpdated;
import org.roda.core.events.EventsHandler;
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
    mediator.tell(new DistributedPubSubMediator.Subscribe(AkkaEventsHandlerAndNotifier.TOPIC_NAME, getSelf()),
      getSelf());
    this.eventsHandler = eventsHandler;
    this.instanceSenderId = instanceSenderId;
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof AbstractEventMessage) {
      AbstractEventMessage eventMessage = (AbstractEventMessage) msg;
      if (!instanceSenderId.equals(eventMessage.getSenderId())) {
        if (msg instanceof EventUserCreated) {
          EventUserCreated userCreatedMsg = (EventUserCreated) msg;
          eventsHandler.handleUserCreated(getModel(), userCreatedMsg.getUser(), userCreatedMsg.getPassword());
        } else if (msg instanceof EventUserUpdated) {
          EventUserUpdated userUpdatedMsg = (EventUserUpdated) msg;
          if (userUpdatedMsg.isMyUser()) {
            eventsHandler.handleMyUserUpdated(getModel(), userUpdatedMsg.getUser(), userUpdatedMsg.getPassword());
          } else {
            eventsHandler.handleUserUpdated(getModel(), userUpdatedMsg.getUser(), userUpdatedMsg.getPassword());
          }
        } else if (msg instanceof EventUserDeleted) {
          EventUserDeleted userDeletedMsg = (EventUserDeleted) msg;
          eventsHandler.handleUserDeleted(getModel(), userDeletedMsg.getId());
        } else if (msg instanceof EventGroupCreated) {
          EventGroupCreated groupCreatedMsg = (EventGroupCreated) msg;
          eventsHandler.handleGroupCreated(getModel(), groupCreatedMsg.getGroup());
        } else if (msg instanceof EventGroupUpdated) {
          EventGroupUpdated groupUpdatedMsg = (EventGroupUpdated) msg;
          eventsHandler.handleGroupUpdated(getModel(), groupUpdatedMsg.getGroup());
        } else if (msg instanceof EventGroupDeleted) {
          EventGroupDeleted groupDeletedMsg = (EventGroupDeleted) msg;
          eventsHandler.handleGroupDeleted(getModel(), groupDeletedMsg.getId());
        } else {
          LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
          unhandled(msg);
        }
      }
    }
  }

}
