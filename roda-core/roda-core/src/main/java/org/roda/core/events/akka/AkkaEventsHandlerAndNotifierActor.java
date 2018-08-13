package org.roda.core.events.akka;

import static akka.cluster.ddata.Replicator.writeLocal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.akka.Messages.EventGroupCreated;
import org.roda.core.common.akka.Messages.EventGroupDeleted;
import org.roda.core.common.akka.Messages.EventGroupUpdated;
import org.roda.core.common.akka.Messages.EventUserCreated;
import org.roda.core.common.akka.Messages.EventUserDeleted;
import org.roda.core.common.akka.Messages.EventUserUpdated;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.events.EventsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.GSet;
import akka.cluster.ddata.GSetKey;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.Replicator.Changed;
import akka.cluster.ddata.Replicator.Subscribe;
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.UpdateFailure;
import akka.cluster.ddata.Replicator.UpdateSuccess;
import scala.Option;

public class AkkaEventsHandlerAndNotifierActor extends AbstractActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEventsHandlerAndNotifierActor.class);

  private final ActorRef replicator = DistributedData.get(context().system()).replicator();
  private final Cluster node = Cluster.get(context().system());

  private EventsHandler eventsHandler;
  private String instanceSenderId;

  private final Key<GSet<ObjectKey>> allObjectsKey = GSetKey.create("objects-keys");
  private Set<ObjectKey> keys = new HashSet<>();

  public AkkaEventsHandlerAndNotifierActor(EventsHandler eventsHandler) {
    this.eventsHandler = eventsHandler;
    this.instanceSenderId = self().toString();
  }

  @Override
  public void preStart() {
    Subscribe<GSet<ObjectKey>> subscribe = new Subscribe<>(allObjectsKey, getSelf());
    replicator.tell(subscribe, ActorRef.noSender());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(Changed.class, c -> handleChanged(c))
      .match(EventUserCreated.class, e -> handleUserCreated(e)).match(EventUserUpdated.class, e -> handleUserUpdated(e))
      .match(EventUserDeleted.class, e -> handleUserDeleted(e))
      .match(EventGroupCreated.class, e -> handleGroupCreated(e))
      .match(EventGroupUpdated.class, e -> handleGroupUpdated(e))
      .match(EventGroupDeleted.class, e -> handleGroupDeleted(e))
      .match(UpdateSuccess.class, e -> handleUpdateSuccess(e)).match(UpdateFailure.class, e -> handleUpdateFailure(e))
      .matchAny(msg -> {
        LOGGER.warn("Received unknown message '{}'", msg);
      }).build();
  }

  private void handleUpdateSuccess(UpdateSuccess e) {

  }

  private void handleUpdateFailure(UpdateFailure e) {

  }

  private void handleChanged(Changed<?> e) {
    if (e.key().equals(allObjectsKey)) {
      handleAllObjectsKey((Changed<GSet<ObjectKey>>) e);
    } else if (e.key() instanceof ObjectKey) {
      handleObjectChanged((Changed<LWWMap<String, Object>>) e);
    }
  }

  private void handleAllObjectsKey(Changed<GSet<ObjectKey>> e) {
    Set<ObjectKey> newKeys = e.dataValue().getElements();
    Set<ObjectKey> diff = new HashSet<>(newKeys);
    diff.removeAll(keys);
    diff.forEach(dKey -> {
      // subscribe to get notifications of when objects with this name are
      // added or removed
      replicator.tell(new Subscribe<>(dKey, self()), self());
    });
    keys = newKeys;
  }

  private void handleObjectChanged(Changed<LWWMap<String, Object>> e) {
    String objectId = e.key().id().replaceFirst("cache-", "");
    Option<Object> option = e.dataValue().get(objectId);
    if (option.isDefined()) {
      ObjectWrapper wrapper = (ObjectWrapper) option.get();
      if (!wrapper.getInstanceId().equals(instanceSenderId)) {
        if (objectId.startsWith("user")) {
          if (!wrapper.isUpdate) {
            // FIXME 20180814 hsilva: still missing passwords
            eventsHandler.handleUserCreated(RodaCoreFactory.getModelService(), (User) wrapper.getRodaObject(), null);
          } else {
            // FIXME 20180814 hsilva: still missing my user update & passwords
            eventsHandler.handleUserUpdated(RodaCoreFactory.getModelService(), (User) wrapper.getRodaObject(), null);
          }
        } else if (objectId.startsWith("group")) {
          if (!wrapper.isUpdate) {
            eventsHandler.handleGroupCreated(RodaCoreFactory.getModelService(), (Group) wrapper.getRodaObject());
          } else {
            eventsHandler.handleGroupUpdated(RodaCoreFactory.getModelService(), (Group) wrapper.getRodaObject());
          }
        }
      }
    } else {
      // this is a deletion
      if (objectId.startsWith("user")) {
        eventsHandler.handleUserDeleted(RodaCoreFactory.getModelService(), objectId.replaceFirst("user-", ""));
      } else if (objectId.startsWith("group")) {
        eventsHandler.handleGroupDeleted(RodaCoreFactory.getModelService(), objectId.replaceFirst("group-", ""));
      }
    }
  }

  private void handleUserCreated(EventUserCreated e) {
    String key = "user-" + e.getUser().getId();
    putObjectInCache(key, new ObjectWrapper(e.getUser(), false, instanceSenderId));
  }

  private void handleUserUpdated(EventUserUpdated e) {
    String key = "user-" + e.getUser().getId();
    putObjectInCache(key, new ObjectWrapper(e.getUser(), true, instanceSenderId));
  }

  private void handleUserDeleted(EventUserDeleted e) {
    String key = "user-" + e.getId();
    evictObjectFromCache(key);
  }

  private void handleGroupCreated(EventGroupCreated e) {
    String key = "group-" + e.getGroup().getId();
    putObjectInCache(key, new ObjectWrapper(e.getGroup(), false, instanceSenderId));
  }

  private void handleGroupUpdated(EventGroupUpdated e) {
    String key = "group-" + e.getGroup().getId();
    putObjectInCache(key, new ObjectWrapper(e.getGroup(), true, instanceSenderId));
  }

  private void handleGroupDeleted(EventGroupDeleted e) {
    String key = "group-" + e.getId();
    evictObjectFromCache(key);
  }

  private void putObjectInCache(String key, Object value) {
    ObjectKey objectKey = dataKey(key);
    if (!keys.contains(objectKey)) {
      Update<GSet<ObjectKey>> update1 = new Update<>(allObjectsKey, GSet.create(), writeLocal(),
        curr -> curr.add(objectKey));
      replicator.tell(update1, self());
    }

    Optional<Object> ctx = Optional.of(getSender());
    Update<LWWMap<String, Object>> update = new Update<LWWMap<String, Object>>(dataKey(key), LWWMap.create(),
      writeLocal(), ctx, curr -> curr.put(node, key, value));
    replicator.tell(update, self());
  }

  private void evictObjectFromCache(String key) {
    ObjectKey objectKey = dataKey(key);
    if (!keys.contains(objectKey)) {
      Update<GSet<ObjectKey>> update1 = new Update<>(allObjectsKey, GSet.create(), writeLocal(),
        curr -> curr.add(objectKey));
      replicator.tell(update1, self());
    }

    Update<LWWMap<String, Object>> update = new Update<>(objectKey, LWWMap.create(), writeLocal(),
      curr -> curr.remove(node, key));
    replicator.tell(update, self());
  }

  private ObjectKey dataKey(String entryKey) {
    return new ObjectKey("cache-" + entryKey);
  }

  public static class ObjectWrapper implements IsRODAObject {
    private static final long serialVersionUID = 2512655095108429642L;

    private IsRODAObject rodaObject;
    private boolean isUpdate;
    private String instanceId;

    public ObjectWrapper(IsRODAObject rodaObject, boolean isUpdate, String instanceId) {
      this.rodaObject = rodaObject;
      this.isUpdate = isUpdate;
      this.instanceId = instanceId;
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

  }

  public static class ObjectKey extends Key<LWWMap<String, Object>> {
    private static final long serialVersionUID = 1L;

    public ObjectKey(String eventKey) {
      super(eventKey);
    }
  }

}