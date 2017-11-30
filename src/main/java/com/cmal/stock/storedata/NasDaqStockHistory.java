package com.cmal.stock.storedata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.common.collect.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cmall.stock.bean.StockBaseInfo;
import com.kers.httpmodel.BaseConnClient;

public class NasDaqStockHistory {
	public  static Map<String, StockBaseInfo> stockHistory(String jsonContent, String stock)
			throws ClientProtocolException, IOException {
		// "6y|false|MU"
		stock = stock.toLowerCase();
		String url = "http://www.nasdaq.com/symbol/" + stock + "/historical";
		String scontent = BaseConnClient.RequestJsonPost(url, jsonContent);
		Document doc = Jsoup.parse(scontent);
		Element element = doc.getElementById("quotes_content_left_pnlAJAX");
		Elements trs = element.select("table").select("tr");
		Map<String, StockBaseInfo> mapSource = Maps.newConcurrentMap();
		for (int i = 2; i < trs.size(); i++) {
			Elements tds = trs.get(i).select("td");
			String rises = "0";
			if (i < trs.size() - 1) {
				double currdayClose = Double.valueOf(tds.get(4).text());
				double lastdayClose = Double.valueOf(trs.get((i + 1)).select("td").get(4).text());
				double rrs = ((currdayClose - lastdayClose) / currdayClose) * 100;

				BigDecimal b = new BigDecimal(rrs);
				rises = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()) ;//+ "%";
				// Date Open High Low Close / Last Volume
				String date = tds.get(0).text().replace("\"","").replace(",", "");
				String open = tds.get(1).text().replace("\"","").replace(",", "");
				String high = tds.get(2).text().replace("\"","").replace(",", "");
				String low = tds.get(3).text().replace("\"","").replace(",", "");
				String close = tds.get(4).text().replace("\"","").replace(",", "");
				String volume = tds.get(5).text().replace("\"","").replace(",", "");
				StockBaseInfo stockBaseInfo = new StockBaseInfo(date, open, high, low, close, volume, rises);
				mapSource.put(date, stockBaseInfo);

			}
		}
		return mapSource;

	}
	 public static void main(String[] args) throws ClientProtocolException, IOException {
		String stockCode="MOMO";
		System.out.println(stockHistory("6y|false|MOMO", "MOMO"));
	}
	
}
