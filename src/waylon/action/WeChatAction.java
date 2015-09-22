package waylon.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
import waylon.domain.WeChatUserInfo;
import waylon.wechat.Article;
import waylon.wechat.HttpRequestUtil;
import waylon.wechat.MessageUtil;
import waylon.wechat.NewsMessage;
import waylon.wechat.SignUtil;
import waylon.wechat.TextMessage;

public class WeChatAction extends ActionSupport {

	/**
	 * wxy
	 */
	private static final long serialVersionUID = 1L;
	private String signature;
	private String timestamp;
	private String nonce;
	private String echostr;
	private static final String appid = "wx55ccb70d4c7330aa";
	private static final String secret = "781e0684cedd7aafb609adb7d6d6e1d0";


	public String dealWeChat() throws Exception{
		HttpServletResponse response = ServletActionContext.getResponse();
		HttpServletRequest request = ServletActionContext.getRequest();
		String respContent = null;  
		String respXml = null;  
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		if (SignUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}

		//调用parseXml方法解析请求消息
		Map<String, String> requestMap = MessageUtil.parseXml(request);
		String fromUserName = requestMap.get("FromUserName");  
		String toUserName = requestMap.get("ToUserName");  
		String msgType = requestMap.get("MsgType");  
		String content = requestMap.get("Content");  
		String mobile = "";
		String userName ="";
		// 回复文本消息  
		TextMessage textMessage = new TextMessage();  
		textMessage.setToUserName(fromUserName);  
		textMessage.setFromUserName(toUserName);  
		textMessage.setCreateTime(new Date().getTime());  
		textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);  
		// 文本消息  
		if (MessageUtil.RESP_MESSAGE_TYPE_TEXT.equals(msgType)) { 
			//处理用户输入信息
			String[] userinfo = content.split("#");
			if(userinfo!=null &&userinfo.length>1){
				mobile = userinfo[0].trim();
				userName = userinfo[1].trim();
			}
			if(isMobile(mobile) && !userName.isEmpty()){
				String access_token_url = "https://api.weixin.qq.com/cgi-bin/token";
				String user_info_url ="https://api.weixin.qq.com/cgi-bin/user/info";
				//获取access_token
				String access_token_info = HttpRequestUtil.sendGet(access_token_url,"grant_type=client_credential&appid="+appid+"&secret="+secret);
				JSONObject jsonObject = new JSONObject(access_token_info);
				String access_token = jsonObject.getString("access_token");

				//获取用户信息（昵称）
				String user_info = HttpRequestUtil.sendGet(user_info_url, "access_token="+access_token+"&openid="+fromUserName+"&lang=zh_CN");
				JSONObject user_info_json = new JSONObject(user_info);
				String nickname = "";
				if(user_info_json.has("nickname")){
					nickname = user_info_json.getString("nickname");
					WeChatUserInfo weChatUserInfo = new WeChatUserInfo();
					weChatUserInfo.setOpenId(fromUserName);
					weChatUserInfo.setNickname(nickname);
					weChatUserInfo.setMobile(content);
					weChatUserInfo.setUserName(userName);
				}
				//调用接口存储数据，并判断用户是否已经参与过，如果参与过，提示用户等待开奖
				//TODO 调用service

				//如果没有参与过
				respContent = userName+",您已成功参与抽奖活动,谢谢参与！";  
			}else{
				respContent = "您输入的内容为："+content+" 请检查格式是否符合15966666666#张三";  
			}

			// 设置文本消息的内容  
			textMessage.setContent(respContent);  
			// 将文本消息对象转换成xml  
			respXml = MessageUtil.textMessageToXml(textMessage);  
			// 响应消息  
			out.print(respXml); 
		}  
		if(msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)){
			String event = requestMap.get("Event"); 
			String eventKey = requestMap.get("EventKey"); 
			if(MessageUtil.EVENT_TYPE_CLICK.equals(event)){
				NewsMessage newsMessage = new NewsMessage();  
				newsMessage.setToUserName(fromUserName);  
				newsMessage.setFromUserName(toUserName);  
				newsMessage.setCreateTime(new Date().getTime());  
				newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS); 
				if("apply_meterial".equals(eventKey)){
					List<Article> articleList = new ArrayList<Article>();  
					Article article = new Article();  
					article.setTitle("申请材料");  
					article.setDescription("轻松上传申请材料，一分钟预审，贷款快乐实现");  
					String requestUrl = request.getRequestURL().toString();
					int indexOf = requestUrl.indexOf("dealWeChat");
					String imgUrl = requestUrl.substring(0, indexOf)+"image/apply_meterial.jpg";
					article.setPicUrl(imgUrl);  
					article.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=200407327&idx=1&sn=f07fd55cee860b0e235522cbefff6476&scene=18#rd");  
					articleList.add(article);  
					newsMessage.setArticleCount(articleList.size());  
					newsMessage.setArticles(articleList);  
					String respMessage = MessageUtil.newsMessageToXml(newsMessage);  
					out.print(respMessage); 

				}else if("common_question".equals(eventKey)){
					List<Article> articleList = new ArrayList<Article>();  
					Article article = new Article();  
					article.setTitle("常见问题");  
					article.setDescription("如果您有任何疑问、建议或意见，欢迎微信留言或咨询公司电话：021-58780023。");  
					String requestUrl = request.getRequestURL().toString();
					int indexOf = requestUrl.indexOf("dealWeChat");
					String imgUrl = requestUrl.substring(0, indexOf)+"image/common_question.jpg";
					article.setPicUrl(imgUrl);  
					article.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=205108839&idx=1&sn=e613af5f0ca7c052748dfb954b9977e5&scene=18#rd");  
					articleList.add(article);  
					newsMessage.setArticleCount(articleList.size());  
					newsMessage.setArticles(articleList);  
					String respMessage = MessageUtil.newsMessageToXml(newsMessage);  
					out.print(respMessage); 
				}else if("company_brief".equals(eventKey)){
					List<Article> articleList = new ArrayList<Article>();  
					Article article = new Article();  
					article.setTitle("公司简介");  
					article.setDescription("上海乐融金融信息服务有限公司是位于上海陆家嘴的互联网金融公司.公司已经获得世界著名的风险投资基金的资金支持。");  
					String requestUrl = request.getRequestURL().toString();
					int indexOf = requestUrl.indexOf("dealWeChat");
					String imgUrl = requestUrl.substring(0, indexOf)+"image/company_brief.jpg";
					article.setPicUrl(imgUrl);  
					article.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=200405794&idx=1&sn=d5a768b009bf230e445c9ad9fe0bfd59&scene=18#rd");  
					articleList.add(article);  
					newsMessage.setArticleCount(articleList.size());  
					newsMessage.setArticles(articleList);  
					String respMessage = MessageUtil.newsMessageToXml(newsMessage);  
					out.print(respMessage);  
				}
				else if("contact_us".equals(eventKey)){
					List<Article> articleList = new ArrayList<Article>();  
					Article article = new Article();  
					article.setTitle("联系我们");  
					article.setDescription("如果您有任何疑问，欢迎微信留言或直接致电021-58780023。");  
					String requestUrl = request.getRequestURL().toString();
					int indexOf = requestUrl.indexOf("dealWeChat");
					String imgUrl = requestUrl.substring(0, indexOf)+"image/contact_us.jpg";
					article.setPicUrl(imgUrl);  
					article.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=200407862&idx=1&sn=0de116a39cdeee4e2c2b940d4621e33a&scene=18#rd");  
					articleList.add(article);  
					newsMessage.setArticleCount(articleList.size());  
					newsMessage.setArticles(articleList);  
					String respMessage = MessageUtil.newsMessageToXml(newsMessage);  
					out.print(respMessage); 
				}
				else if("happy_enjoy".equals(eventKey)){
					List<Article> articleList = new ArrayList<Article>();  
					Article article1 = new Article(); 
					//article1.setDescription("最高放贷500000 零担保无抵押 贷款好轻松");  
					String requestUrl = request.getRequestURL().toString();
					int indexOf = requestUrl.indexOf("dealWeChat");
					String imgUrl = requestUrl.substring(0, indexOf)+"image/happy_enjoy.jpg";
					article1.setPicUrl(imgUrl);  
					article1.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=206690162&idx=1&sn=3bafd54413781155e807b9a42d3d8b90&scene=18#rd");  

					Article article2 = new Article();  
					article2.setTitle("让99%的人看懂自己的征信报告！");  
					String requestUrl2 = request.getRequestURL().toString();
					int indexOf2 = requestUrl2.indexOf("dealWeChat");
					String imgUrl2 = requestUrl2.substring(0, indexOf2)+"image/1.jpg";
					article2.setPicUrl(imgUrl2);  
					article2.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=206690162&idx=2&sn=e6d28846314ce86e3a276675467d26cc&scene=18#rd"); 

					Article article3 = new Article();  
					article3.setTitle("腾讯80页重磅报告：哪些行业将被颠覆!");  
					String requestUrl3 = request.getRequestURL().toString();
					int indexOf3 = requestUrl3.indexOf("dealWeChat");
					String imgUrl3 = requestUrl3.substring(0, indexOf3)+"image/2.jpg";
					article3.setPicUrl(imgUrl3);  
					article3.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=206690162&idx=3&sn=45fbb33a40cfb579b6628e533df16bc9&scene=18#rd"); 

					Article article4 = new Article();  
					article4.setTitle("有的人25岁就死了，75岁才埋葬");  
					String requestUrl4 = request.getRequestURL().toString();
					int indexOf4 = requestUrl4.indexOf("dealWeChat");
					String imgUrl4 = requestUrl4.substring(0, indexOf4)+"image/3.jpg";
					article4.setPicUrl(imgUrl4);  
					article4.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=206690162&idx=4&sn=b35f7071340c0c8fc80efbee050fffb3&scene=18#rd"); 
					
					Article article5 = new Article();  
					article5.setTitle("8分钟震撼全球：我们身处的谎言世界！");  
					String requestUrl5 = request.getRequestURL().toString();
					int indexOf5 = requestUrl5.indexOf("dealWeChat");
					String imgUrl5 = requestUrl5.substring(0, indexOf5)+"image/4.jpg";
					article5.setPicUrl(imgUrl5);  
					article5.setUrl("http://mp.weixin.qq.com/s?__biz=MzAxNTAwNTg0Nw==&mid=206690162&idx=5&sn=edd14f6d2b599e5d8ced33887cfd8bcd&scene=18#rd"); 
					articleList.add(article1);  
					articleList.add(article2);
					articleList.add(article3);  
					articleList.add(article4);  
					articleList.add(article5);  
					
					newsMessage.setArticleCount(articleList.size());  
					newsMessage.setArticles(articleList);  
					String respMessage = MessageUtil.newsMessageToXml(newsMessage);  
					out.print(respMessage); 
				}
			}
		}

		out.close();  
		return SUCCESS;
	}

	public  boolean isMobile(String str) {   
		Pattern p = null;  
		Matcher m = null;  
		boolean b = false;   
		p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号  
		m = p.matcher(str);  
		b = m.matches();   
		return b;  
	}  

	public String getSignature() {
		return signature;
	}


	public void setSignature(String signature) {
		this.signature = signature;
	}


	public String getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}


	public String getNonce() {
		return nonce;
	}


	public void setNonce(String nonce) {
		this.nonce = nonce;
	}


	public String getEchostr() {
		return echostr;
	}


	public void setEchostr(String echostr) {
		this.echostr = echostr;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}


