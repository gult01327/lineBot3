package test.com.service;

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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;

import test.com.dao.DetailDao;
import test.com.model.Detail;

@Service
public class DetailService {
	private static final Logger logger = LoggerFactory.getLogger(DetailService.class);

	@Autowired
	private DetailDao detailDao;

	public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("SUCCESS:笑死");
		TextMessage replyMessage = new TextMessage("笑死");
//		reply(replyMessage, event.getReplyToken());
		return replyMessage;
	}

	public Message handlePictureMessageEvent(MessageEvent<TextMessageContent> event) throws URISyntaxException {
		logger.info("SUCCESS:我瘋子");
		URI originalContentUrl = new URI(
				"https://media.nownews.com/nn_media/thumbnail/2019/10/1570089924-27a9b9c9d7facd3422fe4610dd8ebe42-696x386.png");
		URI previewimageUrl = new URI(
				"https://media.nownews.com/nn_media/thumbnail/2019/10/1570089924-27a9b9c9d7facd3422fe4610dd8ebe42-696x386.png");
		Message replyMessage = new ImageMessage(originalContentUrl, previewimageUrl);
//		reply(replyMessage, event.getReplyToken());
		return replyMessage;
	}

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

	public FlexMessage handleNearLocationTemplate(MessageEvent<TextMessageContent> event, String location)
			throws Exception {
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

	// 新增飲料
	public Message addDrink(String userId, String userName, String originalMessageText) {
		logger.info("進入SERVICCE method: addDrink");
		String[] str = originalMessageText.substring(1).split(" ");
		// 檢核輸入格式
		if (str.length != 5) {
			logger.info("======新增飲料:空格位置錯誤=========");
			TextMessage replyMessage = new TextMessage("@" + userId + "，注意空格位置,請輸入『+飲料 甜度 冰塊 大小 金額』");
			return replyMessage;
		}
		String drink = str[0];
		String sugar = str[1];
		String ice = str[2];
		String size = str[3];
		String pricestr = str[4];
		int price = Integer.parseInt(pricestr);
		logger.info("新增飲料：" + drink + ",甜度：" + sugar + ",冰塊：" + ice + ",大小：" + size + ",價錢：" + pricestr);
		// 檢核輸入內容格式
		if (sugar.contains("冰") || sugar.contains("溫") || sugar.contains("熱") || ice.contains("糖")
				|| ice.contains("甜")) {
			TextMessage replyMessage = new TextMessage("@" + userId + "，請依排列順序輸入『+飲料 甜度 冰塊 大小 金額』");
			return replyMessage;
		}
		Detail detail = new Detail();
		detail.setDrink(drink);
		detail.setSugar(sugar);
		detail.setIce(ice);
		detail.setSize(size);
		detail.setPrice(price);
		detail.setUserName(userName);
		detail.setInputdate(new Date());
		detail.setStatus("0");
		logger.info("========開始新增飲料=======");
		Detail returnDetail = insertDetail(detail);
		logger.info("========回傳新增成功訊息=======");
		TextMessage replyMessage = new TextMessage("@" + userId + "新增成功，訂單編號：" + returnDetail.getId());
		return replyMessage;
	}

	// 存入DB
	public Detail insertDetail(@RequestBody Detail detail) {
		logger.info("=====新增資料 JPA======");
		return detailDao.save(detail);
	}

	// 修改飲料
	public Message updateDrink(String userId, String userName, String originalMessageText) {
		logger.info("進入SERVICCE method: updateDrink");
		String[] str = originalMessageText.substring(1).split(" ");
		// 檢核輸入格式
		if (str.length != 6) {
			logger.info("======新增飲料:空格位置錯誤=========");
			TextMessage replyMessage = new TextMessage("@" + userId + "，注意空格位置,請輸入『%飲料 甜度 冰塊 大小 金額 訂單編號』");
			return replyMessage;
		}
		String drink = str[0];
		String sugar = str[1];
		String ice = str[2];
		String size = str[3];
		String pricestr = str[4];
		String idstr = str[5];
		int price = Integer.parseInt(pricestr);
		Long id = Long.parseLong(idstr);
		logger.info(
				"修改飲料：" + drink + ",甜度：" + sugar + ",冰塊：" + ice + ",大小：" + size + ",價錢：" + pricestr + ",訂單編號：" + idstr);
		// 檢核輸入內容格式
		if (sugar.contains("冰") || sugar.contains("溫") || sugar.contains("熱") || ice.contains("糖")
				|| ice.contains("甜")) {
			TextMessage replyMessage = new TextMessage("@" + userId + "，請依排列順序輸入『+飲料 甜度 冰塊 大小 金額』");
			return replyMessage;
		}
		Detail detail = new Detail();
		detail.setDrink(drink);
		detail.setSugar(sugar);
		detail.setIce(ice);
		detail.setSize(size);
		detail.setPrice(price);
		detail.setUpdate(new Date());
		detail.setUpdateName(userName);
		detail.setStatus("0");
		detail.setId(id);
		logger.info("========開始修改飲料=======");
		Detail returnDetail = updateDetail(detail);
		logger.info("========回傳修改訊息=======");
		String returnStr = "";
		if (returnDetail != null) {
			logger.info("========修改成功=======");
			returnStr = "編號:" + returnDetail.getId() + "修改成功";
		} else {
			logger.info("========查無訂單編號=======");
			returnStr = "查無訂單編號:" + id;
		}
		TextMessage replyMessage = new TextMessage("@" + userId + "，" + returnStr);
		return replyMessage;
	}

	// 修改DB
	public Detail updateDetail(Detail detail) {
		Detail returnDetail = null;
		logger.info("=====查詢資料 JPA======");
		Optional<Detail> optionalDetail = detailDao.findById(detail.getId());
		if (optionalDetail.isPresent()) {
			logger.info("=====修改資料 JPA======");
			returnDetail = detailDao.save(detail);
		} else {
			logger.info("=====查無訂單編號======");
		}

		return returnDetail;
	}

}
