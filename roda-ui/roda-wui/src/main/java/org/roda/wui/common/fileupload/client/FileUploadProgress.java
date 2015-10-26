/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.fileupload.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("fileuploadprogress")
public interface FileUploadProgress extends RemoteService {

  public static class Util {

    public static FileUploadProgressAsync getInstance() {

      return GWT.create(FileUploadProgress.class);
    }
  }

  /**
   * Get the upload progress
   * 
   * @return The progress percentage, from 0 to 1, or -1 if unknown or not
   *         applicable
   */
  public double getProgress();

}
