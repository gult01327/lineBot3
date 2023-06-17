package test.com.linebot;

import java.net.URISyntaxException;
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
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import test.com.model.Shop;
import test.com.service.DetailService;
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
			Message replyMessage = detailService.addDrink(userId, userName, originalMessageText);
			logger.info("取得回傳字串：" + replyMessage);
			reply(replyMessage, event.getReplyToken());
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
			String order = detailService.checkOrder();
			logger.info("回傳字串:" + order);
			if (order == null || order.equals("")) {
				order = "今日尚未有訂單，請點單";
			}
			TextMessage replyMessage = new TextMessage(order);
			reply(replyMessage, event.getReplyToken());
		} else if (originalMessageText.equals("訂單結單")) {
			// 跳出今日資料庫存入的店家
			FlexMessage flexMessage = shopService.getShopTemplate(event);
			if (flexMessage.getAltText().equals("已結單")) {
				String orderShop = shopService.checkOrder();
				String orderDetail = detailService.checkOrder();
				logger.info("已結單回傳字串:" + orderShop + orderDetail);
				TextMessage replyMessage = new TextMessage(orderShop + orderDetail);
				reply(replyMessage, event.getReplyToken());
			} else if (flexMessage.getAltText().equals("查無資料")) {
				logger.info("尚未存入店家資料");
				TextMessage replyMessage = new TextMessage("尚未存入店家資料，請輸入?地址或分享位置資訊");
				reply(replyMessage, event.getReplyToken());
			} else {
				// 創建 Template Message
				replyTemplet(flexMessage, userId);
			}
		}
	}

	// 回傳單筆訊息
	private void reply(Message replyMessage, String replyToken) {
//		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, replyMessage);
		lineMessagingClient.replyMessage(reply);
	}

	// 回傳多筆訊息
	private void replyList(List<Message> messages, String replyToken) {
		ReplyMessage reply = new ReplyMessage(replyToken, messages);
		lineMessagingClient.replyMessage(reply);
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
	public Message handlePostbackEvent(PostbackEvent event) {
		String data = event.getPostbackContent().getData();
		logger.info("按鈕回傳取得data：" + data);
		String[] parts = data.split("\\|");
		if (parts.length == 2 && parts[0].equals("SAVE_SHOP")) {
			String id = parts[1];
			logger.info("點選店家編碼：" + id);
			Shop returnShop = shopService.saveShopStatus(id);
			if (returnShop != null) {
				logger.info("店家編碼：" + id + "狀態修改成功");
				return new TextMessage("訂單結單成功");
			}
		}
		logger.info("shop_order狀態修改失敗");
		return new TextMessage("訂單結單失敗");
	}
}
