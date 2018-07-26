package org.roda.core.events;

import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public class EventsManager implements EventsNotifier, EventsHandler {
  private static final long serialVersionUID = 3733394744862836327L;

  private EventsNotifier eventsNotifier;
  private EventsHandler eventsHandler;
  private NodeType nodeType;
  private ModelService model;

  public EventsManager(EventsNotifier eventsNotifier, EventsHandler eventsHandler, NodeType nodeType,
    ModelService model) {
    this.eventsNotifier = eventsNotifier;
    this.eventsHandler = eventsHandler;
    this.nodeType = nodeType;
    this.model = model;
  }

  @Override
  public void notifyUserCreated(ModelService model, User user, String password) {
    eventsNotifier.notifyUserCreated(model, user, password);
  }

  @Override
  public void handleUserCreated(ModelService model, User user, String password) {
    eventsHandler.handleUserCreated(model, user, password);
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, String password) {
    eventsNotifier.notifyUserUpdated(model, user, password);
  }

  @Override
  public void handleUserUpdated(ModelService model, User user, String password) {
    eventsHandler.handleUserUpdated(model, user, password);
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, String password) {
    eventsNotifier.notifyMyUserUpdated(model, user, password);
  }

  @Override
  public void handleMyUserUpdated(ModelService model, User user, String password) {
    eventsHandler.handleMyUserUpdated(model, user, password);
  }

  @Override
  public void notifyUserDeleted(ModelService model, String userID) {
    eventsNotifier.notifyUserDeleted(model, userID);
  }

  @Override
  public void handleUserDeleted(ModelService model, String userID) {
    eventsHandler.handleUserDeleted(model, userID);
  }

}
