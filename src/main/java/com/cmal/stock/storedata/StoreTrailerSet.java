package com.cmal.stock.storedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmall.stock.bean.StoreTrailer;
import com.google.common.collect.Lists;
import com.kers.esmodel.BaseCommonConfig;
import com.kers.esmodel.UtilEs;
import com.kers.httpmodel.BaseConnClient;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

/**
 * 获取业绩预告数据
 * 
 * @author temp1
 *
 */
public class StoreTrailerSet {
	public static final String   P_TYPE_2017_12_31="2017-12-31";
	public static void main(String[] args) throws Exception {
		final JestClient jestClient = BaseCommonConfig.clientConfig();
		// List<StoreTrailer> list= Lists.newArrayList();
		for (int i = 0; i <= 26; i++) {
			// String content = StoreTrailerUrl(i);
			// System.out.println(content);|
			try {
				// list.addAll(getList(StoreTrailerUrl(i)));
				insBatchEs(getList(StoreTrailerUrl(i)), jestClient, "storetrailer");
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			// 保存es
		}
		Thread.sleep(10000);
		// System.out.println(list.size());
		// insBatchEs(list , jestClient , "storetrailer");

		// System.out.println(content);
	}

	public static List<StoreTrailer> getList(String data) {
		List<StoreTrailer> list = new ArrayList<StoreTrailer>();
		if (StringUtils.isNotBlank(data)) {
			JSONObject obj = (JSONObject) JSONObject.parse(data);
			JSONArray arr = obj.getJSONArray("data");
			for (int i = 0; i < arr.size(); i++) {
				Object o = arr.get(i);
				StoreTrailer tr = getDate(o.toString());
				list.add(tr);
			}
		}
		return list;
	}

	public static String StoreTrailerUrl(int index) throws ClientProtocolException, IOException {
		String url = "http://datainterface.eastmoney.com/EM_DataCenter/JS.aspx?type=SR&sty="
				+ "YJYG&fd=2017-12-31&st=4&sr=-1&p=" + index + "&ps=50&js={pages:(pc),data:[(x)]}&stat=0&rt=50342803";
		String content = BaseConnClient.baseGetReq(url);
		System.out.println(content);
		return content;
	}

	public static StoreTrailer getDate(String date) {
		StoreTrailer tr = new StoreTrailer();
		String[] datas = date.split(",");
		tr.setStockCode(datas[0]);
		tr.setStockName(datas[1]);
		tr.setPerChanges(datas[2]);
		tr.setRangeability(datas[3]);
		if (!StringUtils.isEmpty(tr.getRangeability())) {
			String[] s = tr.getRangeability().split("～");
			tr.setStartRangeability(Double.parseDouble(s[0].replace("%", "")));
			if (s.length > 1) {
				tr.setEndRangeability(Double.parseDouble(s[1].replace("%", "")));
			}
		}
		tr.setType(datas[4]);
		tr.setNetProfit(datas[5]);
		tr.setStartDate(datas[7]);
		tr.setEndDate(datas[8]);
		return tr;
	}

	public static void insBatchEs(List<StoreTrailer> list, JestClient jestClient, String indexIns) throws Exception {

		int i = 0;
		Bulk.Builder bulkBuilder = new Bulk.Builder();
		for (StoreTrailer bean : list) {
			i++;
			// System.out.println(bean.getUnionId());
			Index index = new Index.Builder(bean).index(indexIns).type("2017-12-31")
					.id(bean.getStockCode() + bean.getStartDate()).build();// type("walunifolia").build();
			bulkBuilder.addAction(index);
			if (i % 5000 == 0) {
				jestClient.execute(bulkBuilder.build());
				bulkBuilder = new Bulk.Builder();
			}
		}
		jestClient.execute(bulkBuilder.build());
		// jestClient.shutdownClient();
	}

	public static Map<String, StoreTrailer> getAllTrailerMap(String type) throws Exception {
		final JestClient jestClient = BaseCommonConfig.clientConfig();
		Map<String, StoreTrailer> map = new HashMap<String, StoreTrailer>();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		Search selResult = UtilEs.getSearch(searchSourceBuilder, "storetrailer", type, 0, 3800);
		JestResult results = jestClient.execute(selResult);
		List<StoreTrailer> lstBean = results.getSourceAsObjectList(StoreTrailer.class);
		for (StoreTrailer storeTrailer : lstBean) {
			map.put(storeTrailer.getStockCode(), storeTrailer);
		}
		return map;
	}
}
