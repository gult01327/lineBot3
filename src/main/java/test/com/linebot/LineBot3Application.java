package test.com.linebot;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import test.com.model.Detail;
import test.com.model.Main;
import test.com.model.Shop;
import test.com.service.DetailService;
import test.com.service.MainService;
import test.com.service.ShopService;

@SpringBootApplication
@ComponentScan("test.com.service ")
@EnableJpaRepositories("test.com.dao")
@EntityScan("test.com.model")
@RestController
@RequestMapping("/lineBot")
@LineMessageHandler
public class LineBot3Application {
	private static final Logger logger = LoggerFactory.getLogger(LineBot3Application.class);

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private DetailService detailService;

	@Autowired
	private ShopService shopService;

	@Autowired
	private MainService mainService;

	public static void main(String[] args) {
		SpringApplication.run(LineBot3Application.class, args);
	}

	@EventMapping
	public void handle(MessageEvent<TextMessageContent> event) throws Exception {
		// 收到文字訊息
		String originalMessageText = event.getMessage().getText();
		// 取得使用者資訊
		String userId = event.getSource().getUserId();
		UserProfileResponse userProfile = lineMessagingClient.getProfile(userId).get();
		String userName = userProfile.getDisplayName();
		logger.info("userId:" + userId + ",userName: " + userName + ",收到文字訊息:" + originalMessageText);
		if (originalMessageText.substring(0, 1).equals("+") && originalMessageText.length() > 1) {
			// 新增範例：+飲料 甜度 冰塊 大小 金額
			logger.info("========新增飲料=========");
			Detail returnDetail = detailService.addDrink(userId, userName, originalMessageText);
			logger.info("取得回傳status：" + returnDetail.getStatus());
			if (returnDetail.getStatus().equals("1")) {
				logger.info("detail_order明細新增成功");
			} else {
				logger.info("輸入資料有誤");
				TextMessage replyMessage = new TextMessage(returnDetail.getStatus());
				reply(replyMessage, event.getReplyToken());
			}
			// 選擇店家
			logger.info("========選擇店家=========");
			FlexMessage flexMessage = shopService.findShopTemplate(event, returnDetail.getId());
			if (flexMessage.getAltText().equals("查無資料")) {
				logger.info("尚未存入店家資料");
				TextMessage replyMessage = new TextMessage("尚未存入店家資料，請輸入?地址或分享位置資訊");
				logger.info("========刪除存入的detail_order=========");
				detailService.removeDetail(returnDetail.getId());
				reply(replyMessage, event.getReplyToken());
			} else {
				// 回傳Template Message
				replyTemplet(flexMessage, userId);
			}
		} else if (originalMessageText.substring(0, 1).equals("%") && originalMessageText.length() > 1) {
			// 修改範例：%飲料 甜度 冰塊 大小 金額 訂單編號(line bot新增後回傳)
			logger.info("========修改飲料=========");
			Message replyMessage = detailService.updateDrink(userId, userName, originalMessageText);
			logger.info("取得回傳字串：" + replyMessage);
			reply(replyMessage, event.getReplyToken());
		} else if (originalMessageText.substring(0, 1).equals("-") && originalMessageText.length() > 1) {
			// 刪除範例：-訂單編號(line bot新增後回傳)
			logger.info("========刪除飲料=========");
			Message replyMessage = detailService.removeDrink(userId, userName, originalMessageText);
			logger.info("取得回傳字串：" + replyMessage);
			reply(replyMessage, event.getReplyToken());
		} else if (originalMessageText.substring(0, 1).equals("?") && originalMessageText.length() > 1) {
			// 地址查詢：以？開頭並輸入地址
			String address = originalMessageText.substring(1);
			logger.info("輸入地址:" + address);
			// 取得地址緯度、經度
			String location = shopService.getGoogleMapLocation(address);
			logger.info("取得地址緯度、經度:" + location);
			if (!location.equals("X")) {
				try {
					FlexMessage flexMessage = shopService.handleNearLocationTemplate(event, location, userName);
					replyTemplet(flexMessage, userId);
				} catch (Exception e) {
					logger.info("取得附近店家失敗");
					e.printStackTrace();
					TextMessage replyMessage = new TextMessage("取得附近店家失敗");
					reply(replyMessage, event.getReplyToken());
				}
			} else {
				logger.info("查無附近店家地址");
				TextMessage replyMessage = new TextMessage("查無附近店家地址");
				reply(replyMessage, event.getReplyToken());
			}
		} else if (originalMessageText.equals("我誰")) {
			Message replyMessage = detailService.handlePictureMessageEvent(event);
			logger.info("回傳Ｍessage:" + replyMessage);
			reply(replyMessage, event.getReplyToken());
			logger.info("======回傳圖片成功=======");
		} else if (originalMessageText.equals("訂單查詢")) {
			// 跳出主檔
			logger.info("======訂單查詢：main_order=======");
			FlexMessage flexMessage = mainService.findMainTemplate(event);
			if (flexMessage.getAltText().equals("查無資料")) {
				logger.info("尚未存入主檔資料");
				TextMessage replyMessage = new TextMessage("尚未有訂單，請先點單");
				reply(replyMessage, event.getReplyToken());
			} else {
				// 創建 Template Message
				replyTemplet(flexMessage, userId);
			}
		} else if (originalMessageText.equals("訂單結單")) {
			logger.info("======訂單結單:查詢明細檔=======");
			// 檢核今日是否已有明細檔
			String checkDetail = detailService.checkDetailOrder();
			if (checkDetail.equals("無明細")) {
				TextMessage replyMessage = new TextMessage("尚未有訂單，請先點單");
				reply(replyMessage, event.getReplyToken());
			} else {
				logger.info("======訂單結單:查詢主檔=======");
				// 跳出今日資料庫存入的店家
				FlexMessage flexMessage = mainService.getMainTemplate(event);
				if (flexMessage.getAltText().equals("查無資料")) {
					logger.info("尚未存入主檔資料");
					TextMessage replyMessage = new TextMessage("尚未有訂單，請先點單");
					reply(replyMessage, event.getReplyToken());
				} else {
					// 創建 Template Message
					replyTemplet(flexMessage, userId);
				}
			}
		}
	}

	// 回傳單筆訊息
	private void reply(Message replyMessage, String replyToken) {
//		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, replyMessage);
		lineMessagingClient.replyMessage(reply).join();
	}

	// 回傳多筆訊息
	private void replyList(List<Message> messages, String replyToken) {
		ReplyMessage reply = new ReplyMessage(replyToken, messages);
		lineMessagingClient.replyMessage(reply).join();
	}

	// 回傳模板訊息
	private void replyTemplet(Message flexMessage, String userId) {
		// 創建 PushMessage 並發送消息给 Line Bot
		PushMessage pushMessage = new PushMessage(userId, flexMessage);
		lineMessagingClient.pushMessage(pushMessage).join();
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

	@EventMapping
	public void handleLocationMessage(MessageEvent<LocationMessageContent> event) throws Exception {
		// 收到位置
		logger.info("收到位置訊息,method: handleLocationMessage");
		String userId = event.getSource().getUserId();
		LocationMessageContent locationMessage = event.getMessage();
		double latitude = locationMessage.getLatitude(); // 取得緯度
		double longitude = locationMessage.getLongitude(); // 取得經度
		logger.info("取得經度" + latitude + ",取得緯度: " + longitude);
		// 取user資訊
		UserProfileResponse userProfile = lineMessagingClient.getProfile(userId).get();
		String userName = userProfile.getDisplayName();
		String location = Double.toString(latitude) + "," + Double.toString(longitude);
		try {
			FlexMessage flexMessage = shopService.NearLocationTemplate(event, location, userName);
			replyTemplet(flexMessage, userId);
		} catch (Exception e) {
			logger.info("取得附近店家失敗");
			e.printStackTrace();
			TextMessage replyMessage = new TextMessage("取得附近店家失敗");
			reply(replyMessage, event.getReplyToken());
		}
	}

	// 接收回傳值的方法
	@EventMapping
	public Message handlePostbackEvent(PostbackEvent event) throws Exception, ExecutionException {
		String userId = event.getSource().getUserId();
		UserProfileResponse userProfile = lineMessagingClient.getProfile(userId).get();
		String userName = userProfile.getDisplayName();
		String data = event.getPostbackContent().getData();
		logger.info("按鈕回傳取得data：" + data);
		String[] parts = data.split("\\|");
		String flag = parts[0];
		if (flag.equals("SAVE_MAIN")) {
			String shopName = parts[1];
			String shopId = parts[2];
			String detailIdstr = parts[3];
			long detailId = Long.parseLong(detailIdstr);
			logger.info("=====檢核main_order是否已有單=====");
			List<Main> mainList = mainService.checkorderDate(shopName, shopId);
			if (mainList != null && mainList.size() > 0) {
				logger.info("=====main_order已有單，未結=====");
				Main main = mainList.get(0);
				logger.info("=====detail_order修改明細檔order_no=====");
				// 更新Detail_ordere欄位order_no
				Detail returnDetail = detailService.updateOrderNo(main.getOrderNo(), detailId);
				if (returnDetail != null) {
					return new TextMessage(userName + ",訂單編號：" + detailId + ",儲存成功");
				} else {
					return new TextMessage(userName + ",訂單編號：" + detailId + ",儲存失敗");
				}
			} else {
				logger.info("=====main_order尚未有單/已有結單的單，新增主檔=====");
				// 尚未有訂單，新增main_order
				Main newMain = null;
				newMain.setShopName(shopName);
				newMain.setShopId(shopId);
				newMain.setInputDate(new Date());
				Main returnMain = mainService.saveMain(newMain);
				if (returnMain.getOrderNo() != null) {
					logger.info("=====main_order新增主檔成功=====");
					logger.info("=====detail_order修改明細檔=====");
					Detail returnDetail = detailService.updateOrderNo(returnMain.getOrderNo(), detailId);
					return new TextMessage(userName + ",訂單編號：" + detailId + ",儲存成功");
				} else {
					logger.info("=====main_order新增主檔失敗=====");
					logger.info("=====detail_order刪除明細檔=====");
					detailService.removeDetail(detailId);
					return new TextMessage(userName + ",訂單新增失敗");
				}

			}
		} else if (flag.equals("END_MAIN")) {
			logger.info("=====結單回傳=====");
			String mainIdstr = parts[1];
			long mainlId = Long.parseLong(mainIdstr);
			logger.info("=====結單回傳：查詢主檔main_order=====");
			Main main = mainService.findMainById(mainlId);
			if (main.getOrderDate() != null) {
				logger.info("=====結單回傳：回傳主檔已結單=====");
				// 查出明細檔order_no
				logger.info("======訂單查詢：detail_order=======");
				String order = detailService.checkOrder(mainlId);
				if (order.equals("")) {
					logger.info("此訂單主檔：" + mainlId + ",查無明細檔");
				}
				String shopName = main.getShopName();
				order = "<" + shopName + ">" + order;
				logger.info("回傳明細:" + order);
				return new TextMessage("訂單已結單" + "\n" + order);
			} else {
				logger.info("=====結單回傳：回傳主檔未結單=====");
				logger.info("=====結單回傳：修改主檔main_order=====");
				Main returnMain = mainService.saveMain(main);
				return new TextMessage(userName + ",結單成功");
			}
		} else if (flag.equals("FIND_MAIN")) {
			logger.info("=====訂單查詢回傳=====");
			String mainIdstr = parts[1];
			long mainlId = Long.parseLong(mainIdstr);
			logger.info("=====訂單查詢回傳：查詢主檔main_order=====");
			Main main = mainService.findMainById(mainlId);
			// 查出明細檔order_no
			logger.info("======訂單查詢：detail_order=======");
			String order = detailService.checkOrder(mainlId);
			if (order.equals("")) {
				logger.info("此訂單主檔：" + mainlId + ",查無明細檔");
			}
			String shopName = main.getShopName();
			order = "<" + shopName + ">" + order;
			logger.info("回傳明細:" + order);
			if (main.getOrderDate() != null) {
				logger.info("=====訂單查詢回傳：回傳主檔已結單=====");
				return new TextMessage("訂單已結單" + "\n" + order);
			} else {
				logger.info("=====訂單查詢回傳：回傳主檔尚未結單=====");
				return new TextMessage("訂單尚未結單" + "\n" + order);
			}
		}
		logger.info("訂單失敗");
		return new TextMessage("訂單失敗");
	}
}
