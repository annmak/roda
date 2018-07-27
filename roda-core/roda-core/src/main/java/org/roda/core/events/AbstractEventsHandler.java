package org.roda.core.events;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventsHandler implements EventsHandler {
  private static final long serialVersionUID = -1284727831525932207L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventsHandler.class);

  @Override
  public void handleUserCreated(ModelService model, User user, String password) {
    try {
      model.createUser(user, password, true, true);
    } catch (EmailAlreadyExistsException | UserAlreadyExistsException | IllegalOperationException | GenericException
      | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user created event", e);
    }
  }

  @Override
  public void handleUserUpdated(ModelService model, User user, String password) {
    try {
      model.updateUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    }
  }

  @Override
  public void handleMyUserUpdated(ModelService model, User user, String password) {
    try {
      model.updateMyUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    }
  }

  @Override
  public void handleUserDeleted(ModelService model, String userID) {
    try {
      model.deleteUser(userID, true, true);
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user deleted event", e);
    }
  }

  public void handleGroupCreated(ModelService model, Group group) {
    try {
      model.createGroup(group, true, true);
    } catch (GenericException | AlreadyExistsException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling create group event", e);
    }
  }

  public void handleGroupUpdated(ModelService model, Group group) {
    try {
      model.updateGroup(group, true, true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling update group event", e);
    }
  }

  public void handleGroupDeleted(ModelService model, String id) {
    try {
      model.deleteGroup(id, true, true);
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling delete group event", e);
    }
  }

}
