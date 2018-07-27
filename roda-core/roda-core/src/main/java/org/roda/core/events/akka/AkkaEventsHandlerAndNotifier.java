package org.roda.core.events.akka;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.akka.Messages.EventGroupCreated;
import org.roda.core.common.akka.Messages.EventGroupDeleted;
import org.roda.core.common.akka.Messages.EventGroupUpdated;
import org.roda.core.common.akka.Messages.EventUserCreated;
import org.roda.core.common.akka.Messages.EventUserDeleted;
import org.roda.core.common.akka.Messages.EventUserUpdated;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.events.AbstractEventsHandler;
import org.roda.core.events.EventsHandler;
import org.roda.core.events.EventsNotifier;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.dispatch.OnComplete;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AkkaEventsHandlerAndNotifier extends AbstractEventsHandler implements EventsNotifier {
  private static final long serialVersionUID = 919188071375009042L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEventsHandlerAndNotifier.class);

  protected static final String TOPIC_NAME = "events";

  private ActorSystem eventsSystem;
  private ActorRef eventsNotifier;
  private ActorRef eventsHandler;
  private String instanceSenderId;
  private boolean shuttingDown = false;

  public AkkaEventsHandlerAndNotifier() {
    Config akkaConfig = getAkkaConfiguration();
    eventsSystem = ActorSystem.create("EventsSystem", akkaConfig);
    eventsNotifier = DistributedPubSub.get(eventsSystem).mediator();
    instanceSenderId = eventsNotifier.toString();
    eventsHandler = eventsSystem
      .actorOf(Props.create(AkkaEventsHandler.class, (EventsHandler) this, eventsNotifier.toString()), "eventsHandler");
  }

  private Config getAkkaConfiguration() {
    Config akkaConfig = null;

    try (InputStream originStream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CORE_ORCHESTRATOR_FOLDER + "/events.conf")) {
      String configAsString = IOUtils.toString(originStream, RodaConstants.DEFAULT_ENCODING);
      akkaConfig = ConfigFactory.parseString(configAsString);
    } catch (IOException e) {
      LOGGER.error("Could not load Akka configuration", e);
    }

    return akkaConfig;
  }

  @Override
  public void notifyUserCreated(ModelService model, User user, String password) {
    LOGGER.debug("notifyUserCreated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserCreated(user, password, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, String password) {
    LOGGER.debug("notifyUserUpdated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserUpdated(user, password, false, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, String password) {
    LOGGER.debug("notifyMyUserUpdated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserUpdated(user, password, true, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserDeleted(ModelService model, String id) {
    LOGGER.debug("notifyUserDeleted '{}'", id);
    eventsNotifier.tell(new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserDeleted(id, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyGroupCreated(ModelService model, Group group) {
    LOGGER.debug("notifyGroupCreated '{}'", group);
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventGroupCreated(group, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyGroupUpdated(ModelService model, Group group) {
    LOGGER.debug("notifyGroupUpdated '{}'", group);
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventGroupUpdated(group, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyGroupDeleted(ModelService model, String id) {
    LOGGER.debug("notifyGroupDeleted '{}'", id);
    eventsNotifier.tell(new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventGroupDeleted(id, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void shutdown() {
    if (!shuttingDown) {
      shuttingDown = true;

      LOGGER.info("Going to shutdown EVENTS actor system");
      Future<Terminated> terminate = eventsSystem.terminate();
      terminate.onComplete(new OnComplete<Terminated>() {
        @Override
        public void onComplete(Throwable failure, Terminated result) {
          if (failure != null) {
            LOGGER.error("Error while shutting down EVENTS actor system", failure);
          } else {
            LOGGER.info("Done shutting down EVENTS actor system");
          }
        }
      }, eventsSystem.dispatcher());

      try {
        LOGGER.info("Waiting up to 30 seconds for EVENTS actor system  to shutdown");
        Await.result(eventsSystem.whenTerminated(), Duration.create(30, "seconds"));
      } catch (TimeoutException e) {
        LOGGER.warn("EVENTS Actor system shutdown wait timed out, continuing...");
      } catch (Exception e) {
        LOGGER.error("Error while shutting down EVENTS actor system", e);
      }
    }
  }

}
