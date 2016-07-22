/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.IncrementalAssociativeList;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class PluginParameterPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final PluginParameter parameter;
  private final FlowPanel layout;

  private String value;

  public PluginParameterPanel(PluginParameter parameter) {
    super();
    this.parameter = parameter;

    layout = new FlowPanel();
    initWidget(layout);

    updateLayout();

    layout.addStyleName("plugin-options-parameter");
  }

  private void updateLayout() {
    if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
      createBooleanLayout();
    } else if (PluginParameterType.STRING.equals(parameter.getType())) {
      createStringLayout();
    } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
      createPluginSipToAipLayout();
    } else if (PluginParameterType.AIP_ID.equals(parameter.getType())) {
      createSelectAipLayout();
    } else if (PluginParameterType.RISK_ID.equals(parameter.getType())) {
      createSelectRiskLayout();
    } else if (PluginParameterType.SEVERITY.equals(parameter.getType())) {
      createSelectSeverityLayout();
    } else {
      logger
        .warn("Unsupported plugin parameter type: " + parameter.getType() + ". Reverting to default parameter editor.");
      createStringLayout();
    }
  }

  private void createSelectSeverityLayout() {
    Label parameterName = new Label(parameter.getName());
    final ListBox severityBox = new ListBox();
    severityBox.addStyleName("form-selectbox");
    severityBox.addStyleName("form-textbox-small");

    for (SEVERITY_LEVEL severity : SEVERITY_LEVEL.values()) {
      severityBox.addItem(messages.severityLevel(severity), severity.toString());
    }

    value = severityBox.getSelectedValue();

    severityBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        value = severityBox.getSelectedValue();
      }
    });

    layout.add(parameterName);
    layout.add(severityBox);
    addHelp();
  }

  private void createSelectRiskLayout() {
    Label parameterName = new Label(parameter.getName());
    IncrementalAssociativeList list = new IncrementalAssociativeList(IndexedRisk.class, RodaConstants.RISK_ID,
      RodaConstants.RISK_SEARCH, messages.getRisksDialogName());

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        IncrementalAssociativeList sourceList = (IncrementalAssociativeList) event.getSource();
        List<String> values = sourceList.getTextBoxesValue();
        if (!values.isEmpty()) {
          value = getValuesString(values);
        }
      }

      private String getValuesString(List<String> values) {
        String builder = "";

        for (String value : values) {
          builder += value + ",";
        }

        return builder.substring(0, builder.length() - 1);
      }

    });

    layout.add(parameterName);
    layout.add(list);
    addHelp();
  }

  private void createSelectAipLayout() {
    Label parameterName = new Label(parameter.getName());
    final HorizontalPanel editPanel = new HorizontalPanel();
    final FlowPanel aipPanel = new FlowPanel();
    final Button button = new Button(messages.pluginAipIdButton());
    final FlowPanel buttonsPanel = new FlowPanel();
    final Anchor editButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-edit\"></i>"));
    final Anchor removeButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));

    buttonsPanel.add(editButton);
    buttonsPanel.add(removeButton);

    ClickHandler editClickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        SelectAipDialog selectAipDialog = new SelectAipDialog(parameter.getName());
        selectAipDialog.showAndCenter();
        selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

          @Override
          public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
            IndexedAIP aip = event.getValue();

            Label itemTitle = new Label();
            HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
            itemIconHtmlPanel.addStyleName("itemIcon");
            itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
            itemTitle.addStyleName("itemText");

            aipPanel.clear();
            aipPanel.add(itemIconHtmlPanel);
            aipPanel.add(itemTitle);

            editPanel.add(aipPanel);
            editPanel.add(buttonsPanel);

            editPanel.setCellWidth(aipPanel, "100%");

            editPanel.setVisible(true);
            button.setVisible(false);

            value = aip.getId();
          }
        });
      }
    };

    ClickHandler removeClickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        editPanel.setVisible(false);
        button.setVisible(true);

        value = null;
      }
    };

    button.addClickHandler(editClickHandler);
    editButton.addClickHandler(editClickHandler);
    removeButton.addClickHandler(removeClickHandler);

    layout.add(parameterName);
    layout.add(button);
    layout.add(editPanel);

    parameterName.addStyleName("form-label");
    aipPanel.addStyleName("itemPanel");
    button.addStyleName("form-button btn btn-play");
    buttonsPanel.addStyleName("itemButtonsPanel");
    editButton.addStyleName("toolbarLink toolbarLinkSmall");
    removeButton.addStyleName("toolbarLink toolbarLinkSmall");
  }

  private void createPluginSipToAipLayout() {

    List<PluginType> plugins = Arrays.asList(PluginType.SIP_TO_AIP);
    BrowserService.Util.getInstance().getPluginsInfo(plugins, new AsyncCallback<List<PluginInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<PluginInfo> pluginsInfo) {
        Label parameterName = new Label(parameter.getName());

        layout.add(parameterName);
        addHelp();

        FlowPanel radioGroup = new FlowPanel();

        PluginUtils.sortByName(pluginsInfo);

        for (final PluginInfo pluginInfo : pluginsInfo) {
          if (pluginInfo != null) {
            RadioButton pRadio = new RadioButton(parameter.getName(),
              messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()));

            if (pluginInfo.getId().equals(parameter.getDefaultValue())) {
              pRadio.setValue(true);
              value = pluginInfo.getId();
            }

            Label pHelp = new Label(pluginInfo.getDescription());

            radioGroup.add(pRadio);
            radioGroup.add(pHelp);

            pRadio.addStyleName("form-radiobutton");
            pHelp.addStyleName("form-help");

            pRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

              @Override
              public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                  value = pluginInfo.getId();
                }
              }
            });
          } else {
            GWT.log("Got a null plugin");
          }
        }

        layout.add(radioGroup);

        radioGroup.addStyleName("form-radiogroup");
        parameterName.addStyleName("form-label");
      }
    });
  }

  private void createStringLayout() {
    Label parameterName = new Label(parameter.getName());
    TextBox parameterBox = new TextBox();
    if (parameter.getDefaultValue() != null) {
      parameterBox.setText(parameter.getDefaultValue());
      value = parameter.getDefaultValue();
    }

    layout.add(parameterName);
    layout.add(parameterBox);
    addHelp();

    parameterName.addStyleName("form-label");
    parameterBox.addStyleName("form-textbox");

    // binding change
    parameterBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        value = event.getValue();
      }
    });
  }

  private void createBooleanLayout() {
    CheckBox checkBox = new CheckBox(parameter.getName());
    checkBox.setValue("true".equals(parameter.getDefaultValue()));
    value = "true".equals(parameter.getDefaultValue()) ? "true" : "false";
    checkBox.setEnabled(!parameter.isReadonly());

    layout.add(checkBox);
    addHelp();

    checkBox.addStyleName("form-checkbox");

    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        value = event.getValue() ? "true" : "false";
      }
    });
  }

  private void addHelp() {
    String pDescription = parameter.getDescription();
    if (pDescription != null && pDescription.length() > 0) {
      Label pHelp = new Label(pDescription);

      layout.add(pHelp);

      pHelp.addStyleName("form-help");
    }
  }

  public String getValue() {
    return value;
  }

  public PluginParameter getParameter() {
    return parameter;
  }

}
