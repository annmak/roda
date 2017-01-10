/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ShowPreservationEvent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowPreservationAgent extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      GWT.log(historyTokens.toString());
      if (historyTokens.size() == 1) {
        final String agentId = historyTokens.get(0);
        ShowPreservationAgent preservationAgents = new ShowPreservationAgent(agentId);
        callback.onSuccess(preservationAgents);
      } else if (historyTokens.size() == 2) {
        final String eventId = historyTokens.get(0);
        final String agentId = historyTokens.get(1);
        ShowPreservationAgent preservationAgents = new ShowPreservationAgent(eventId, agentId);
        callback.onSuccess(preservationAgents);
      } else {
        HistoryUtils.newHistory(PreservationAgents.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {PreservationAgents.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(PreservationAgents.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "agent";
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowPreservationAgent> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label agentId, agentName, agentType, agentVersion, agentNote, agentExtension;

  @UiField
  FlowPanel agentVersionPanel, agentNotePanel, agentExtensionPanel;

  @UiField
  Button backButton;

  private IndexedPreservationAgent agent = null;
  private String eventId = null;

  public ShowPreservationAgent(final String agentId) {
    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieve(IndexedPreservationAgent.class.getName(), agentId,
      new AsyncCallback<IndexedPreservationAgent>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationAgents.RESOLVER.getHistoryPath()));
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }
        }

        @Override
        public void onSuccess(IndexedPreservationAgent agent) {
          ShowPreservationAgent.this.agent = agent;
          viewAction();
        }
      });
  }

  public ShowPreservationAgent(final String eventId, final String agentId) {
    this(agentId);
    this.eventId = eventId;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void viewAction() {
    agentId.setText(agent.getId());
    agentName.setText(agent.getName());
    agentType.setText(agent.getType());

    agentVersionPanel.setVisible(StringUtils.isNotBlank(agent.getVersion()));
    agentVersion.setText(agent.getVersion());

    agentNotePanel.setVisible(StringUtils.isNotBlank(agent.getNote()));
    agentNote.setText(agent.getNote());

    agentExtensionPanel.setVisible(StringUtils.isNotBlank(agent.getExtension()));
    agentExtension.setText(agent.getExtension());
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    if (StringUtils.isNotBlank(eventId)) {
      HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, eventId);
    } else {
      HistoryUtils.newHistory(PreservationAgents.RESOLVER);
    }
  }
}