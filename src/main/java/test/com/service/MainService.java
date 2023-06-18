package test.com.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;

import test.com.dao.MainDao;
import test.com.model.Main;

@Service
public class MainService {
	private static final Logger logger = LoggerFactory.getLogger(MainService.class);

	@Autowired
	private MainDao mainDao;

	public List<Main> checkorderDate(String shopName, String shopId) {
		List<Main> mainList = mainDao.findByShopIdInputdate(shopId, new Date());
		return mainList;

	}
	
	//結單
	public Main saveMain(Main main) {
		main.setOrderDate(new Date());
		Main returnMain = mainDao.save(main);
		return returnMain;

	}

	public Main findMainById(Long mainId) {
		Main main = new Main();
		Optional<Main> mainOption = mainDao.findById(mainId);
		if (mainOption.isPresent()) {
			main = mainOption.get();
		}
		return main;
	}

	public FlexMessage getMainTemplate(MessageEvent<TextMessageContent> event) throws Exception {
		logger.info("查詢main_oder：今日主檔");
		List<Main> mainList = mainDao.findByinputDate(new Date());
		if (mainList == null || mainList.size() < 1) {
			logger.info("查詢main_oder：查無主檔");
			FlexMessage flexMessage = FlexMessage.builder().altText("查無資料")
					.contents(Carousel.builder().contents(null).build()).build();
			return flexMessage;
		}

		List<FlexComponent> flexComponent = new ArrayList<>();
		List<Bubble> flexBubbles = new ArrayList<>();
		// 創建文字說明
		Text text = Text.builder().text("請選擇店家").weight(Text.TextWeight.BOLD).size(FlexFontSize.LG)
				.align(FlexAlign.CENTER).margin(FlexMarginSize.NONE).build();
		flexComponent.add(text);
		// 創建Bubble的内容
		for (int i = 0; i < mainList.size(); i++) {
			String shopName = mainList.get(i).getShopName();
			Long mainId = mainList.get(i).getOrderNo();
			logger.info("店名：" + shopName + "主檔編號：" + mainId);
			// 創建按钮動作
			Action action = new PostbackAction(mainId + "-" + shopName, "END_MAIN|" + mainId);

			// 創建按钮组件
			Button button = Button.builder().action(action).build();
			flexComponent.add(button);
		}

		// 創建Bubble组件
		Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(flexComponent).build();
		Bubble bubble = Bubble.builder().body(body).build();
		flexBubbles.add(bubble);

		// 創建Flex訊息
		FlexMessage flexMessage = FlexMessage.builder().altText("Nearby Drink Shops")
				.contents(Carousel.builder().contents(flexBubbles).build()).build();

		return flexMessage;
	}
	
	public FlexMessage findMainTemplate(MessageEvent<TextMessageContent> event) throws Exception {
		logger.info("查詢main_oder：今日的主檔");
		List<Main> mainList = mainDao.findByinputDate(new Date());
		if (mainList == null || mainList.size() < 1) {
			logger.info("查詢main_oder：查無主檔");
			FlexMessage flexMessage = FlexMessage.builder().altText("查無資料")
					.contents(Carousel.builder().contents(null).build()).build();
			return flexMessage;
		}

		List<FlexComponent> flexComponent = new ArrayList<>();
		List<Bubble> flexBubbles = new ArrayList<>();
		// 創建文字說明
		Text text = Text.builder().text("請選擇店家").weight(Text.TextWeight.BOLD).size(FlexFontSize.LG)
				.align(FlexAlign.CENTER).margin(FlexMarginSize.NONE).build();
		flexComponent.add(text);
		// 創建Bubble的内容
		for (int i = 0; i < mainList.size(); i++) {
			String shopName = mainList.get(i).getShopName();
			Long mainId = mainList.get(i).getOrderNo();
			logger.info("店名：" + shopName + "主檔編號：" + mainId);
			// 創建按钮動作
			Action action = new PostbackAction(mainId + "-" + shopName, "FIND_MAIN|" + mainId);

			// 創建按钮组件
			Button button = Button.builder().action(action).build();
			flexComponent.add(button);
		}

		// 創建Bubble组件
		Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(flexComponent).build();
		Bubble bubble = Bubble.builder().body(body).build();
		flexBubbles.add(bubble);

		// 創建Flex訊息
		FlexMessage flexMessage = FlexMessage.builder().altText("Nearby Drink Shops")
				.contents(Carousel.builder().contents(flexBubbles).build()).build();

		return flexMessage;
	}
}
