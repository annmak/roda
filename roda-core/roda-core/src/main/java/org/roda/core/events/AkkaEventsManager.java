package org.roda.core.events;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.akka.Messages.EventUserCreated;
import org.roda.core.plugins.orchestrate.akka.Messages.EventUserUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

public class AkkaEventsManager extends AbstractEventsHandler implements EventsNotifier {
  private static final long serialVersionUID = 919188071375009042L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEventsManager.class);

  protected static final String TOPIC_NAME = "events";

  private ActorSystem eventsSystem;
  private ActorRef eventsNotifier;
  private ActorRef eventsHandler;
  private String instanceSenderId;

  public AkkaEventsManager() {
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
    LOGGER.info("notifyUserCreated '{}'", user);
    // FIXME create proper messages
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserCreated(user, password, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("notifyUserUpdated '{}' with password ''", user, password != null ? "******" : "NULL");
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserUpdated(user, password, false, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("notifyMyUserUpdated '{}' with password ''", user, password != null ? "******" : "NULL");
    eventsNotifier.tell(
      new DistributedPubSubMediator.Publish(TOPIC_NAME, new EventUserUpdated(user, password, true, instanceSenderId)),
      ActorRef.noSender());
  }

  @Override
  public void notifyUserDeleted(ModelService model, String userID) {
    LOGGER.info("notifyUserDeleted '{}'", userID);
    // FIXME create proper messages
    eventsNotifier.tell(new DistributedPubSubMediator.Publish(TOPIC_NAME, userID), ActorRef.noSender());
  }

  @Override
  public void handleUserCreated(ModelService model, User user, String password) {
    LOGGER.info("handleUserCreated '{}'", user);
    super.handleUserCreated(model, user, password);
  }

  @Override
  public void handleUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("handleUserUpdated '{}' with password ''", user, password != null ? "******" : "NULL");
    super.handleUserUpdated(model, user, password);
  }

  @Override
  public void handleMyUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("handleMyUserUpdated '{}' with password ''", user, password != null ? "******" : "NULL");
    super.handleMyUserUpdated(model, user, password);
  }

  @Override
  public void handleUserDeleted(ModelService model, String userID) {
    LOGGER.info("handleUserDeleted '{}'", userID);
    super.handleUserDeleted(model, userID);
  }

}
