package com.kers.stock.Controller;

import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kers.gov.GovBankOMOBean;
import com.kers.stock.bean.StockZjlx;
import com.kers.stock.storedata.CommonBaseStockInfo;
import com.kers.stock.strage.SelGetStock;
import com.kers.stock.vo.StockBasePageInfo;

@RestController
public class GobBankOMOController  extends BaseController<GovBankOMOBean>{
	
	@RequestMapping("/govomo/getList")
    public Map<String,Object> getList(StockBasePageInfo page, String type) throws Exception {
    	BoolQueryBuilder query = QueryBuilders.boolQuery();
    	setQuery(query,page);
        return SelGetStock.getCommonLstResult(query,page,CommonBaseStockInfo.ES_INDEX_GOV_OMO,type);
    }
    
    @RequestMapping("/govomo/getClassNameList")
    public String[] getClassNameList() throws Exception {
    	StockZjlx info = new StockZjlx();
    	java.lang.reflect.Field[] fields=info.getClass().getDeclaredFields();  
        String[] fieldNames=new String[fields.length-1];  
	    for(int i=1;i<fields.length;i++){  
	        fieldNames[i-1]=fields[i].getName();  
	    }  
        return fieldNames;
    }
    
    
   
}