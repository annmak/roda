/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

/**
 * Thrown to indicate that an ingest error occurred.
 * 
 * @author Rui Castro
 */
public class IngestException extends EditorException {
  private static final long serialVersionUID = -3123133514163489091L;

  /**
   * Constructs a new {@link IngestException}.
   */
  public IngestException() {
  }

  /**
   * Constructs a new {@link IngestException} with the given error message.
   * 
   * @param message
   *          the error message
   */
  public IngestException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link IngestException} with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public IngestException(String message, Throwable cause) {
    super(message, cause);
  }

}
