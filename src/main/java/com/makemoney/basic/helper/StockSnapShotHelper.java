package com.makemoney.basic.helper;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.makemoney.DB.DBHelper;
import com.makemoney.basic.bean.Stock;
import com.makemoney.basic.bean.StockSnapShot;
import com.makemoney.basic.constvalue.Market;
import com.mongodb.MongoClient;

/**
 * @author cheruixi
 *
 */

public class StockSnapShotHelper {

	private static final String APIPath = "http://hq.sinajs.cn/list=";
	private static String proxyServer;
	private static int proxyServerPort;
	private static StockSnapShot previousStoredSnapShot;
	
	/**
	 * @param stockCode
	 * @param market
	 * @return request URL for information.
	 * @author Jesse.Chen
	 * @Date 2015-12-10
	 */
	private static String generateURL(String stockCode, Market market) {

		return APIPath + market.getName() + stockCode;
	}
	
	
	/**
	 * @param URL
	 * @return stock information string.
	 * @throws Exception
	 */
	private static String requestInfo(String URL) throws Exception {
		URL stockInfoURL = new URL(URL);
		URLConnection connection;
		if(proxyServer != null){
			InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(proxyServer),proxyServerPort);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
			connection = stockInfoURL.openConnection(proxy);
		} else {
			connection = stockInfoURL.openConnection();
		}
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		byte[] bytes = new byte[inputStream.available()];
		inputStream.read(bytes);
		inputStream.close();
		String result = new String(bytes, "GBK");
		return result;
	}
	
	
	/**
	 * @param stockInfo
	 * @return parse the information to stock snap shot
	 * @throws Exception
	 */
	private static StockSnapShot parseSnapshot(String stockInfo, String stockCode) throws Exception {
		int index = stockInfo.indexOf("=");
		String usableInfo = stockInfo.substring(index+2, stockInfo.length()-3);
		String[] infoList = usableInfo.split(",");
		StockSnapShot stockSnapShot = new StockSnapShot();
		stockSnapShot.setName(infoList[0]);
		stockSnapShot.setCode(stockCode);
		stockSnapShot.setTodayInitPrice(Float.parseFloat(infoList[1]));
		stockSnapShot.setYesterdayEndPrice(Float.parseFloat(infoList[2]));
		stockSnapShot.setPrice(Float.parseFloat(infoList[3]));
		stockSnapShot.setTodayHighestPrice(Float.parseFloat(infoList[4]));
		stockSnapShot.setTodayLowestPrice(Float.parseFloat(infoList[5]));
		stockSnapShot.setTotalAmount(Long.parseLong(infoList[8]));
		stockSnapShot.setBuyOneAmount(Long.parseLong(infoList[10]));
		stockSnapShot.setBuyOne(Float.parseFloat(infoList[11]));
		stockSnapShot.setBuyTwoAmount(Long.parseLong(infoList[12]));
		stockSnapShot.setBuyTwo(Float.parseFloat(infoList[13]));
		stockSnapShot.setBuyThreeAmount(Long.parseLong(infoList[14]));
		stockSnapShot.setBuyThree(Float.parseFloat(infoList[15]));
		stockSnapShot.setBuyFourAmount(Long.parseLong(infoList[16]));
		stockSnapShot.setBuyFour(Float.parseFloat(infoList[17]));
		stockSnapShot.setBuyFiveAmount(Long.parseLong(infoList[18]));
		stockSnapShot.setBuyFive(Float.parseFloat(infoList[19]));
		stockSnapShot.setSellOneAmount(Long.parseLong(infoList[20]));
		stockSnapShot.setSellOne(Float.parseFloat(infoList[21]));
		stockSnapShot.setSellTwoAmount(Long.parseLong(infoList[22]));
		stockSnapShot.setSellTwo(Float.parseFloat(infoList[23]));
		stockSnapShot.setSellThreeAmount(Long.parseLong(infoList[24]));
		stockSnapShot.setSellThree(Float.parseFloat(infoList[25]));
		stockSnapShot.setSellFourAmount(Long.parseLong(infoList[26]));
		stockSnapShot.setSellFour(Float.parseFloat(infoList[27]));
		stockSnapShot.setSellFiveAmount(Long.parseLong(infoList[28]));
		stockSnapShot.setSellFive(Float.parseFloat(infoList[29]));
		stockSnapShot.setTime(new Date());
		return stockSnapShot;
	}

	/**
	 * @param stockCode
	 * @param market
	 * @throws Exception
	 * @purpose refresh the stock snapshot and store them into DB, basic data generator.
	 */
	public static void refreshSnapshot(String stockCode, Market market) throws Exception {
		
		String url = generateURL(stockCode, market);
		String responseString = requestInfo(url);
		System.out.println(url);
		System.out.println(responseString);
		if(responseString.length()>25){
			StockSnapShot snapShot = parseSnapshot(responseString, stockCode);
			storeStockSnapShot(snapShot);
		}
	}
	
	/**
	 * @param snapShot
	 * @purpose generate current amount, using current total amount - previous total amount
	 */
	private static void initCurrentAmount(StockSnapShot snapShot){
		
		if(previousStoredSnapShot != null){
			snapShot.setCurrenAmount(snapShot.getTotalAmount() - previousStoredSnapShot.getTotalAmount());
		} else {
			snapShot.setCurrenAmount(0);
		}
	}
	
	private static void storeStockSnapShot(StockSnapShot snapShot)throws Exception{
		
		initCurrentAmount(snapShot);
		MongoClient mongo = DBHelper.generateDB();
		Morphia morphia = new Morphia();
		morphia.map(StockSnapShot.class);
		Datastore ds = morphia.createDatastore(mongo, "Make_Money");
		ds.save(snapShot);
		previousStoredSnapShot = snapShot;
	}
	

	public static void main(String[] args) {
		try {
			MongoClient mongo = DBHelper.generateDB();
			Morphia morphia = new Morphia();
			Datastore ds = morphia.createDatastore(mongo, "Make_Money");
			List<Stock> stockList = ds.find(Stock.class).asList();
			proxyServer = "web-proxy-sha.chn.hp.com";
			proxyServerPort = 8080;
			Date requestDate = new Date();
			for (Stock stock : stockList){
				if(stock.getCode().equals("sh")){
					refreshSnapshot(stock.getCode(), Market.ShangHai);
				} else {
					refreshSnapshot(stock.getCode(), Market.ShenZhen);
				}
			}
			Date responseDate = new Date();
			System.out.println(responseDate.getTime() - requestDate.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
