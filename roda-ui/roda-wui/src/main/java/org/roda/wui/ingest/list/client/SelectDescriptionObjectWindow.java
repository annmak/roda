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
package org.roda.wui.ingest.list.client;

import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;

/**
 * @author Luis Faria
 * 
 */
public class SelectDescriptionObjectWindow extends WUIWindow {

  public interface SelectDescriptionObjectListener {
    public void onSelect(SimpleDescriptionObject sdo);
  }

  private static IngestListConstants constants = (IngestListConstants) GWT.create(IngestListConstants.class);
  // private ClientLogger logger = new ClientLogger(getClass().getName());

  // private final CollectionsTree tree;
  private final WUIButton close;
  private final Set<SelectDescriptionObjectListener> listeners;

  public SelectDescriptionObjectWindow(SimpleDescriptionObject sdo) {
    super(constants.selectDescriptionObjectWindowTitle(), 850, 400);

    // this.tree = new CollectionsTree(sdo, null, null, 10, true);
    this.close = new WUIButton(constants.selectDescriptionObjectWindowClose(), WUIButton.Left.ROUND,
      WUIButton.Right.CROSS);
    this.listeners = new HashSet<SelectDescriptionObjectListener>();

    // this.setWidget(tree);
    this.addToBottom(close);

    // tree.addTreeListener(new TreeListener() {
    //
    // public void onTreeItemSelected(TreeItem item) {
    // if (item instanceof CollectionsTreeItem) {
    // onSelect(((CollectionsTreeItem) item).getSDO());
    // }
    //
    // }
    //
    // public void onTreeItemStateChanged(TreeItem item) {
    // // nothing to do
    //
    // }
    //
    // });

    close.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        onCancel();
        hide();
      }

    });

    // tree.getRootItem().setState(true);
    //
    // tree.addStyleName("select-do-window-tree");
  }

  public void addSelectDescriptionObjectListener(SelectDescriptionObjectListener listener) {
    listeners.add(listener);
  }

  public void removeSelectDescriptionObjectListener(SelectDescriptionObjectListener listener) {
    listeners.remove(listener);
  }

  private void onSelect(SimpleDescriptionObject sdo) {
    for (SelectDescriptionObjectListener listener : listeners) {
      listener.onSelect(sdo);
    }
    onSuccess();
  }
}
