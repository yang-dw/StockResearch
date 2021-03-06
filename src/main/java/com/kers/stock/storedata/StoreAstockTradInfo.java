package com.kers.stock.storedata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.artbulb.httpmodel.HttpClientEx;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kers.esmodel.BaseCommonConfig;
import com.kers.esmodel.QueryComLstData;
import com.kers.esmodel.UtilEs;
import com.kers.httpmodel.BaseConnClient;
import com.kers.stock.bean.StockBaseInfo;
import com.kers.stock.bean.StockDetailInfoBean;
import com.kers.stock.bean.StockRealBean;
import com.kers.stock.bean.StoreTrailer;
import com.kers.stock.strage.StockStragEnSey;
import com.kers.stock.utils.CsvHandUtils;
import com.kers.stock.utils.FilePath;
import com.kers.stock.utils.TimeUtils;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Index;
import io.searchbox.indices.DeleteIndex;

public class StoreAstockTradInfo {

	public final static String stockHisCrawUrl = "http://quotes.money.163.com/service/chddata.html?code=";
	public final static String  stockRealTimeUrl="http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=2&num=5000&sort=symbol&asc=1&node=hs_a&symbol=&_s_r_a=init#";
//symbol:"sz300720",code:"300720",name:"海川智能",trade:"22.510",pricechange:"2.050",changepercent:"10.020",buy:"22.510",sell:"0.000",settlement:"20.460",open:"22.510",high:"22.510",low:"22.510",volume:2300,amount:51773,ticktime:"11:35:03",per:32.157,pb:5.104,mktcap:162072,nmc:40518,turnoverratio:0.01278
	static ExecutorService executorServiceLocal = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(30));

	
	@SuppressWarnings("all")
	public static void getHistoryData() throws Exception {
		  final String  sfd=new SimpleDateFormat("yyyyMMdd").format(new Date());
//		List<String> filePath =CommonBaseStockInfo.getAllAStockInfo();// FileUtils.readLines(new File(CommonBaseStockInfo.astockfilePath));
		List<StockDetailInfoBean>  lstBean =StockDetailInfoHand.getDetailForNetLst();//Lists.newArrayList();//
		
		//深证成指
		
		for (final StockDetailInfoBean bean : lstBean) {
			 final String s = bean.getStockCode();

			executorServiceLocal.execute(new Thread() {

				public void run() {
					String code = s.split(",")[0];
					String scode = s.startsWith("6") ? "0" + code : "1" + code;
					if(code.equals(CommonBaseStockInfo.SPEC_STOCK_CODE_SH)||code.equals(CommonBaseStockInfo.SPEC_STOCK_CODE_SZ))
						scode=code;
					//String scode = s.startsWith("6") ? "0" + code : "1" + code;
					 
					String webUri = stockHisCrawUrl + scode + "&start=20170101&end="+sfd;
					try {
						HttpClientEx.downloadFromUri(webUri, FilePath.savePathsuff + code + ".csv");
					} catch (Exception e) {
						e.printStackTrace();
					}
				};

			});

		}

	}

	public static List<StockBaseInfo> getstockBaseInfoFile( StockBaseInfo  beanParam , StockDetailInfoBean info,StoreTrailer  storeTrailer) throws Exception {
		String stockCode=beanParam.getStockCode();
		  final StockStragEnSey stockStragEnSey = new StockStragEnSey();
		String absPath = FilePath.savePathsuff + stockCode + ".csv";
		CsvHandUtils csvHandUtils = new CsvHandUtils(absPath);
		List<List<String>> lstSource = csvHandUtils.readCSVFile();
		List<StockBaseInfo> result = Lists.newArrayList();
		for (int i = lstSource.size() - 1; i >=1; i--) {
			List<String> objdata = lstSource.get(i);
			//日期,股票代码,名称,收盘价,最高价,最低价,开盘价,前收盘,涨跌额,涨跌幅,换手率(10),成交量,成交金额(12),总市值(13),流通市值(14),成交笔数(15)
			if(!(objIsEmpty(objdata.get(6))||objIsEmpty(objdata.get(3))||objIsEmpty(objdata.get(4))||objIsEmpty(objdata.get(5))||objdata.get(9).equals("None"))){///|StringUtils.isBlank(stockBaseInfo.getCjbs()))){//!(stockBaseInfo.getOpen()==0||stockBaseInfo.getClose()==0||stockBaseInfo.getHigh()==0||stockBaseInfo.getLow()==0||StringUtils.isBlank(stockBaseInfo.getCjbs()))){
				String volumn=objdata.get(11);
				
				if(stockCode.length()==6){  //排除深市  上证
					Long  vnm = Long.parseLong(volumn)/100;
					volumn=vnm+"";
					
				}
				StockBaseInfo stockBaseInfo = new StockBaseInfo(objdata.get(0), objdata.get(6), objdata.get(4),
						objdata.get(5), objdata.get(3), volumn, objdata.get(9), stockCode, objdata.get(2),objdata.get(10),objdata.get(12),objdata.get(13),objdata.get(14),objdata.size()<15?null: objdata.get(14),TimeUtils.dayForWeek(objdata.get(0))+"");
				if(info!=null){
					stockBaseInfo.setIndustry(info.getIndustry());
					stockBaseInfo.setArea(info.getArea());
					stockBaseInfo.setPe(info.getPe());
				}
			
				if(storeTrailer!=null){
					double npe=storeTrailer.getNpe();
					if( info.getTotals()!=0&&storeTrailer.getJlr()!=0)
					  npe = stockBaseInfo.getClose() / (storeTrailer.getJlr() / info.getTotals());
					
					stockBaseInfo.setNpe(npe);
				}
				//002252
				stockBaseInfo.setSbType(beanParam.getSbType());
				if(i==1)
			   stockBaseInfo.setLsImp(beanParam.getLsImp());
				if(stockBaseInfo.getZsz()>0||((CommonBaseStockInfo.SPEC_STOCK_CODE_SH.equals(stockCode)||CommonBaseStockInfo.SPEC_STOCK_CODE_SZ.equals(stockCode))))//排除停牌情况
				result.add(stockBaseInfo);
			}
			//|stockBaseInfo.getRises()))
			
		}
//		StockRealBean bean = StoreRealSet.getBeanByCode(stockCode);
//		StockBaseInfo stockBaseInfo = new StockBaseInfo(bean.getUpdate().split("[ ]")[0].replace("/", "-"), bean.getOpen()+"", bean.getHigh()+"",
//				bean.getLow()+"", bean.getPrice()+"", bean.getVolume()+"", df.format((bean.getPrice()-bean.getYestclose()) /bean.getYestclose() * 100) +"", stockCode, bean.getName(),"","","","","","");
//		if(info!=null){
//			stockBaseInfo.setIndustry(info.getIndustry());
//			stockBaseInfo.setArea(info.getArea());
//		}
//		result.add(stockBaseInfo);
		stockStragEnSey.addStockBaseInfos(result);
		stockStragEnSey.computeStockIndex();
		return result;

	}
	
	public      static  void  wDataRealToEs() throws Exception{
		List<StockDetailInfoBean> lstSource =StockDetailInfoHand.getDetailForLst();
		 final JestClient  jestClient =BaseCommonConfig.clientConfig();
		 final Map<String , StockDetailInfoBean> map =QueryComLstData.getDetailInfo();
		 
		for(final StockDetailInfoBean  bean:lstSource){
			 final String sat=bean.getStockCode();
//			if(sat.equals("603612")){
			executorServiceLocal.execute(new Thread(){
				@Override
				public void run() {
			          try {
//			        	  List<StockBaseInfo> lstInfo = Lists.newArrayList();
			        	  List<StockBaseInfo> lstInfo = getstockBaseInfo(sat ,  map.get(sat));
//			        	 lstInfo.add(info);
			        	 insBatchEs(lstInfo, jestClient, "stockpcse");
					} catch (Exception e) {
						System.out.println(sat);
						e.printStackTrace();
					}
						
				}
			});
		}
		
	}
	
	public static List<StockBaseInfo> getstockBaseInfo(String stockCode , StockDetailInfoBean info ) throws Exception {
		  final StockStragEnSey stockStragEnSey = new StockStragEnSey();
		  List<StockBaseInfo> list = Lists.newArrayList();
		  DecimalFormat df = new DecimalFormat("#.00");
		String absPath = FilePath.savePathsuff + stockCode + ".csv";
		CsvHandUtils csvHandUtils = new CsvHandUtils(absPath);
		List<List<String>> lstSource = csvHandUtils.readCSVFile();
		List<StockBaseInfo> result = Lists.newArrayList();
		for (int i = lstSource.size() - 1; i >=1; i--) {
			List<String> objdata = lstSource.get(i);
			if(!(objIsEmpty(objdata.get(6))||objIsEmpty(objdata.get(3))||objIsEmpty(objdata.get(4))||objIsEmpty(objdata.get(5))||objdata.get(9).equals("None"))){///|StringUtils.isBlank(stockBaseInfo.getCjbs()))){//!(stockBaseInfo.getOpen()==0||stockBaseInfo.getClose()==0||stockBaseInfo.getHigh()==0||stockBaseInfo.getLow()==0||StringUtils.isBlank(stockBaseInfo.getCjbs()))){
				StockBaseInfo stockBaseInfo = new StockBaseInfo(objdata.get(0), objdata.get(6), objdata.get(4),
						objdata.get(5), objdata.get(3), objdata.get(11), objdata.get(9), stockCode, objdata.get(2),objdata.get(10),objdata.get(12),objdata.get(13),objdata.get(14),objdata.size()<15?null: objdata.get(14),TimeUtils.dayForWeek(objdata.get(0))+"");
				if(info!=null){
					stockBaseInfo.setIndustry(info.getIndustry());
					stockBaseInfo.setArea(info.getArea());
					stockBaseInfo.setPe(info.getPe());
				}
				result.add(stockBaseInfo);
			}
		}
		
		//增加今天的实时数据
			StockRealBean bean = StoreRealSet.getBeanByCode(stockCode);
			if(null == bean || bean.getOpen() ==0){
				return list;
			}
			StockBaseInfo stockBaseInfo = new StockBaseInfo(bean.getUpdate().split("[ ]")[0].replace("/", "-"), bean.getOpen()+"", bean.getHigh()+"",
					bean.getLow()+"", bean.getPrice()+"", bean.getVolume()+"", df.format((bean.getPrice()-bean.getYestclose()) /bean.getYestclose() * 100) +"", stockCode, bean.getName(),"","","","","","");
			if(info!=null){
				stockBaseInfo.setIndustry(info.getIndustry());
				stockBaseInfo.setArea(info.getArea());
			}
			result.add(stockBaseInfo);
		stockStragEnSey.addStockBaseInfos(result);
		stockStragEnSey.computeStockIndex();
		if(result.size() > 0){
			
			StockBaseInfo h = result.get(result.size()-1);
			if(result.size() > 1){
				StockBaseInfo h2 = result.get(result.size()-2);
				h2.setNextRises(h.getRises());
				list.add(h2);
			}
			list.add(h);
		}
		return list;

	}
	
	public static boolean objIsEmpty(String str){
		if(StringUtils.isEmpty(str) || str.equals("0")||str.equals("None") ){
			return true;
		}
		return false;
	}

	public static Map<String, StockBaseInfo> fetchKeyStockInfo(String stockCode) throws IOException, ParseException {
		Map<String, StockBaseInfo> mapsInfo = Maps.newConcurrentMap();
		String absPath = FilePath.savePathsuff + stockCode + ".csv";
		CsvHandUtils csvHandUtils = new CsvHandUtils(absPath);
		List<List<String>> lstSource = csvHandUtils.readCSVFile();
		for (int i = lstSource.size() - 1; i > 0; i--) {
			List<String> objdata = lstSource.get(i);
			if(!(objIsEmpty(objdata.get(6))||objIsEmpty(objdata.get(3))||objIsEmpty(objdata.get(4))||objIsEmpty(objdata.get(5))||objdata.get(9).equals("None"))){
				StockBaseInfo stockBaseInfo = new StockBaseInfo(objdata.get(0), objdata.get(6), objdata.get(4),
						objdata.get(5), objdata.get(3), objdata.get(11), objdata.get(9), stockCode, objdata.get(2),objdata.get(10),objdata.get(12),objdata.get(13),objdata.get(14),objdata.get(14),TimeUtils.dayForWeek(objdata.get(0))+"");
				mapsInfo.put(stockBaseInfo.getDate(), stockBaseInfo);
			}
			
		}
		return mapsInfo;

	}
	
	public static Map<String, StockBaseInfo>   getAllLastStockInfo() throws IOException, ParseException{
		Map<String, StockBaseInfo> mapsInfo = Maps.newConcurrentMap();
	
		
		File[] files= new File(FilePath.savePathsuff).listFiles();
		for (int i = 0; i < files.length; i++) {
			String absPath = files[i].getAbsolutePath();//FilePath.savePathsuff + stockCode + ".csv"; 
			CsvHandUtils csvHandUtils = new CsvHandUtils(absPath);
			List<List<String>> lstSource = csvHandUtils.readCSVFile();
			
			       if(lstSource.isEmpty()||lstSource.size()<=1||(!absPath.endsWith(".csv")))
			    	   continue;
				List<String> objdata = lstSource.get(1);
				String stockCode = objdata.get(1).substring(1);
				
				if(!(objIsEmpty(objdata.get(6))||objIsEmpty(objdata.get(3))||objIsEmpty(objdata.get(4))||objIsEmpty(objdata.get(5))||objdata.get(9).equals("None"))){
					StockBaseInfo stockBaseInfo = new StockBaseInfo(objdata.get(0), objdata.get(6), objdata.get(4),
							objdata.get(5), objdata.get(3), objdata.get(11), objdata.get(9), stockCode, objdata.get(2),objdata.get(10),objdata.get(12),objdata.get(13),objdata.get(14),objdata.get(14),TimeUtils.dayForWeek(objdata.get(0))+"");
					mapsInfo.put(stockCode, stockBaseInfo);
				}
		}
		
		return mapsInfo;
	}
	public static Map<String, StockBaseInfo> fetchKeyStockInfo(List<StockBaseInfo> lstSource ) throws IOException {
		Map<String, StockBaseInfo> mapsInfo = Maps.newConcurrentMap();
		for(StockBaseInfo  infos:lstSource){
			mapsInfo.put(infos.getStockCode(), infos)	;
		}
		return mapsInfo;

	}

	public static StockBaseInfo getStockContinuePrice(Map<String, StockBaseInfo> lstResult, String date, boolean rise) {
		// true + false -
		StockBaseInfo info = null;
		Date cdate = TimeUtils.stringToDate(date, TimeUtils.DEFAULT_DATEYMD_FORMAT);
		for (int i = 1; i < 100; i++) {
			info = lstResult.get(date);
			if (info != null) {
				return info;
			}

			if (rise)
				date = TimeUtils.format(TimeUtils.addDay(cdate, i), TimeUtils.DEFAULT_DATEYMD_FORMAT);
			else
				date = TimeUtils.format(TimeUtils.addDay(cdate, -i), TimeUtils.DEFAULT_DATEYMD_FORMAT);
		}
		return info;
	}
	public static void insBatchEs(List<StockBaseInfo> list,JestClient jestClient,String  indexIns) throws Exception{
		 
		 
		  int i =0;
		  Bulk.Builder bulkBuilder = new Bulk.Builder();
		 for(StockBaseInfo bean:list){
			   i++;
			   // System.out.println(bean.getUnionId());
			 Index index =new Index.Builder(bean).index(indexIns).type(bean.getDate().substring(0, 4)).id(bean.getStockCode()+bean.getDate()).build();//type("walunifolia").build();
			 bulkBuilder.addAction(index);
			 if(i%5000==0){
				 jestClient.execute(bulkBuilder.build());
				 bulkBuilder = new   Bulk.Builder();
			 }
		 	}
		 jestClient.execute(bulkBuilder.build());
		 //jestClient.shutdownClient();
	 }
	public      static  void  wDataToEs() throws Exception{
		List<StockDetailInfoBean> lstSource =StockDetailInfoHand.getDetailForLst();// CommonBaseStockInfo.getAllAStockInfo();
		
		
		 final JestClient  jestClient =BaseCommonConfig.clientConfig();
		 final Map<String , StockDetailInfoBean> map =QueryComLstData.getDetailInfo(); //getInfoByCsv();
		 final Map<String , StoreTrailer> mapStoreTrailer =QueryComLstData.getStoreTrailerMapsInfo(); //getInfoByCsv();
		 try {
			 UtilEs.deleteIndex(CommonBaseStockInfo.ES_INDEX_STOCK_STOCKPCSE, "", "");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		 
		 
		for(final StockDetailInfoBean  bean:lstSource){ //导入基础数据
			 final String stockCode=bean.getStockCode();
			final  StockBaseInfo stockBaseInfo = new StockBaseInfo();
			 stockBaseInfo.setStockCode(stockCode);
			executorServiceLocal.execute(new Thread(){
				@Override
				public void run() {
			          try {
			        	 List<StockBaseInfo> lstInfo = getstockBaseInfoFile(stockBaseInfo ,  map.get(stockCode),mapStoreTrailer.get(stockCode));
			  			 insBatchEs(lstInfo, jestClient, CommonBaseStockInfo.ES_INDEX_STOCK_STOCKPCSE);
					} catch (Exception e) {
						System.out.println(stockCode);
						e.printStackTrace();
					}
				}
			});
		}
		 //导入特殊数据  龙虎榜 最近火热数据
		 List<StockBaseInfo>  lstEcho=StockStrongRiseHand.getstockBaseInfoFile();
			for(final StockBaseInfo  bean:lstEcho){ //导入基础数据
				 final String stockCode=bean.getStockCode();
				executorServiceLocal.execute(new Thread(){
					@Override
					public void run() {
				          try {
				        	 List<StockBaseInfo> lstInfo = getstockBaseInfoFile(bean ,  map.get(stockCode),mapStoreTrailer.get(stockCode));
				        	
				  			 insBatchEs(Lists.newArrayList(lstInfo.get(lstInfo.size()-1)), jestClient, CommonBaseStockInfo.ES_INDEX_STOCK_STOCKPCSE);
						} catch (Exception e) {
							System.out.println(stockCode);
							e.printStackTrace();
						}
					}
				});
			}
	}
	
//	public static Map<String , StockFuncDetailInfo> getInfoByCsv() throws IOException{
//		Map<String , StockFuncDetailInfo> map = Maps.newHashMap();
//		CsvHandUtils util = new CsvHandUtils(FilePath.stockFuncDetailInfoPath);
//		List<List<String>> list = util.readCSVFile();
//		for (int i = 1; i < list.size(); i++) {
//			List<String> data = list.get(i);
//			StockFuncDetailInfo info = new StockFuncDetailInfo();
//			info.setCode(data.get(0));
//			info.setName(data.get(1));
//			info.setIndustry(data.get(2));
//			info.setArea(data.get(3));
//			map.put(data.get(0), info);
////			System.out.println("chax:"+data.get(2));
//		}
//		return map;
//	}
	
	public static List<StockRealBean>   getRealTimeData() throws ClientProtocolException, IOException{
		List<StockRealBean>  lstRes= Lists.newArrayList();
		Type type = new TypeToken<List<StockRealBean>>() {
		}.getType();
		Gson gson = new Gson();
		
//		String ccc="http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=0&num=100&sort=symbol&asc=0&node=hs_a&symbol=&_s_r_a=init#";
	     for (int i = 1; i <360; i++) {
	 		String ccc="http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page="+i+"&num=100&sort=symbol&asc=0&node=hs_a&symbol=&_s_r_a=init#";
	 		String content=BaseConnClient.baseGetReq(ccc);
	 		System.out.println(content);
	 		List<StockRealBean> lstResult = gson.fromJson(content, type);
           
            if(null!=lstResult)
            lstRes.addAll(lstResult);
		}
//	     System.out.println(lstRes);
	     return lstRes;
		
	}
	public static void main(String[] args) throws ClientProtocolException, IOException, Exception {
		
		getHistoryData();
//		executorServiceLocal.shutdown();
//		 System.out.println(getAllLastStockInfo());
		Thread.sleep(1000*100);
//		System.out.println("start write Es data ");
		wDataToEs();
		//Thread.sleep(1000*60);
		//executorServiceLocal.shutdown();
//		wDataRealToEs();
	}
	}
