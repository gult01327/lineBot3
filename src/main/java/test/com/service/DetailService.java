package test.com.service;

import java.net.URISyntaxException;

import org.springframework.stereotype.Service;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;

import test.com.model.Detail;

@Service
public interface DetailService {

	public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event);

	public Message handlePictureMessageEvent(MessageEvent<TextMessageContent> event) throws URISyntaxException;

	// 取得輸入地址的座標
	public String getGoogleMapLocation(String address);

	// 取得附近店家的模板
	public FlexMessage handleNearLocationTemplate(MessageEvent<TextMessageContent> event, String location)
			throws Exception;

	// 新增飲料:檢核資料
	public Message addDrink(String userId, String userName, String originalMessageText);
	// 新增飲料:寫入DB
	public Detail createDetail(Detail detail);

}
