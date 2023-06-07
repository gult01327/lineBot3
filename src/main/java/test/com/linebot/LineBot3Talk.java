package test.com.linebot;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

@LineMessageHandler
public class LineBot3Talk {
	
	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		String originalMessageText = event.getMessage().getText();
		System.out.println("event: " + event);
		if(originalMessageText.contains("在哪")||originalMessageText.contains("住哪")) {
			handleLocationMessageEvent(event);
		}
		
		// 收到文字訊息做回覆
		return new TextMessage(originalMessageText);
	}
	
	@EventMapping
	public LocationMessage handleLocationMessageEvent(MessageEvent<TextMessageContent> event) {
		System.out.println("event: " + event);
		return new LocationMessage("location","Tokyo",35.65910807942215,139.70372892916203);

	}
	
	@EventMapping
	public Message handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) throws URISyntaxException {
		URI originalContentUrl = new URI("https://imgur.com/cUfzuej");
		URI previewimageUrl = new URI("https://imgur.com/cUfzuej");
		// 收到圖片做回覆
		return new ImageMessage(originalContentUrl,previewimageUrl);
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		// 就是加入聊天室, 離開聊天室, 還有一些有的沒的事件
		System.out.println("event: " + event);
	}

}
