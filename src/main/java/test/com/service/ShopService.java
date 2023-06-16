package test.com.service;

import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;

import test.com.dao.ShopDao;
import test.com.model.Shop;

@Service
public class ShopService {
	private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
	
	@Autowired
	private ShopDao shopDao;

	public String getGoogleMapLocation(String address) {
		logger.info("進入SERVICCE method: getGoogleMapLocation");
		// google map金鑰
		String GOOGLE_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
		GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build();
		try {
			GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
			if (results.length > 0) {
				// 取第一個结果的經緯度
				LatLng location = results[0].geometry.location;
				logger.info("取得輸入地址緯度: " + location.lat);
				logger.info("取得輸入地址經度: " + location.lng);
				return location.lat + "," + location.lng;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("獲取輸入地址位置訊息錯誤");
			return "X";
		}
		logger.info("獲取輸入地址位置訊息錯誤");
		return "X";

	}

	public FlexMessage handleNearLocationTemplate(MessageEvent<TextMessageContent> event, String location,
			String userName) throws Exception {
		logger.info("進入SERVICCE method: handleNearLocationTemplate");
//		String LINE_CHANNEL_ACCESS_TOKEN = "u2559vPjHa8bDO7hrn0C232jQHdcC2NG68Fo6bGl7VRxDc36eT7w74pWlM0SzbIsCvxEKPJa7byGFX9KIOGDYz5TUFoYnig574mtiCFY5NF3S73DpPstr8rmYejYCDpm5QvFgNZL8mRwlhHiykrzNQdB04t89/1O/w1cDnyilFU=";
		// 初始化Line Messaging Client
//		LineMessagingClientBuilder builder = LineMessagingClient.builder(LINE_CHANNEL_ACCESS_TOKEN);
//		LineMessagingClient lineMessagingClient = builder.build();

		String GOOGLE_MAPS_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
		// 建立Google Maps API客户端
		GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_MAPS_API_KEY).build();

		String[] latlng = location.split(",");
		// 查詢附近飲料店
		double latitude = Double.parseDouble(latlng[0]); // 傳入的緯度
		double longitude = Double.parseDouble(latlng[1]); // 傳入的經度
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
			String name = result.name; // 店名
			// 資料放入list
			Shop shop = new Shop();
			shop.setShopName(name);
			shop.setInputDate(new Date());
			shop.setInputName(userName);
			shop.setOrderStatus("0");
			shopDao.save(shop);
			// 設定圖片
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
		// 創建 FlexMessage
		FlexMessage flexMessage = FlexMessage.builder().altText("Nearby Drink Shops")
				.contents(Carousel.builder().contents(flexBubbles).build()).build();
		return flexMessage;
	}

	public FlexMessage NearLocationTemplate(MessageEvent<LocationMessageContent> event, String location,String userName)
			throws Exception {
		logger.info("進入SERVICCE method: NearLocationTemplate");

		String GOOGLE_MAPS_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
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

		// 創建 FlexMessage
		FlexMessage flexMessage = FlexMessage.builder().altText("Nearby Drink Shops")
				.contents(Carousel.builder().contents(flexBubbles).build()).build();
		return flexMessage;
	}

}