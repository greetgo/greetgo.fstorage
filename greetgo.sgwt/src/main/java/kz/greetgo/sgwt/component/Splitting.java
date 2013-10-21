package kz.greetgo.sgwt.component;

import kz.greetgo.sgwt.base.Snippet;

import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;

public final class Splitting<T> extends Snippet {
  private final IButton rightToLeft;
  private final IButton leftToRight;
  public final Listing<T> left;
  public final Listing<T> right;
  public HLayout root;
  
  private final <T> ClickHandler clickHandler(final Listing<T> from, final Listing<T> to) {
    return new ClickHandler() {
      public void onClick(ClickEvent event) {
        for (ListGridRecord record : from.grid.getSelectedRecords()) {
          to.add(from.associated(record));
          from.grid.removeData(record);
          rightToLeft.disable();
          leftToRight.disable();
        }
      }
    };
  }
  
  private static final <T> SelectionChangedHandler selectionHandler(final Listing<T> listing,
      final IButton button) {
    return new SelectionChangedHandler() {
      public void onSelectionChanged(SelectionEvent event) {
        button.setDisabled(!listing.grid.anySelected());
      }
    };
  }
  
  public Splitting(Listing<T> leftListing, Listing<T> rightListing) {
    this.left = leftListing;
    this.right = rightListing;
    
    rightToLeft = new IButton(LEFT, clickHandler(right, left));
    leftToRight = new IButton(RIGHT, clickHandler(left, right));
    rightToLeft.setWidth(TOOL);
    leftToRight.setWidth(TOOL);
    rightToLeft.disable();
    leftToRight.disable();
    
    left.grid.addSelectionChangedHandler(selectionHandler(left, leftToRight));
    right.grid.addSelectionChangedHandler(selectionHandler(right, rightToLeft));
    
    root = hl(left.grid, toolbar(vl(rightToLeft, leftToRight)), right.grid);
  }
}