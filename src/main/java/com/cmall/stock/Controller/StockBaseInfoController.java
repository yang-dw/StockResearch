package com.cmall.stock.Controller;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmal.stock.strage.SelGetStock;
import com.cmall.stock.bean.StockBaseInfo;
import com.cmall.stock.vo.StockBaseInfoVo;
import com.cmall.stock.vo.StockBasePageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
public class StockBaseInfoController {
	
    @RequestMapping("/getList")
    public Map<String,Object> getList(StockBasePageInfo page) throws Exception {
    	BoolQueryBuilder query = QueryBuilders.boolQuery();
    	setQuery(query,page);
        return SelGetStock.getLstResult(query,page);
    }
    
    @RequestMapping("/getClassNameList")
    public String[] getClassNameList() throws Exception {
    	StockBaseInfo info = new StockBaseInfo();
    	java.lang.reflect.Field[] fields=info.getClass().getDeclaredFields();  
        String[] fieldNames=new String[fields.length-1];  
	    for(int i=1;i<fields.length;i++){  
	        fieldNames[i-1]=fields[i].getName();  
	    }  
        return fieldNames;
    }
    
    public void setQuery(BoolQueryBuilder query , StockBasePageInfo info){
    	if(!StringUtils.isEmpty(info.getDatas())){
    		Type type = new TypeToken<List<StockBaseInfoVo>>() {}.getType();
    		Gson gson = new Gson();
    		List<StockBaseInfoVo> list = gson.fromJson(info.getDatas(), type);
    		for (StockBaseInfoVo vo : list) {
    			if(StringUtils.isEmpty(vo.getValue())){
    				continue;
    			}
				if(vo.getType().equals("=")){
					query.must(QueryBuilders.termQuery(vo.getName(), vo.getValue()));
				}
				if(vo.getType().equals(">")){
					query.must(QueryBuilders.rangeQuery(vo.getName()).from(vo.getValue()).includeLower(false));
				}
				if(vo.getType().equals("<")){
					query.must(QueryBuilders.rangeQuery(vo.getName()).to(vo.getValue()).includeUpper(false));
				}
				if(vo.getType().equals(">=")){
					query.must(QueryBuilders.rangeQuery(vo.getName()).from(vo.getValue()).includeLower(true));
				}
				if(vo.getType().equals("<=")){
					query.must(QueryBuilders.rangeQuery(vo.getName()).to(vo.getValue()).includeUpper(true));
				}
			}
    	}
    }
}