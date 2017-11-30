package com.cmal.stock.storedata;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.cmall.stock.bean.CnInvRoot;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.kers.httpmodel.BaseConnClient;

public class StockNasdaqStockReport {

	public String getLinkUrl(String stock){
		String url="https://cn.investing.com/search/service/search";
		System.out.println(BaseConnClient.RequestMulJsonPost(url, "search_text="+stock+"&term="+stock+"&country_id=0&tab_id=Stocks"));
		CnInvRoot  cnvroot=  new Gson().fromJson(BaseConnClient.RequestMulJsonPost(url, "search_text="+stock+"&term="+stock+"&country_id=0&tab_id=Stocks"),CnInvRoot.class);
        if(!cnvroot.getAll().isEmpty()) {       
		 // System.out.println(cnvroot.getAll().get(0).getLink());
          return cnvroot.getAll().get(0).getLink();
        }
          return null;
	}
	
	@Test
	public void getMorehistory()throws Exception{
		//https://cn.investing.com/equities/
		System.out.println(getLinkUrl("momo"));
		 System.out.println(BaseConnClient.baseGetReq(""));
		
//		 final String pairID = d.getElementsByAttributeValueContaining("class", "earnCalCompany").get(0).attr("_r_pid");
		 
//		 List<NameValuePair> parms   =  Lists.newArrayList();
//		 parms.add(new BasicNameValuePair("pairID","15358" ));
//		 parms.add(new BasicNameValuePair("last_timestamp", "2016-07-26"));
//		 
//		String content = BaseConnClient.basePostReq("https://cn.investing.com/equities/morehistory", parms);
//          System.out.println(content);
	}
}
