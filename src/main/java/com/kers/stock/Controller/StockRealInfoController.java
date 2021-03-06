package com.kers.stock.Controller;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kers.stock.bean.StockBaseInfo;
import com.kers.stock.bean.StockRealBean;
import com.kers.stock.storedata.CommonBaseStockInfo;
import com.kers.stock.storedata.StoreRealSet;
import com.kers.stock.strage.SelGetStock;
import com.kers.stock.vo.StockBaseInfoVo;
import com.kers.stock.vo.StockBasePageInfo;

@RestController
public class StockRealInfoController extends BaseController{
	
    @RequestMapping("/real/getList")
    public Map<String,Object> getList(StockBasePageInfo page, String type) throws Exception {
    	BoolQueryBuilder query = QueryBuilders.boolQuery();
    	setQuery(query,page);
        return SelGetStock.getCommonLstResult(query,page,CommonBaseStockInfo.ES_INDEX_STOCKREALINFO,type);
    }
    
    @RequestMapping("/real/getClassNameList")
    public String[] getClassNameList() throws Exception {
    	StockRealBean info = new StockRealBean();
    	java.lang.reflect.Field[] fields=info.getClass().getDeclaredFields();  
        String[] fieldNames=new String[fields.length-1];  
	    for(int i=1;i<fields.length;i++){  
	        fieldNames[i-1]=fields[i].getName();  
	    }  
        return fieldNames;
    }
    
    @RequestMapping("/real/getRealByCode")
    public StockRealBean getRealByCode(String code) throws Exception {
        return StoreRealSet.getBeanByCode(code);
    }
}