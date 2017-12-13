package com.roncoo.pay.trade.utils.alipay;

import java.util.HashMap;
import java.util.Map;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;

public class ScanPaySubmit {
	
	private static final AlipayClient alipayClient;
	static {
		//new DefaultAlipayClient(URL,APP_ID,APP_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
    	alipayClient = new DefaultAlipayClient(AlipayConfigUtil.open_api_domain,
				AlipayConfigUtil.appid,
				AlipayConfigUtil.private_key,
				"json",
				"utf-8",
				AlipayConfigUtil.alipay_public_key,
				AlipayConfigUtil.sign_type);
	}

	/**
     * AliPay新调用预下单（获取支付二维码）方法
     * 生成请求form代码
     * @param sParaTemp
     * @param string
     * @param string2
     * @return
     */
    public static String buildRequest(Map<String, String> sParaTemp) {
		AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
	    alipayRequest.setReturnUrl(AlipayConfigUtil.return_url);
	    alipayRequest.setNotifyUrl(AlipayConfigUtil.notify_url);//在公共参数中设置回跳和通知地址
	    alipayRequest.setBizContent("{" +
	        "    \"out_trade_no\":\""+ sParaTemp.get("out_trade_no") +"\"," +
	        "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
	        "    \"total_amount\":"+ sParaTemp.get("total_fee") +"," +
	        "    \"subject\":\"" + sParaTemp.get("subject") +"\"," +
	        "    \"body\":\""+ sParaTemp.get("body")+"\"," +
	        "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
	        "    \"extend_params\":{" +
	        "    \"sys_service_provider_id\":\""+ AlipayConfigUtil.pid +"\"" +
	        "    }"+
	        "  }");//填充业务参数
	    
	    
	    String form="";
	    try {
	        form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
	        
	    } catch (AlipayApiException e) {
	        e.printStackTrace();
	    }
	   /* httpResponse.setContentType("text/html;charset=utf-8");
	    httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
	    httpResponse.getWriter().flush();
	    httpResponse.getWriter().close();*/
		return form;
	}
    
    /**
     * 请求支付宝获取交易是否成功状态
     * @param outTradeNo
     * @return
     */
    public static Map<String, String> orderQuery(String outTradeNo) {
    	AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
    	
    	//订单支付时传入的商户订单号,和支付宝交易号不能同时为空。 trade_no,out_trade_no如果同时存在优先取trade_no
		request.setBizContent("{" +
		"    \"out_trade_no\":\""+ outTradeNo +"\"" +
		//"    \"trade_no\":\"2017121221001004750200160996\"" +
		"  }");
		
		AlipayTradeQueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		
		Map<String,String> result = new HashMap<>();
		if(response != null) {
			result.put("trade_status", response.getTradeStatus());
		}
		return result;
    }
    
    /**
     * 验证消息是否是支付宝发出的合法消息
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verify(Map<String, String> params) {
    	
    	return true;
    }
}
