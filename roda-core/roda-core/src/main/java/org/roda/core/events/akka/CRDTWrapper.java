package org.roda.core.events.akka;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.cluster.ddata.AbstractReplicatedData;
import akka.cluster.ddata.ReplicatedDataSerialization;

public class CRDTWrapper extends AbstractReplicatedData<CRDTWrapper>
  implements IsRODAObject, ReplicatedDataSerialization {
  private static final long serialVersionUID = -9133998132086063749L;
  private static final Logger LOGGER = LoggerFactory.getLogger(CRDTWrapper.class);

  private IsRODAObject rodaObject;
  private boolean isUpdate;
  private String instanceId;
  private long timeinmillis;

  public CRDTWrapper(IsRODAObject rodaObject, boolean isUpdate, String instanceId, long timeinmillis) {
    this.rodaObject = rodaObject;
    this.isUpdate = isUpdate;
    this.instanceId = instanceId;
    this.setTimeinmillis(timeinmillis);
  }

  @Override
  public String getId() {
    return rodaObject.getId();
  }

  public IsRODAObject getRodaObject() {
    return rodaObject;
  }

  public boolean isUpdate() {
    return isUpdate;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public long getTimeinmillis() {
    return timeinmillis;
  }

  public void setTimeinmillis(long timeinmillis) {
    this.timeinmillis = timeinmillis;
  }

  @Override
  public CRDTWrapper mergeData(CRDTWrapper that) {
    if (rodaObject instanceof User) {
      User user = (User) rodaObject;
      User thatUser = (User) that.getRodaObject();
      LOGGER.info("Merging ({}; {}; {}) '{}' to ({}; {}; {}) '{}'", isUpdate, instanceId, timeinmillis,
        user.getId() + " " + user.getFullName(), that.isUpdate(), that.getInstanceId(), that.getTimeinmillis(),
        thatUser.getId() + " " + thatUser.getFullName());
    }
    return that;
  }

}
