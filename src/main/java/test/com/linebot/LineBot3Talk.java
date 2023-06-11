package test.com.linebot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineMessagingClientBuilder;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
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

@RestController
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
		}else if(originalMessageText.substring(0,1).equals("?")&& originalMessageText.length()>1){
			//地址查詢：以？開頭並輸入地址
			String place = originalMessageText.substring(1);
	        System.out.println("輸入地址:" + place);
	        
	        //取得地址緯度、經度
	        String location = getGoogleMapLocation(place);
	        System.out.println("取得地址緯度、經度:" + location);
	        
	        if(!location.equals("X")) {
		        //附近飲料店
		        String nearbyPlaces = getNearbyPlaces(location, "飲料店");
		        System.out.println("附近的飲料店: " + nearbyPlaces);
		        //傳送多筆座標
		        handleNearLocationMessageEvent(event,nearbyPlaces);
	        }else {
	        	TextMessage replyMessage = new TextMessage("取得地址失敗");
	    		reply(replyMessage, event.getReplyToken());
	        }
		}else{
			logger.info("笑死");
			handleTextMessageEvent(event);
		}
	}
	
	//回覆固定座標
	public void handleLocationMessageEvent(MessageEvent<TextMessageContent> event) {
		logger.info("SUCCESS:單筆固定座標");
		// 收到文字訊息做回覆
		Message replyMessage = new LocationMessage("location", "基隆市中山區中和路168巷7弄54號", 25.06752, 121.585664);
		reply(replyMessage, event.getReplyToken());
	}
	
	//回覆多筆座標
	public void handleNearLocationMessageEvent(MessageEvent<TextMessageContent> event,String nearbyPlaces) {
		logger.info("SUCCESS:多筆座標");
		if(nearbyPlaces.length()>1) {
			// 收到文字訊息做回覆
			String[] token = nearbyPlaces.split(";");
			for(int i = 0;i<token.length;i++) {
				String[] object =token[i].split(",");
				String name=(object[0]);
				double lat=(Double.parseDouble(object[1]));
				double lng=(Double.parseDouble(object[2]));
				Message replyMessage = new LocationMessage("location",name,lat,lng);
				reply(replyMessage, event.getReplyToken());
			}
		}else {
			TextMessage replyMessage = new TextMessage("查無附近飲料店");
    		reply(replyMessage, event.getReplyToken());
		}

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
	
	//獲取輸入地址的經緯度
    private static String getGoogleMapLocation(String address) {
    	//google map金鑰
    	String  GOOGLE_API_KEY = "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA";
        GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build();
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results.length > 0) {
                //取第一個结果的經緯度
                LatLng location = results[0].geometry.location;
                System.out.println("緯度: " + location.lat);
                System.out.println("經度: " + location.lng);
                
                return location.lat+","+location.lng;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "獲取位置信息發收錯誤";
        }
        System.out.println("獲取位置信息發收錯誤");
		return "X";
    }
    
    private void replyTextMessage(String replyToken, String message) {
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage(message)));
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
    
	//回覆多筆座標
	public static void mainNearLocationMessageEvent(String nearbyPlaces) {
		logger.info("SUCCESS:多筆座標");
		// 收到文字訊息做回覆
		String[] token = nearbyPlaces.split(";");
		for(int i = 0;i<token.length;i++) {
			String[] object =token[i].split(",");
			System.out.println("location"+","+object[0]+","+object[1]+","+object[2]);
		}
	}
    
    /**取得裝置當前位置(取到Server位置)
    private static String getCurrentLocation() {
    	try {
            // 獲取公網IP地址
            URL ipApiUrl = new URL("https://api.ipify.org?format=json");
            HttpURLConnection ipConnection = (HttpURLConnection) ipApiUrl.openConnection();
            ipConnection.setRequestMethod("GET");
            int ipResponseCode = ipConnection.getResponseCode();
            if (ipResponseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader ipReader = new BufferedReader(new InputStreamReader(ipConnection.getInputStream()));
                String ipResponse = ipReader.readLine();
                JsonParser ipParser = new JsonParser();
                JsonObject ipJson = ipParser.parse(ipResponse).getAsJsonObject();
                String ipAddress = ipJson.get("ip").getAsString();

                // 使用Geolocation API獲取設備當前位置信息
                URL locationApiUrl = new URL("https://www.googleapis.com/geolocation/v1/geolocate?key=" + "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA");
                HttpURLConnection locationConnection = (HttpURLConnection) locationApiUrl.openConnection();
                locationConnection.setRequestMethod("POST");
                locationConnection.setRequestProperty("Content-Type", "application/json");
                locationConnection.setDoOutput(true);
                String requestBody = "{\"considerIp\": \"true\",\"wifiAccessPoints\": []}";
                locationConnection.getOutputStream().write(requestBody.getBytes());

                int locationResponseCode = locationConnection.getResponseCode();
                if (locationResponseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader locationReader = new BufferedReader(new InputStreamReader(locationConnection.getInputStream()));
                    StringBuilder locationResponse = new StringBuilder();
                    String line;
                    while ((line = locationReader.readLine()) != null) {
                        locationResponse.append(line);
                    }
                    locationReader.close();
                    JsonParser locationParser = new JsonParser();
                    JsonObject locationJson = locationParser.parse(locationResponse.toString()).getAsJsonObject();
                    JsonObject location = locationJson.get("location").getAsJsonObject();
                    double latitude = location.get("lat").getAsDouble();
                    double longitude = location.get("lng").getAsDouble();

                    // 返回当前位置信息
                    return latitude + "," + longitude; //緯度,經度
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "無法獲取當前位置信息。";
    }
    */
    
  public static void main(String[] args) {
	  String place = "基隆市中山區中和路168巷7弄54號";
	  String location = getGoogleMapLocation(place);
	  System.out.println("當前位置: " + location);

	  String nearbyPlaces = getNearbyPlaces(location, "飲料店");
	  System.out.println("附近的飲料店: " + nearbyPlaces);
	  mainNearLocationMessageEvent(nearbyPlaces);
  
  	}

    //取得附近飲料店：店名（緯度，經度）
    private static String getNearbyPlaces(String location, String keyword) {
        try {
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");

            // 使用Places API获取附近的饮料店信息
            URL placesApiUrl = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + encodedLocation + "&radius=1000&keyword=" + encodedKeyword + "&key=" + "AIzaSyBGQRnDgWX0c4WJbUNiBxU6MbOvDFPD_QA");
            HttpURLConnection placesConnection = (HttpURLConnection) placesApiUrl.openConnection();
            placesConnection.setRequestMethod("GET");

            int placesResponseCode = placesConnection.getResponseCode();
            if (placesResponseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader placesReader = new BufferedReader(new InputStreamReader(placesConnection.getInputStream()));
                StringBuilder placesResponse = new StringBuilder();
                String line;
                while ((line = placesReader.readLine()) != null) {
                    placesResponse.append(line);
                }
                placesReader.close();

                // 解析JSON
                JsonParser placesParser = new JsonParser();
                JsonObject jsonResponse = placesParser.parse(placesResponse.toString()).getAsJsonObject();;
                JsonArray results = jsonResponse.getAsJsonArray("results");
                
                StringBuilder nearbyPlaces = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                	JsonObject place = results.get(i).getAsJsonObject();
                	JsonObject nearLocation = place.getAsJsonObject("geometry").getAsJsonObject("location");
                	
                	String name = place.get("name").getAsString();
                    double lat = nearLocation.get("lat").getAsDouble();
                    double lng = nearLocation.get("lng").getAsDouble();
                    
                    nearbyPlaces.append(name).append(",");
                    nearbyPlaces.append(lat).append(",");
                    nearbyPlaces.append(lng).append(";");
                }
                
                return nearbyPlaces.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "無法獲取附近的飲料店信息。";
    }
    
	/**
	public static void main(String[] args) {
		TestParamsDto dto = new TestParamsDto();
		System.out.println("沒有設參數的物件:"+dto.printParam());
		System.out.println("沒有設參數的物件 plus():"+dto.plus());
		TestParamsDto dto2 = new TestParamsDto(1,2);
		System.out.println("有參數的物件：" + dto2.printParam());
		System.out.println("有參數的物件 plus()：" + dto2.plus());
	}
	*/

}
