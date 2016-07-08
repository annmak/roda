/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class AkkaJobStateInfoActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobStateInfoActor.class);

  private JobInfo jobInfo;
  private Plugin<?> plugin;
  private ActorRef jobCreator;

  public AkkaJobStateInfoActor(Plugin<?> plugin, ActorRef jobCreator) {
    jobInfo = new JobInfo();
    this.plugin = plugin;
    this.jobCreator = jobCreator;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Messages.JobStateUpdated) {
      handleJobStateUpdated(msg);
    } else if (msg instanceof Messages.JobInfoUpdated) {
      handleJobInfoUpdated(msg);
    } else if (msg instanceof Messages.PluginInitEnded) {
      handlePluginInitEnded(msg);
    } else if (msg instanceof Messages.JobInitEnded) {
      handleJobInitEnded(msg);
    } else if (msg instanceof Messages.PluginExecuteIsDone) {
      handleExecuteIsDone(msg);
    } else if (msg instanceof Messages.PluginAfterAllExecuteIsDone) {
      handleAfterAllExecuteIsDone(msg);
    } else {
      LOGGER.error("Received a message that it doesn't know how to process (" + msg.getClass().getName() + ")...");
      unhandled(msg);
    }
  }

  private void handleJobStateUpdated(Object msg) {
    Messages.JobStateUpdated message = (Messages.JobStateUpdated) msg;
    Plugin<?> p = message.getPlugin() == null ? plugin : message.getPlugin();
    try {
      Job job = PluginHelper.getJobFromIndex(message.getPlugin(), RodaCoreFactory.getIndexService());
      LOGGER.info("Setting job '{}' ({}) state to {}", job.getName(), job.getId(), message.getState());
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Unable to get Job from index to log its state change", e);
    }
    PluginHelper.updateJobState(p, RodaCoreFactory.getModelService(), message.getState(), message.getStateDatails());
    if (JOB_STATE.FAILED_TO_COMPLETE == message.getState() || JOB_STATE.COMPLETED == message.getState()) {
      jobCreator.tell("Done", getSelf());
      getContext().stop(getSelf());
    }
  }

  private void handleJobInfoUpdated(Object msg) {
    Messages.JobInfoUpdated message = (Messages.JobInfoUpdated) msg;
    jobInfo.put(message.plugin, message.jobPluginInfo);
    JobPluginInfo infoUpdated = message.jobPluginInfo.processJobPluginInformation(message.plugin, jobInfo);
    jobInfo.setObjectsCount(infoUpdated.getSourceObjectsCount());
    PluginHelper.updateJobInformation(message.plugin, RodaCoreFactory.getModelService(), infoUpdated);
  }

  private void handlePluginInitEnded(Object msg) {
    Messages.PluginInitEnded message = (Messages.PluginInitEnded) msg;
    jobInfo.setStarted(message.getPlugin());
  }

  private void handleJobInitEnded(Object msg) {
    jobInfo.setInitEnded(true);
    // INFO 20160630 hsilva: the following test is needed because messages can
    // be out of order and a plugin might already arrived to the end
    if (jobInfo.isDone()) {
      getSender().tell(new Messages.PluginAfterAllExecuteIsReady(plugin), getSelf());
    }
  }

  private void handleExecuteIsDone(Object msg) {
    Messages.PluginExecuteIsDone message = (Messages.PluginExecuteIsDone) msg;

    jobInfo.setDone(message.getPlugin());
    if (jobInfo.isDone() && jobInfo.isInitEnded()) {
      getSender().tell(new Messages.PluginAfterAllExecuteIsReady(plugin), getSelf());
    }
  }

  private void handleAfterAllExecuteIsDone(Object msg) {
    getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.COMPLETED), getSelf());
  }

}