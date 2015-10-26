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
package org.roda.wui.management.editor.client;

import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Text;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class CountryEditor implements MetadataElementEditor {

  private final ControlledVocabularyEditor editor;

  public CountryEditor() {
    // FIXME
    // editor = new ControlledVocabularyEditor(DescriptionObject.COUNTRYCODE);
    editor = new ControlledVocabularyEditor("");
    editor.getWidget().addStyleName("wui-editor-country");
  }

  public EadCValue getValue() {
    return new Text(editor.getSelected());
  }

  public Widget getWidget() {
    return editor.getWidget();
  }

  public boolean isEmpty() {
    return false;
  }

  public void setValue(EadCValue value) {
    if (value instanceof Text) {
      Text text = (Text) value;
      editor.setSelected(text.getText());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    editor.addChangeListener(listener);

  }

  public void removeChangeListener(ChangeListener listener) {
    editor.removeChangeListener(listener);
  }

  public boolean isValid() {
    return true;
  }
}
