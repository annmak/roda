/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Risks extends RodaCoreService {

  private static final String RISKS_COMPONENT = "Risks";
  private static final String INGEST_SUBMIT_ROLE = "ingest.submit";

  private Risks() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Risk createRisk(RodaUser user, Risk risk) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, INGEST_SUBMIT_ROLE);

    RodaCoreFactory.getModelService().createRisk(risk);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, RISKS_COMPONENT, "createRisk", null, duration, "risk", risk);

    return risk;
  }

  public static void deleteRisk(RodaUser user, String riskId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check user permissions
    // FIXME

    // delegate
    RodaCoreFactory.getModelService().deleteRisk(riskId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, RISKS_COMPONENT, "deleteRisk", null, duration, "riskId", riskId);
  }

  public static List<Risk> retrieveRisks(IndexResult<Risk> listRisksIndexResult) {
    List<Risk> risks = new ArrayList<Risk>();
    for (Risk risk : listRisksIndexResult.getResults()) {
      risks.add(risk);
    }
    return risks;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}