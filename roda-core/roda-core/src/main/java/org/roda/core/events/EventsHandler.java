package org.roda.core.events;

import java.io.Serializable;

import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public interface EventsHandler extends Serializable {
  public void handleUserCreated(ModelService model, User user, String password);

  public void handleUserUpdated(ModelService model, User user, String password);

  public void handleMyUserUpdated(ModelService model, User user, String password);

  public void handleUserDeleted(ModelService model, String userID);
}
