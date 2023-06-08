package test.com.linebot;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;

@LineMessageHandler
@Slf4j
public class LineBot3Talk {
	private static final Logger logger = LoggerFactory.getLogger(LineBot3Talk.class);
	@Autowired
    private LineMessagingClient lineMessagingClient;
	
	@EventMapping
	public void handle(MessageEvent<TextMessageContent> event) {
		String originalMessageText = event.getMessage().getText();
		logger.info("Hello, Heroku log!");
		if (originalMessageText.equals("123")) {
			logger.info("座標");
			handleLocationMessageEvent(event);
		} else if(originalMessageText.equals("我誰")){
			try {
				logger.info("我要瘋子");
				handlePictureMessageEvent(event);
			} catch (URISyntaxException e) {
				logger.info("我瘋子失敗");
			}
		}else{
			logger.info("笑死");
			handleTextMessageEvent(event);
		}
	}

	public void handleLocationMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("SUCCESS:座標");
		// 收到文字訊息做回覆
		Message replyMessage = new LocationMessage("location", "〒160-0004 東京都新宿區四谷一丁目6番1號", 35.687574, 139.72922);
		reply(replyMessage, event.getReplyToken());
	}

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
	
	

	private void reply(Message replyMessage, String replyToken) {
//		LineMessagingClient lineMessagingClient = LineMessagingClient.builder(replyToken).build();
		ReplyMessage reply = new ReplyMessage(replyToken, replyMessage);
		lineMessagingClient.replyMessage(reply);
	}
	

	@EventMapping
	public Message handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) throws URISyntaxException {
		logger.info("event:"+event);
		// 收到圖片做回覆
		return new StickerMessage("11537"," 52002735");
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		// 就是加入聊天室, 離開聊天室, 還有一些有的沒的事件
		logger.info("event: " + event);
	}
	
	public static void main(String[] args) {
		TestParamsDto dto = new TestParamsDto();
		System.out.println("沒有設參數的物件:"+dto.printParam());
		System.out.println("沒有設參數的物件 plus():"+dto.plus());
		TestParamsDto dto2 = new TestParamsDto(1,2);
		System.out.println("有參數的物件：" + dto2.printParam());
		System.out.println("有參數的物件 plus()：" + dto2.plus());
	}

}
