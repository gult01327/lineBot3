package test.com.linebot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LineBot3Test {
	private static final Logger logger = LoggerFactory.getLogger(LineBot3Test.class);
	/**
	 * // 回覆多筆座標 public void
	 * handleNearLocationMessageEvent(MessageEvent<TextMessageContent> event, String
	 * nearbyPlaces) { String replyToken = event.getReplyToken();
	 * logger.info("準備回傳多筆座標"); if (nearbyPlaces.length() > 1) { // 將資料拆分並寫入messages
	 * List<Message> messages = new ArrayList<>(); String[] token =
	 * nearbyPlaces.split(";"); for (int i = 0; i < token.length; i++) { String[]
	 * object = token[i].split(","); String name = (object[0]); double lat =
	 * (Double.parseDouble(object[1])); double lng =
	 * (Double.parseDouble(object[2])); Message replyMessage = new
	 * LocationMessage("location", name, lat, lng); messages.add(replyMessage);
	 * System.out.println("飲料店:" + name + ",緯度:" + lat + ",經度:" + lng); } // 分批發送消息
	 * int maxMessagesPerRequest = 5; int messageCount = messages.size();
	 * 
	 * for (int i = 0; i < messageCount; i += maxMessagesPerRequest) { int endIndex
	 * = Math.min(i + maxMessagesPerRequest, messageCount); List<Message>
	 * subMessages = messages.subList(i, endIndex); System.out.println("發送消息-i:" + i
	 * + ",endIndex:" + endIndex );
	 * 
	 * ReplyMessage replyMessage = new ReplyMessage(replyToken, subMessages);
	 * System.out.println("replyToken:" + replyToken + ",subMessages:" + subMessages
	 * );
	 * 
	 * // 初始化Line Messaging Client LineMessagingClientBuilder builder =
	 * LineMessagingClient.builder("u2559vPjHa8bDO7hrn0C232jQHdcC2NG68Fo6bGl7VRxDc36eT7w74pWlM0SzbIsCvxEKPJa7byGFX9KIOGDYz5TUFoYnig574mtiCFY5NF3S73DpPstr8rmYejYCDpm5QvFgNZL8mRwlhHiykrzNQdB04t89/1O/w1cDnyilFU=");
	 * LineMessagingClient lineMessagingClient = builder.build();
	 * lineMessagingClient.replyMessage(replyMessage).join(); // 使用 .join()
	 * 方法等待異步操作完成 System.out.println("replyMessage:" + replyMessage); } } else {
	 * TextMessage replyMessage = new TextMessage("查無附近飲料店"); reply(replyMessage,
	 * event.getReplyToken()); } }
	 */
	
	/**
	 * 取得裝置當前位置(取到Server位置) private static String getCurrentLocation() { try { //
	 * 獲取公網IP地址 URL ipApiUrl = new URL("https://api.ipify.org?format=json");
	 * HttpURLConnection ipConnection = (HttpURLConnection)
	 * ipApiUrl.openConnection(); ipConnection.setRequestMethod("GET"); int
	 * ipResponseCode = ipConnection.getResponseCode(); if (ipResponseCode ==
	 * HttpURLConnection.HTTP_OK) { BufferedReader ipReader = new BufferedReader(new
	 * InputStreamReader(ipConnection.getInputStream())); String ipResponse =
	 * ipReader.readLine(); JsonParser ipParser = new JsonParser(); JsonObject
	 * ipJson = ipParser.parse(ipResponse).getAsJsonObject(); String ipAddress =
	 * ipJson.get("ip").getAsString();
	 * 
	 * // 使用Geolocation API獲取設備當前位置信息 URL locationApiUrl = new
	 * URL("https://www.googleapis.com/geolocation/v1/geolocate?key=" +
	 * "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA"); HttpURLConnection
	 * locationConnection = (HttpURLConnection) locationApiUrl.openConnection();
	 * locationConnection.setRequestMethod("POST");
	 * locationConnection.setRequestProperty("Content-Type", "application/json");
	 * locationConnection.setDoOutput(true); String requestBody = "{\"considerIp\":
	 * \"true\",\"wifiAccessPoints\": []}";
	 * locationConnection.getOutputStream().write(requestBody.getBytes());
	 * 
	 * int locationResponseCode = locationConnection.getResponseCode(); if
	 * (locationResponseCode == HttpURLConnection.HTTP_OK) { BufferedReader
	 * locationReader = new BufferedReader(new
	 * InputStreamReader(locationConnection.getInputStream())); StringBuilder
	 * locationResponse = new StringBuilder(); String line; while ((line =
	 * locationReader.readLine()) != null) { locationResponse.append(line); }
	 * locationReader.close(); JsonParser locationParser = new JsonParser();
	 * JsonObject locationJson =
	 * locationParser.parse(locationResponse.toString()).getAsJsonObject();
	 * JsonObject location = locationJson.get("location").getAsJsonObject(); double
	 * latitude = location.get("lat").getAsDouble(); double longitude =
	 * location.get("lng").getAsDouble();
	 * 
	 * // 返回当前位置信息 return latitude + "," + longitude; //緯度,經度 } } } catch (Exception
	 * e) { e.printStackTrace(); }
	 * 
	 * return "無法獲取當前位置信息。"; }
	 */


	/**
	 * // 回覆多筆座標 public static void handleNearLocationMessageEventmain(String
	 * nearbyPlaces) {
	 * 
	 * logger.info("準備回傳多筆座標"); if (nearbyPlaces.length() > 1) { // 將資料拆分並寫入messages
	 * List<Message> messages = new ArrayList<>(); String[] token =
	 * nearbyPlaces.split(";"); for (int i = 0; i < token.length; i++) { String[]
	 * object = token[i].split(","); String name = (object[0]); double lat =
	 * (Double.parseDouble(object[1])); double lng =
	 * (Double.parseDouble(object[2])); Message replyMessage = new
	 * LocationMessage("location", name, lat, lng); messages.add(replyMessage);
	 * System.out.println("飲料店:" + name + ",緯度:" + lat + ",經度:" + lng); }
	 * 
	 * // 分批發送消息 int maxMessagesPerRequest = 5; int messageCount = messages.size();
	 * 
	 * for (int i = 0; i < messageCount; i += maxMessagesPerRequest) { int endIndex
	 * = Math.min(i + maxMessagesPerRequest, messageCount); List<Message>
	 * subMessages = messages.subList(i, endIndex); System.out.println("i:" + i +
	 * ",endIndex:" + endIndex ); } } }
	 * 
	 * //取得附近飲料店：店名（緯度，經度） private static String getNearbyPlaces(String location,
	 * String keyword) { try { String encodedLocation = URLEncoder.encode(location,
	 * "UTF-8"); String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
	 * 
	 * // 使用Places API获取附近的饮料店信息 URL placesApiUrl = new
	 * URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
	 * + encodedLocation + "&radius=1000&keyword=" + encodedKeyword + "&key=" +
	 * "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA"); HttpURLConnection
	 * placesConnection = (HttpURLConnection) placesApiUrl.openConnection();
	 * placesConnection.setRequestMethod("GET");
	 * 
	 * int placesResponseCode = placesConnection.getResponseCode(); if
	 * (placesResponseCode == HttpURLConnection.HTTP_OK) { BufferedReader
	 * placesReader = new BufferedReader(new
	 * InputStreamReader(placesConnection.getInputStream())); StringBuilder
	 * placesResponse = new StringBuilder(); String line; while ((line =
	 * placesReader.readLine()) != null) { placesResponse.append(line); }
	 * placesReader.close();
	 * 
	 * // 解析JSON JsonParser placesParser = new JsonParser(); JsonObject jsonResponse
	 * = placesParser.parse(placesResponse.toString()).getAsJsonObject();; JsonArray
	 * results = jsonResponse.getAsJsonArray("results");
	 * 
	 * StringBuilder nearbyPlaces = new StringBuilder(); for (int i = 0; i <
	 * results.size(); i++) { JsonObject place = results.get(i).getAsJsonObject();
	 * JsonObject nearLocation =
	 * place.getAsJsonObject("geometry").getAsJsonObject("location");
	 * 
	 * String name = place.get("name").getAsString(); double lat =
	 * nearLocation.get("lat").getAsDouble(); double lng =
	 * nearLocation.get("lng").getAsDouble();
	 * 
	 * nearbyPlaces.append(name).append(","); nearbyPlaces.append(lat).append(",");
	 * nearbyPlaces.append(lng).append(";"); }
	 * 
	 * return nearbyPlaces.toString(); } } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * return "無法獲取附近的飲料店信息。"; }
	 */
	
	/**
	 * public static void main(String[] args) { 
	 * TestParamsDto dto = new
	 * TestParamsDto(); System.out.println("沒有設參數的物件:"+dto.printParam());
	 * System.out.println("沒有設參數的物件 plus():"+dto.plus()); TestParamsDto dto2 = new
	 * TestParamsDto(1,2); System.out.println("有參數的物件：" + dto2.printParam());
	 * System.out.println("有參數的物件 plus()：" + dto2.plus()); }
	 */
	
	/**
	public static void main(String[] args) { 
		Detail detail = new Detail();
		detail.setShopName("發發");
		detail.setDrink("飲料");
		detail.setSugar("微糖");
		detail.setIce("去冰");
		detail.setSize("大");
		detail.setPrice(100);
		detail.setUserName("小馬");
		detail.setInputdate(new Date());
		detail.setUpdate(new Date());
		detail.setUpdateName("小馬");
		detail.setStatus("0");
		System.out.println("========開始新增飲料=======");
		detailService.save(detail);
		System.out.println("========回傳新增成功訊息=======");
	}*/
	
}
