package waylon.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.opensymphony.xwork2.ActionSupport;

import net.sf.json.JSONObject;
import waylon.domain.TokenInfo;
import waylon.domain.UserInfo;
import waylon.service.TokenInfoService;
import waylon.service.UserInfoService;

public class RegisterAction extends ActionSupport{

	/**
	 * wxy
	 */
	private static final long serialVersionUID = -6292866143864611853L;

	private String userName;
	private String password;
	private String result;
	private Map resultMap;
	private String mobile;
	public Map getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map resultMap) {
		this.resultMap = resultMap;
	}

	private String FromUserName;
	private String code;
	private String state;

	private UserInfoService userInfoService;

	private TokenInfoService tokenInfoService;

	@Override
	public String execute() throws Exception {
		return super.execute();
	}

	public String register() throws Exception {

		try {
			UserInfo userInfo = userInfoService.getUserInfoByMobile(mobile);
			if(null == userInfo){
				UserInfo userInfoMO = new UserInfo();
				userInfoMO.setMobile(mobile);
				userInfoMO.setPassword(password);
				int requestCode = userInfoService.addUser(userInfoMO);
				String requestStr = String.valueOf(requestCode);
				if("1".equals(requestStr)){
					result = "1";//成功
				}else{
					result = "0";//失败
				}
			}else{
				result = "2";//已存在
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	public String findUserByMobile(){
		UserInfo userInfo = userInfoService.getUserInfoByMobile(mobile);
		if(userInfo ==null){
			result = "0";//不存在
		}else{
			result = "1";//存在
		}
		return SUCCESS;
	}

	public String loginValidate(){

		UserInfo userInfo = userInfoService.getUserInfoByMobile(mobile);
		if(userInfo ==null){
			result = "0";//不存在
		}else{
			if(password.equals(userInfo.getPassword())){
				TokenInfo tokenInfo = tokenInfoService.getTokenInfoByMobile(mobile);
				TokenInfo tokenInfoTemp = new TokenInfo();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
				Date date = new Date();
				String modifyDate = simpleDateFormat.format(date);
				if(tokenInfo!=null){
					tokenInfoTemp.setMobile(mobile);
					tokenInfoTemp.setModifyDate(modifyDate);
					tokenInfoTemp.setTokenValue(mobile+date.getTime());
					tokenInfoService.updateTokenByMobile(tokenInfoTemp);
				}else{
					tokenInfoTemp.setMobile(mobile);
					tokenInfoTemp.setCreateDate(modifyDate);
					tokenInfoTemp.setTokenValue(mobile+date.getTime());
					tokenInfoService.addToken(tokenInfoTemp);
				}
				Map<String, String> map = new HashMap<String, String>();
				map.put("result","1");
				map.put("token", tokenInfoTemp.getTokenValue());
				resultMap = map;
				
			}else{
				result = "3";
			}
		}
		return SUCCESS;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public UserInfoService getUserInfoService() {
		return userInfoService;
	}

	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getFromUserName() {
		return FromUserName;
	}

	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TokenInfoService getTokenInfoService() {
		return tokenInfoService;
	}

	public void setTokenInfoService(TokenInfoService tokenInfoService) {
		this.tokenInfoService = tokenInfoService;
	}

}
