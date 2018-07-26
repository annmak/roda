package org.roda.core.events;

import java.io.Serializable;

import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public interface EventsNotifier extends Serializable {
  public void notifyUserCreated(ModelService model, User user, String password);

  public void notifyUserUpdated(ModelService model, User user, String password);

  public void notifyMyUserUpdated(ModelService model, User user, String password);

  public void notifyUserDeleted(ModelService model, String userID);
}
