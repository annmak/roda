/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class IncrementalList extends Composite implements HasValueChangeHandlers<List<String>> {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalList> {
  }

  @UiField
  FlowPanel textBoxPanel;

  @UiField
  Button addDynamicButton;

  private ArrayList<RemovableTextBox> textBoxes;

  public IncrementalList() {
    this(false, null);
  }

  public IncrementalList(boolean vertical) {
    this(vertical, null);
  }

  public IncrementalList(boolean vertical, List<String> initialValues) {
    initWidget(uiBinder.createAndBindUi(this));
    textBoxes = new ArrayList<>();
    if (vertical) {
      addStyleDependentName("vertical");
    }
    if (initialValues == null || initialValues.isEmpty()) {
      // make sure there is one text box
      addTextBox(null);
    } else {
      setTextBoxList(initialValues);
    }
  }

  public List<String> getTextBoxesValue() {
    ArrayList<String> listValues = new ArrayList<>();
    for (RemovableTextBox textBox : textBoxes) {
      if (StringUtils.isNotBlank(textBox.getTextBoxValue())) {
        listValues.add(textBox.getTextBoxValue());
      }
    }
    return listValues;
  }

  public void setTextBoxList(List<String> list) {
    clearTextBoxes();
    for (int i = 0; i < list.size(); i++) {
      addTextBox(null);
    }
    for (int i = 0; i < list.size(); i++) {
      textBoxes.get(i).item.setText(list.get(i));
    }
  }

  public void clearTextBoxes() {
    textBoxPanel.clear();
    textBoxes = new ArrayList<>();
    addDynamicButton.setVisible(true);
  }

  @UiHandler("addDynamicButton")
  void addMore(ClickEvent event) {
    addTextBox(null);
  }

  private void addTextBox(String element) {
    final RemovableTextBox box = new RemovableTextBox(textBoxes.isEmpty(), element);
    textBoxPanel.insert(box, 0);
    textBoxes.add(0, box);
    addDynamicButton.setVisible(false);

    // on clicking the button, either add or remove the text box
    box.addRemoveClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        addOrRemoveTextBox(box);
      }
    });

    // pressing ENTER goes to next text box, or adds a new one if we are on the last
    box.item.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent keyPressEvent) {
        if (keyPressEvent.getUnicodeCharCode() == KeyCodes.KEY_ENTER && !keyPressEvent.isAnyModifierKeyDown()) {
          if (box.isAddTextBox()) {
            addOrRemoveTextBox(box);
          } else {
            TextBox nextItem = textBoxes.get(textBoxes.indexOf(box) + 1).item;
            nextItem.setFocus(true);
            nextItem.selectAll();
          }
        }
      }
    });

    box.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        ValueChangeEvent.fire(IncrementalList.this, getTextBoxesValue());
      }
    });

    shiftValuesDown();
  }

  private void addOrRemoveTextBox(RemovableTextBox box) {
    if (box.isAddTextBox()) {
      addTextBox(null);
    } else {
      textBoxPanel.remove(box);
      textBoxes.remove(box);
      if (textBoxes.isEmpty()) {
        addDynamicButton.setVisible(true);
      }
    }

    ValueChangeEvent.fire(IncrementalList.this, getTextBoxesValue());
    textBoxes.get(textBoxes.size() - 1).item.setFocus(true);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  private void shiftValuesDown() {
    for (int i = 0; i < textBoxes.size() - 1; i++) {
      textBoxes.get(i).item.setText(textBoxes.get(i + 1).getTextBoxValue());
    }
    textBoxes.get(textBoxes.size() - 1).item.setText(null);
  }
}
