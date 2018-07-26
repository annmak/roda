package org.roda.core.events;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventsHandler implements EventsHandler {
  private static final long serialVersionUID = -1284727831525932207L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventsHandler.class);

  @Override
  public void handleUserCreated(ModelService model, User user, String password) {
    LOGGER.info("handleUserCreated '{}'", user);
  }

  @Override
  public void handleUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("handleUserUpdated '{}'", user);
    try {
      model.updateUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    }
  }

  @Override
  public void handleMyUserUpdated(ModelService model, User user, String password) {
    LOGGER.info("handleMyUserUpdated '{}'", user);
    try {
      model.updateMyUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    }
  }

  @Override
  public void handleUserDeleted(ModelService model, String userID) {
    LOGGER.info("handleUserDeleted '{}'", userID);
  }

}
