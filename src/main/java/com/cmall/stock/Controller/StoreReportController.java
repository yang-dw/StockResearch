package com.cmall.stock.Controller;

import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cmal.stock.strage.SelGetStock;
import com.cmall.stock.bean.EastReportBean;
import com.cmall.stock.vo.StockBasePageInfo;

@RestController
public class StoreReportController  extends BaseController{
	
    @RequestMapping("/report/getList")
    public Map<String,Object> getList(StockBasePageInfo page , String type) throws Exception {
    	BoolQueryBuilder query = QueryBuilders.boolQuery();
    	setQuery(query,page);
        return SelGetStock.getReportLstResult(query,page,type);
    }
    
    @RequestMapping("/report/getClassNameList")
    public String[] getClassNameList() throws Exception {
    	EastReportBean info = new EastReportBean();
    	java.lang.reflect.Field[] fields=info.getClass().getDeclaredFields();
        String[] fieldNames=new String[fields.length-1+2];  
        fieldNames[0]="stockCode";
        fieldNames[1]="stockName";
	    for(int i=1;i<fields.length;i++){  
	        fieldNames[(i-1)+2]=fields[i].getName();  
	    }  
        return fieldNames;
    }
    
   
}