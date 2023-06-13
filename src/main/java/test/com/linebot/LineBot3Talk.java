package test.com.linebot;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineMessagingClientBuilder;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;
import test.com.service.DetailService;
import test.com.service.MainService;
import test.com.vo.Detail;

@RestController
@LineMessageHandler
@Slf4j
public class LineBot3Talk {
	private static final Logger logger = LoggerFactory.getLogger(LineBot3Talk.class);
	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private static MainService mainService;

	@Autowired
	private static DetailService detailService;
	
    @Bean
    public static DataSource dataSource() {
        String databaseUrl = System.getenv("JDBC_DATABASE_URL");
        return DataSourceBuilder.create()
                .url(databaseUrl)
                .build();
    }


	@EventMapping
	public void handle(MessageEvent<TextMessageContent> event){
		String originalMessageText = event.getMessage().getText();
		logger.info("Hello, Heroku log!");
		// 範例：+飲料 甜度 冰塊 大小 金額
		if (originalMessageText.substring(0, 1).equals("+") && originalMessageText.length() > 1) {
			// 取得使用者資訊
			String userId = event.getSource().getUserId();
			UserProfileResponse userProfile;
			String userName = "";
			try {
				userProfile = lineMessagingClient.getProfile(userId).get();
				userName = userProfile.getDisplayName();
				System.out.println("userId:" + userId+",userName: "+userName);
			} catch (InterruptedException e) {
				System.out.println("InterruptedException-取得userName失敗");
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.out.println("ExecutionException-取得userName失敗");
				e.printStackTrace();
			}
			logger.info("新增飲料");
			String[] str = originalMessageText.substring(1).split(" ");
			//檢核輸入格式
			if (str.length != 5) {
				TextMessage replyMessage = new TextMessage("@"+userName + " 注意空格位置,請輸入『+飲料 甜度 冰塊 大小 金額』");
				reply(replyMessage, event.getReplyToken());
			}
			String drink = str[0];
			String sugar = str[1];
			String ice = str[2];
			String size = str[3];
			String pricestr = str[4];
			int price = Integer.parseInt(pricestr);
			System.out.println("新增飲料："+drink+",甜度："+sugar+",冰塊："+ice+",大小："+size+",價錢："+pricestr);
			// 檢核輸入內容格式
			if (sugar.contains("冰") || sugar.contains("溫") || sugar.contains("熱") || ice.contains("糖")
					|| ice.contains("甜")) {
				TextMessage replyMessage = new TextMessage("@" + userName + " 請依排列順序輸入『+飲料 甜度 冰塊 大小 金額』");
				reply(replyMessage, event.getReplyToken());
			}
			Detail detail = new Detail();
			detail.setDrink(drink);
			detail.setSugar(sugar);
			detail.setIce(ice);
			detail.setSize(size);
			detail.setPrice(price);
			detail.setUserName(userName);
			detail.setInputdate(new Date());
			detail.setUpdate(new Date());
			detail.setUpdateName(userName);
			detail.setStatus("0");
			System.out.println("========開始新增飲料=======");
			detailService.save(detail);
			System.out.println("========回傳新增成功訊息=======");
			TextMessage replyMessage = new TextMessage("@" + userName + "儲存成功");
			reply(replyMessage, event.getReplyToken());
		} else if (originalMessageText.equals("我誰")) {
			try {
				logger.info("我要瘋子");
				handlePictureMessageEvent(event);
			} catch (URISyntaxException e) {
				logger.info("我瘋子失敗");
			}
		} else if (originalMessageText.substring(0, 1).equals("?") && originalMessageText.length() > 1) {
			// 地址查詢：以？開頭並輸入地址
			String place = originalMessageText.substring(1);
			System.out.println("輸入地址:" + place);
			// 取得地址緯度、經度
			String location = getGoogleMapLocation(place);
			System.out.println("取得地址緯度、經度:" + location);
			if (!location.equals("X")) {
				try {
					handleNearLocationTemplate(event, location);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					TextMessage replyMessage = new TextMessage("取得附近店家失敗");
					reply(replyMessage, event.getReplyToken());
				}
			} else {
				TextMessage replyMessage = new TextMessage("查無附近店家地址");
				reply(replyMessage, event.getReplyToken());
			}
		} else {
			logger.info("笑死");
			handleTextMessageEvent(event);
		}
	}

	// 回覆固定座標
	public void handleLocationMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("SUCCESS:單筆固定座標");
		// 收到文字訊息做回覆
		Message replyMessage = new LocationMessage("location", "基隆市中山區中和路168巷7弄54號", 25.06752, 121.585664);
		reply(replyMessage, event.getReplyToken());
	}

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
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("SUCCESS:笑死");
		TextMessage replyMessage = new TextMessage("笑死");
		reply(replyMessage, event.getReplyToken());
	}

	public void handlePictureMessageEvent(MessageEvent<TextMessageContent> event) throws URISyntaxException {
		logger.info("SUCCESS:我瘋子");
		URI originalContentUrl = new URI(
				"https://media.nownews.com/nn_media/thumbnail/2019/10/1570089924-27a9b9c9d7facd3422fe4610dd8ebe42-696x386.png");
		URI previewimageUrl = new URI(
				"https://media.nownews.com/nn_media/thumbnail/2019/10/1570089924-27a9b9c9d7facd3422fe4610dd8ebe42-696x386.png");
		Message replyMessage = new ImageMessage(originalContentUrl, previewimageUrl);
		reply(replyMessage, event.getReplyToken());
	}

	// 回傳單筆訊息
	private void reply(Message replyMessage, String replyToken) {
//		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, replyMessage);
		lineMessagingClient.replyMessage(reply);
	}

	// 回傳多筆訊息
	private void replyList(List<Message> messages, String replyToken) {
//		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, messages);
		lineMessagingClient.replyMessage(reply);
	}

	// 獲取輸入地址的經緯度
	private static String getGoogleMapLocation(String address) {
		// google map金鑰
		String GOOGLE_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
		GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build();
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
			if (results.length > 0) {
				// 取第一個结果的經緯度
				LatLng location = results[0].geometry.location;
				System.out.println("取得輸入地址緯度: " + location.lat);
				System.out.println("取得輸入地址經度: " + location.lng);
				return location.lat + "," + location.lng;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("獲取輸入地址位置訊息錯誤");
			return "X";
		}
		System.out.println("獲取輸入地址位置訊息錯誤");
		return "X";
	}

	@EventMapping
	public Message handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) throws URISyntaxException {
		logger.info("event:" + event);
		// 收到圖片做回覆
		return new StickerMessage("11537", " 52002735");
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		// 就是加入聊天室、離開聊天室等事件
		logger.info("event: " + event);
	}

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

	public void handleNearLocationTemplate(MessageEvent<TextMessageContent> event, String location)
			throws Exception {
		String GOOGLE_MAPS_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
		String LINE_CHANNEL_ACCESS_TOKEN = "u2559vPjHa8bDO7hrn0C232jQHdcC2NG68Fo6bGl7VRxDc36eT7w74pWlM0SzbIsCvxEKPJa7byGFX9KIOGDYz5TUFoYnig574mtiCFY5NF3S73DpPstr8rmYejYCDpm5QvFgNZL8mRwlhHiykrzNQdB04t89/1O/w1cDnyilFU=";

		// 初始化Line Messaging Client
		LineMessagingClientBuilder builder = LineMessagingClient.builder(LINE_CHANNEL_ACCESS_TOKEN);
		LineMessagingClient lineMessagingClient = builder.build();

		// 建立Google Maps API客户端
		GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_MAPS_API_KEY).build();

		String[] latlng = location.split(",");
		// 查詢附近飲料店
		double latitude = Double.parseDouble(latlng[0]); // 使用者的緯度
		double longitude = Double.parseDouble(latlng[1]); // 使用者的經度
		int radius = 1000; // 搜索半徑（單位：米）
		String type = "飲料店"; // 查詢關鍵键字

		NearbySearchRequest request = PlacesApi.nearbySearchQuery(context, new LatLng(latitude, longitude))
				.radius(radius).keyword(type);

		PlacesSearchResponse response = request.await();

		// 建立Flex Message列表
		List<PlacesSearchResult> results = Arrays.asList(response.results);
		int maxResults = Math.min(results.size(), 5); // 查詢5筆
		List<Bubble> flexBubbles = new ArrayList<>();

		for (int i = 0; i < maxResults; i++) {
			PlacesSearchResult result = results.get(i);
			String name = result.name;
			String address = result.vicinity;
			double resultLatitude = result.geometry.location.lat; // 緯度
			double resultLongitude = result.geometry.location.lng; // 經度
			URI photoUrl = new URI(
					"https://media.nownews.com/nn_media/thumbnail/2019/10/1570089924-27a9b9c9d7facd3422fe4610dd8ebe42-696x386.png");

			// 點擊圖片觸發的action
			String placeId = result.placeId; // 取得店家ID
			String encodedPlaceId = URLEncoder.encode(placeId, "UTF-8");
			String mapWebUrl = "https://www.google.com/maps/place/?q=place_id:" + encodedPlaceId;

			// 獲取店家資訊
			PlaceDetails placeDetails = PlacesApi.placeDetails(context, placeId).language("zh-TW") // 指定語言為中文
					.await();
			// 獲取評分星數
			double rating = placeDetails.rating;
			// 格式化評分數到小數點第一位
			String formattedRating = new DecimalFormat("#.#").format(rating);
			String starIcon = "★";
			// 獲取中文地址
			String chineseAddress = placeDetails.formattedAddress;
			// 取得店家的營業時間
			String todayOpeningHours = null;
			if (placeDetails.openingHours != null && placeDetails.openingHours.weekdayText != null) {
				LocalDate currentDate = LocalDate.now();
				DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
				String[] weekdayText = placeDetails.openingHours.weekdayText;

				// 判斷今天是星期幾
				int dayOfWeekIndex = currentDayOfWeek.getValue() - 1; // 假設 API 中星期一為第一個元素，而 Java DayOfWeek 中星期一為第一天

				if (dayOfWeekIndex >= 0 && dayOfWeekIndex < weekdayText.length) {
					String todayHours = weekdayText[dayOfWeekIndex];
					todayOpeningHours = todayHours.substring(todayHours.indexOf(":") + 1).trim();
				}
			}

			// 點擊圖片觸發的action
			Action action = new URIAction("Open Map", new URI(mapWebUrl), null);

			// 建立訊息模板
			Bubble bubble = Bubble.builder()
					.body(Box.builder().layout(FlexLayout.VERTICAL).contents(Arrays.asList(
							Text.builder().text(name).weight(Text.TextWeight.BOLD).size(FlexFontSize.LG)
									.margin(FlexMarginSize.NONE).build(),
							Text.builder().text("評分: " + formattedRating + starIcon).size(FlexFontSize.SM).wrap(true)
									.margin(FlexMarginSize.MD).build(),
							Text.builder().text("地址: " + chineseAddress).size(FlexFontSize.SM).wrap(true)
									.margin(FlexMarginSize.MD).build(),
							Text.builder().text("營業時間: " + todayOpeningHours).size(FlexFontSize.SM).wrap(true)
									.margin(FlexMarginSize.MD).build(),
							Image.builder().url(photoUrl).size(Image.ImageSize.FULL_WIDTH)
									.aspectMode(Image.ImageAspectMode.Cover).aspectRatio(Image.ImageAspectRatio.R1TO1)
									.margin(FlexMarginSize.MD).action(action) // 設置點擊操作
									.build()))
							.build())
					.build();

			flexBubbles.add(bubble);
		}

		// 创建 Flex Message
		FlexMessage flexMessage = FlexMessage.builder().altText("Nearby Drink Shops")
				.contents(Carousel.builder().contents(flexBubbles).build()).build();

		// 创建 PushMessage 并发送消息给 Line Bot
		String userId = event.getSource().getUserId();
		PushMessage pushMessage = new PushMessage(userId, flexMessage);
		lineMessagingClient.pushMessage(pushMessage).join();
	}

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
	 * public static void main(String[] args) { TestParamsDto dto = new
	 * TestParamsDto(); System.out.println("沒有設參數的物件:"+dto.printParam());
	 * System.out.println("沒有設參數的物件 plus():"+dto.plus()); TestParamsDto dto2 = new
	 * TestParamsDto(1,2); System.out.println("有參數的物件：" + dto2.printParam());
	 * System.out.println("有參數的物件 plus()：" + dto2.plus()); }
	 */
	
}
