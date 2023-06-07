package test.com.linebot;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;

@LineMessageHandler
@Slf4j
public class LineBot3Talk {
	private static final Logger logger = LoggerFactory.getLogger(LineBot3Talk.class);

	@Value("line.bot.channel-token")
	private String TOKEN;

	@EventMapping
	public void handle(MessageEvent<TextMessageContent> event) {
		String originalMessageText = event.getMessage().getText();
		logger.info("Hello, Heroku log!");
		if (originalMessageText.equals("123")) {
			logger.info("我要學你");
			handleTextMessageEvent(event);
		} else {
			logger.info("我要笑你");
			handleLocationMessageEvent(event);
		}
	}

//	@EventMapping
	public void handleLocationMessageEvent(MessageEvent<TextMessageContent> event) {
//		{
//			  "type": "location",
//			  "title": "my location",
//			  "address": "〒160-0004 東京都新宿区四谷一丁目6番1号",
//			  "latitude": 35.687574,
//			  "longitude": 139.72922
//			}
//		String originalMessageText = event.getMessage().getText();
		logger.info("我要學你2");
		// 收到文字訊息做回覆
		Message replyMessage = new LocationMessage("location", "〒160-0004 東京都新宿区四谷一丁目6番1号", 35.687574, 139.72922);
		reply(replyMessage, event.getReplyToken());
	}

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("event: " + event);
		logger.info("我要笑你2");
		TextMessage replyMessage = new TextMessage("笑死");
		reply(replyMessage, TOKEN);
	}

	private void reply(Message replyMessage, String replyToken) {
		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, replyMessage);
		lineMessagingClient.replyMessage(reply);
	}

	@EventMapping
	public Message handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) throws URISyntaxException {
		URI originalContentUrl = new URI("https://imgur.com/cUfzuej");
		URI previewimageUrl = new URI("https://imgur.com/cUfzuej");
		// 收到圖片做回覆
		return new ImageMessage(originalContentUrl, previewimageUrl);
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		// 就是加入聊天室, 離開聊天室, 還有一些有的沒的事件
		logger.info("event: " + event);
	}

}
