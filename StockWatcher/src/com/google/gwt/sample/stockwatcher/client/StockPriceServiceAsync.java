package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StockPriceServiceAsync {
	void getStockPrices(String[] symbols, AsyncCallback<StockPrice[]> callback); 
}
