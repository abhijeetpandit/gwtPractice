package com.google.gwt.sample.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {
	private static Logger LOGGER = Logger.getLogger(StockWatcher.class.getName());
	
	
	private static final int REFRESH_INTERVAL = 5000;
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add stock"); 
	private Label lastUpdatedLabel = new Label();
	private List<String> stocks = new ArrayList<>();
	private StockPriceServiceAsync stockService = GWT.create(StockPriceService.class);
	@Override
	public void onModuleLoad() {
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");
	
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);
		
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);
		
		RootPanel.get("stockList").add(mainPanel);
		
		newSymbolTextBox.setFocus(true);
		
		addStockButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addStock();
			}
		});
		newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}
			}
		});
		
		Timer refreshTimer = new Timer() {
			
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		
	}

	private void addStock() {
		final String symbol = newSymbolTextBox.getText().trim();
		
		if (!symbol.matches("^[0-9A-Za-z\\.]{1,10}$")) {
	        Window.alert("'" + symbol + "' is not a valid symbol.");
	        newSymbolTextBox.selectAll();
	        return;
	    }
		
		if(stocks.contains(symbol)) {
			Window.alert("'" + symbol + "' is already present.");
			newSymbolTextBox.selectAll();
			return;
		} 
		
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		newSymbolTextBox.setText("");
		newSymbolTextBox.setFocus(true);
		
		Button removeStockButton = new Button("X (Remove)");
		removeStockButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int removeIndex = stocks.indexOf(symbol);
				stocks.remove(removeIndex);
				stocksFlexTable.removeRow(removeIndex + 1);
			}
		});
		stocksFlexTable.setWidget(row, 4, removeStockButton);
		
		refreshWatchList();
	}
	
	private void refreshWatchList() {
		LOGGER.log(Level.SEVERE, "REfreshing");		
		
		if (stockService == null) {
			stockService = GWT.create(StockPriceService.class);
		}
		AsyncCallback<StockPrice[]> callback = new AsyncCallback<StockPrice[]>() {

			@Override
			public void onFailure(Throwable caught) {
				LOGGER.log(Level.SEVERE, caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(StockPrice[] result) {
				updateTable(result);
			}
		};
		stockService.getStockPrices(stocks.toArray(new String[stocks.size()]), callback);
	}

	private void updateTable(StockPrice[] stockPrices) {
		for (int i = 0; i < stockPrices.length; i++) {
	        updateTable(stockPrices[i]);
	     }
	}

	private void updateTable(StockPrice price) {
		 if (!stocks.contains(price.getSymbol())) {
		   return;
		 }
		
		 int row = stocks.indexOf(price.getSymbol()) + 1;
		
		 // Format the data in the Price and Change fields.
		 String priceText = NumberFormat.getFormat("#,##0.00").format(
		     price.getPrice());
		 NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		 String changeText = changeFormat.format(price.getChange());
		 String changePercentText = changeFormat.format(price.getChangePercent());
		 stocksFlexTable.setText(row, 1, priceText);
	     stocksFlexTable.setText(row, 2, changeText + " (" + changePercentText
	         + "%)");
	     
	     DateTimeFormat dateFormat = DateTimeFormat.getFormat(
	        DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
	     lastUpdatedLabel.setText("Last update : " 
	        + dateFormat.format(new Date()));
	}
}
