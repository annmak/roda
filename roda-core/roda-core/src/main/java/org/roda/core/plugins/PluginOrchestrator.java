/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.akka.Messages.JobPartialUpdate;

public interface PluginOrchestrator {

  public void setup();

  public void shutdown();

  public <T extends IsIndexed> void runPluginFromIndex(Object context, Class<T> classToActOn, Filter filter,
    Plugin<T> plugin);

  public void runPluginOnAIPs(Object context, Plugin<AIP> plugin, List<String> uuids, boolean retrieveFromModel);

  public void runPluginOnRepresentations(Object context, Plugin<Representation> plugin, List<String> uuids);

  public void runPluginOnFiles(Object context, Plugin<File> plugin, List<String> uuids);

  public void runPluginOnAllAIPs(Object context, Plugin<AIP> plugin);

  public void runPluginOnAllRepresentations(Object context, Plugin<Representation> plugin);

  public void runPluginOnAllFiles(Object context, Plugin<File> plugin);

  public void runPluginOnTransferredResources(Object context, Plugin<TransferredResource> plugin, List<String> uuids);

  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin);

  /*
   * Job related methods
   * _________________________________________________________________________________________________________________
   */
  /** 201603 hsilva: only tests should invoke this method synchronously */
  public void executeJob(Job job, boolean async) throws JobAlreadyStartedException;

  /** 201608 hsilva: this method is sync */
  public void stopJob(Job job);

  /** 201607 hsilva: this method is sync */
  public void cleanUnfinishedJobs();

  /** 201607 hsilva: this method is sync */
  public void setJobContextInformation(String jobId, Object object);

  /** 201607 hsilva: this method is async */
  public <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException;

  /** 201608 hsilva: this method is async */
  public <T extends IsRODAObject> void updateJob(Plugin<T> plugin, JobPartialUpdate partialUpdate);

}
