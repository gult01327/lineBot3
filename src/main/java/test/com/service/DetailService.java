package test.com.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import com.linecorp.bot.model.event.message.LocationMessageContent;
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

	// 回傳圖片練習
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

	// 新增飲料
	public Detail addDrink(String userId, String userName, String originalMessageText) {
		logger.info("進入SERVICCE method: addDrink");
		Detail returnDetail = null;
		String[] str = originalMessageText.substring(1).split(" ");
		// 檢核輸入格式
		if (str.length != 5) {
			logger.info("======新增飲料:空格位置錯誤=========");
			returnDetail.setStatus(userName + "，注意空格位置,請輸入『+飲料 甜度 冰塊 大小 金額』");
			return returnDetail;
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
			returnDetail.setStatus(userName + "，請依排列順序輸入『+飲料 甜度 冰塊 大小 金額』");
			return returnDetail;
		}
		Detail detail = new Detail();
		detail.setDrink(drink);
		detail.setSugar(sugar);
		detail.setIce(ice);
		detail.setSize(size);
		detail.setPrice(price);
		detail.setUserName(userName);
		detail.setInputdate(new Date());
		detail.setStatus("1"); // 0：無效，1-有效
		logger.info("========開始新增飲料=======");
		returnDetail = insertDetail(detail);
		return returnDetail;
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
			TextMessage replyMessage = new TextMessage(userName + "，注意空格位置,請輸入『%飲料 甜度 冰塊 大小 金額 訂單編號』");
			return replyMessage;
		}
		String drink = str[0];
		String sugar = str[1];
		String ice = str[2];
		String size = str[3];
		String pricestr = str[4];
		String idstr = str[5];
		String number = "123456789";
		int price;
		Long id;
		if (!number.contains(idstr) || !number.contains(pricestr)) {
			TextMessage replyMessage = new TextMessage(userName + "金額、編號需為數字，請依排列順序輸入『%飲料 甜度 冰塊 大小 金額 訂單編號』");
			return replyMessage;
		} else {
			price = Integer.parseInt(pricestr);
			id = Long.parseLong(idstr);
		}

		logger.info(
				"修改飲料：" + drink + ",甜度：" + sugar + ",冰塊：" + ice + ",大小：" + size + ",價錢：" + pricestr + ",訂單編號：" + idstr);
		// 檢核輸入內容格式
		if (sugar.contains("冰") || sugar.contains("溫") || sugar.contains("熱") || ice.contains("糖")
				|| ice.contains("甜")) {
			TextMessage replyMessage = new TextMessage(userName + "，請依排列順序輸入『%飲料 甜度 冰塊 大小 金額 訂單編號』");
			return replyMessage;
		}
		// 修改欄位
		Detail detail = new Detail();
		detail.setDrink(drink);
		detail.setSugar(sugar);
		detail.setIce(ice);
		detail.setSize(size);
		detail.setPrice(price);
		detail.setUpdate(new Date());
		detail.setUpdateName(userName);
		detail.setStatus("1"); // 0：無效，1-有效
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
		TextMessage replyMessage = new TextMessage(userName + "，" + returnStr);
		return replyMessage;
	}

	// 修改DB
	public Detail updateDetail(Detail detail) {
		Detail returnDetail = null;
		logger.info("=====查詢需修改資料 JPA======");
		Optional<Detail> optionalDetail = detailDao.findById(detail.getId());
		if (optionalDetail.isPresent()) {
			logger.info("撈出舊資料");
			Detail oldDetail = optionalDetail.get();
			// 維持舊資料的欄位
			detail.setUserName(oldDetail.getUserName());
			detail.setInputdate(oldDetail.getInputdate());
			logger.info("=====修改資料 JPA======");
			returnDetail = detailDao.save(detail);
		} else {
			logger.info("=====查無訂單編號======");
		}

		return returnDetail;
	}

	// 刪除資料
	public Message removeDrink(String userId, String userName, String originalMessageText) {
		logger.info("進入SERVICCE method: removeDrink");
		String[] str = originalMessageText.substring(1).split(" ");
		// 檢核輸入格式
		if (str.length != 1) {
			logger.info("======刪除飲料:輸入錯誤=========");
			TextMessage replyMessage = new TextMessage(userName + "，注意空格位置,請輸入『-訂單編號』");
			return replyMessage;
		}
		String number = "123456789";
		String idstr = str[0];
		Long id;
		logger.info("刪除訂單編號：" + idstr);
		// 檢核輸入內容格式
		if (!number.contains(idstr)) {
			TextMessage replyMessage = new TextMessage(userName + "，訂單編號需為數字，請輸入『-訂單編號』");
			return replyMessage;
		} else {
			id = Long.parseLong(idstr);
		}
		logger.info("========開始刪除飲料=======");
		Detail returnDetail = removeDetail(id, userName);
		logger.info("========回傳修改訊息=======");
		String returnStr = "";
		if (returnDetail != null && returnDetail.getStatus().equals("0")) {
			logger.info("========刪除成功=======");
			returnStr = "編號:" + returnDetail.getId() + "刪除成功";
		} else if (returnDetail != null && returnDetail.getStatus().equals("1")) {
			logger.info("========不可刪除非當日訂單編號=======");
			returnStr = "訂單編號:" + id + "，非當日訂單不可刪除";
		} else {
			logger.info("========查無訂單編號=======");
			returnStr = "查無訂單編號:" + id;
		}
		TextMessage replyMessage = new TextMessage(userName + "，" + returnStr);
		return replyMessage;
	}

	// 刪除DB(只做狀態修改)
	public Detail removeDetail(Long id, String userName) {
		Detail returnDetail = null;
		logger.info("=====查詢需刪除資料 JPA======");
		Optional<Detail> optionalDetail = detailDao.findById(id);
		if (optionalDetail.isPresent()) {
			logger.info("撈出舊資料");
			Detail oldDetail = optionalDetail.get();
			// 檢核是否與輸入日相同
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			String inputDateStr = dateFormat.format(oldDetail.getInputdate());
			String upDateStr = dateFormat.format(new Date());
			logger.info("寫入日期:" + inputDateStr + ",修改日期:" + upDateStr);
			if (inputDateStr.compareTo(upDateStr) != 0) {
				return oldDetail; // 回傳原始資料
			}
			// 舊資料需修改的欄位
			oldDetail.setUpdate(new Date());
			oldDetail.setUpdateName(userName);
			oldDetail.setStatus("0"); // 0：無效，1-有效
			logger.info("=====修改資料 JPA======");
			returnDetail = detailDao.save(oldDetail);
		} else {
			logger.info("=====查無訂單編號======");
		}
		return returnDetail;
	}

	// 結單時查詢訂單
	public String checkOrder(Long orderNo) {
		logger.info("=====訂單查詢detail_order=====");
		String order = "";
		List<Detail> detailList = detailDao.findByOrderNo(orderNo);
		if (detailList != null && detailList.size() > 0) {
			for (int i = 0; i < detailList.size(); i++) {
				String userName = detailList.get(i).getUserName();
				String drink = detailList.get(i).getDrink();
				String sugar = detailList.get(i).getSugar();
				String ice = detailList.get(i).getIce();
				String size = detailList.get(i).getSize();
				int price = detailList.get(i).getPrice();
				order = order + "\n" + userName + "," + drink + " " + sugar + " " + ice + " " + size + " " + price;
			}
		}
		return order;
	}

	// 查詢訂單
	public String checkDetailOrder() {
		logger.info("=====訂單查詢detail_order=====");
		String order = "";
		Date today = new Date();
		List<Detail> detailList = detailDao.findByinputDate(today);
		if (detailList == null || detailList.size() < 1) {
			logger.info("=====查無訂單明細=====");
			order = "無明細";
			return order;
		}
		return order;
	}

	// 將oderno寫入
	public Detail updateOrderNo(Long oderNo, Long detailId) {
		Optional<Detail> detailOption = detailDao.findById(detailId);
		Detail returnDetail = null;
		if (detailOption.isPresent()) {
			Detail detail = detailOption.get();
			detail.setOrderNo(oderNo);
			returnDetail=detailDao.save(detail);
		}
		return returnDetail;

	}
	
	public void removeDetail(Long detailId) {
		detailDao.deleteById(detailId);
		logger.info("=====刪除訂單明細=====");
	}

}
