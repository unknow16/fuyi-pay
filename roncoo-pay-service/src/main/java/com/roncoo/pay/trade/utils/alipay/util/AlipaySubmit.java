package com.roncoo.pay.trade.utils.alipay.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.roncoo.pay.trade.utils.alipay.AlipayConfigUtil;
import com.roncoo.pay.trade.utils.alipay.sign.MD5;


/* *
 *类名：AlipaySubmit
 *功能：支付宝各接口请求提交类
 *详细：构造支付宝各接口表单HTML文本，获取远程HTTP数据
 *版本：3.3
 *日期：2012-08-13
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipaySubmit {
    
    /**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    //private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";
    private static final String ALIPAY_GATEWAY_NEW = "https://openapi.alipaydev.com/gateway.do?";
	
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	public static String buildRequestMysign(Map<String, String> sPara) {
    	String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        if(AlipayConfigUtil.sign_type.equals("MD5") ) {
        	mysign = MD5.sign(prestr, AlipayConfigUtil.key, AlipayConfigUtil.input_charset);
        }
        return mysign;
    }
	
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayConfigUtil.sign_type);

        return sPara;
    }

    /**
     * 建立请求，以表单HTML形式构造（默认）
     * @param sParaTemp 请求参数数组
     * @param strMethod 提交方式。两个值可选：post、get
     * @param strButtonName 确认按钮显示文字
     * @return 提交表单HTML文本
     */
    public static String buildRequest(Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuffer sbHtml = new StringBuffer();

        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + ALIPAY_GATEWAY_NEW
                      + "_input_charset=" + AlipayConfigUtil.input_charset + "\" method=\"" + strMethod
                      + "\">");

        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

        return sbHtml.toString();
    }
    
    /**
     * AliPay新调用方法
     * 生成请求form代码
     * @param sParaTemp
     * @param string
     * @param string2
     * @return
     */
    public static String buildRequest1(Map<String, String> sParaTemp, String string, String string2) {
		/*AlipayClient alipayClient = new DefaultAlipayClient("http://openapi.alipaydev.com/gateway.do",
				"2016082700319896",
				"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC3Jc/+CXV21ze7Ys/67YVrIvlxOVxn4lKMOMhgol3Yx/5bGt250nxhKv1yJ7mZzXXv/6aHwhGSrszsElOfRIIk+NQAlFvj1z432e/iX3vMSSKu7MBqimhnxG4rA/q2xfFUAkgn/85cIlzSmwbWEUlo5t19MTea/8ANs3HaYDo33x/Dv47/i37EnIWbE7hX/qMSbsglgbD0/hU64WfgIU+ZcebykEnq6tmGPj+7j8g582MwcfLQGc3eGWMR/MrHvttKjr5H8BUYjS9Ta5zXzF/qhLzH6EAVLo4dIf2VNeIMX41K0fkgbH+zx+GQ45ZII1E/PEk3r3sPuC5gw4ltssxHAgMBAAECggEBAKA0TEcsIPsOcWrRmZomkNFRq57WDTW17H1J4kVdYfgRoTYyPwefzjr07vQfOaQG7IY+O35/TP3hN9G8ijdEJw+ONWw4WlTn4D3cvpBm85ST2OnooLvRTFiQo8mu1m1wopPY1yNeCTXgvQ9gmk9AmdVQNSigl8JWurmBYTMjf2mEnQl90xjilOB67TYhJ2/Ay+AAZpWqXln7QzspOF0ce98v9JWTjIcCTxhBS5yS0Lum02o2DguHF/5po1sTF2R/U+rE3+FgKumnTTkbs5T0kYEKXIx9ZjbqNSayXsk+uTy0PsnQlkfi7Wlbp2VbJ+pS9+DY6qx7KbollKZKSKHhZZECgYEA/QbvRVKHXQO2HQTLznV7MuSqgeJ8+bjvbTJuMmqhhLScXTMq5MnOzXhUeMQcwR0HeD4u7OsN+gq9s8Li3Vaa6wCdiEla9wVxaH7lYX72ilfX5sOZHbrBnEOOTfoLqRExTnVNbHtApWHZOPUv7RKbjA+iLKurdzXviyQNZXbnH4kCgYEAuUyxFFlNALLkbVxLGFYlFyhiaHNAbghjLQ/GrXrM8MSn3WZHn1KaRqiSyIRJzMbRUNn0EeVKi2r/nShWeYDPu7XXWiWcmJku2Y/DYqDHsbBmGkLNFD/achW0OYYSUzAb/ywL1IRe1v3sx5qdO8StDX+WIV0RyBYJbhn4JsbSSU8CgYA4euTMH5jxrVNodNqdkmHWwW5CIfFtuNdRE7G/dUfqnHpO344Sle2gtdx9PKGChd1V/ONypSFwkBc6WiVT7PIVxQRlGKLCgyeGgNTpB3M2/FbIPx8doMN5AydvxoH10k2kStDmhzit8gKQEUMKc13fTNoRiJx0tshq0bhfzsPWqQKBgQCBJnDty+gdqpIHnyJADhq/70fXoSyxBGuLhsllNIgO8CJH7/fPlhUtVmUoGPwPHCvb/G1e4793ONZ8RRcwjJU9MdqtXDWvLmU3AjqeTY2hzV78wr6JdI/eoD4DMe0nygpZaeu87Z3knwsffCZG+CfdlqWfD21LVgCwmq0Y6c67hQKBgQCKjUkfAAFV/qLRKsZ5mfeAlkNu8ErgksstHvzW2AvRPZT805tude60EFnfWESb2a4KBNJge2h1v14v4oGwdOhRvYAHkhcyX/dR8OiQb/Iti1/6eEDwN7UkPY5i/hmgsb70wcgX2zSOfLlf9Gya7qdKYiwKBlgX/SGa3AJgKlKqlA==",
				"json",
				"utf-8",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu6DtX7fm2hEUVqD4+dwg0Jc0nC5R+T57JVxihKqN6b12G7Sh30ZG3ZpHr6vep24wfIHiIlRuUjAZ2MABrE2n8z1dh8N3gDKB9ljCMYqStTljADSPfXt3zwqL6ONsZhHyV7Eo8pHQEKnq+p0bRw4jSLrgyBFdlEmIp2aqOZYNKfd1bfzPCKdHteE1wnKDH4SyUc6U2dZ+Ea0wBmQMzTfTgkgArE6Oyh5fXoSZLC7nmgwZe167rWmKuTWJ1TXuUFM0ebiI0OBxGGrAexEZ6oF6wkLWHX5lkYkZeTBpd/a7FfayXwA5XyqGajYg5ySBMfKwLu/jyC7+HfC9koJ3hBfjyQIDAQAB",
				"RSA2");*/
    	AlipayClient alipayClient = new DefaultAlipayClient("http://openapi.alipaydev.com/gateway.do",
				"2016082700319896",
				"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC3Jc/+CXV21ze7Ys/67YVrIvlxOVxn4lKMOMhgol3Yx/5bGt250nxhKv1yJ7mZzXXv/6aHwhGSrszsElOfRIIk+NQAlFvj1z432e/iX3vMSSKu7MBqimhnxG4rA/q2xfFUAkgn/85cIlzSmwbWEUlo5t19MTea/8ANs3HaYDo33x/Dv47/i37EnIWbE7hX/qMSbsglgbD0/hU64WfgIU+ZcebykEnq6tmGPj+7j8g582MwcfLQGc3eGWMR/MrHvttKjr5H8BUYjS9Ta5zXzF/qhLzH6EAVLo4dIf2VNeIMX41K0fkgbH+zx+GQ45ZII1E/PEk3r3sPuC5gw4ltssxHAgMBAAECggEBAKA0TEcsIPsOcWrRmZomkNFRq57WDTW17H1J4kVdYfgRoTYyPwefzjr07vQfOaQG7IY+O35/TP3hN9G8ijdEJw+ONWw4WlTn4D3cvpBm85ST2OnooLvRTFiQo8mu1m1wopPY1yNeCTXgvQ9gmk9AmdVQNSigl8JWurmBYTMjf2mEnQl90xjilOB67TYhJ2/Ay+AAZpWqXln7QzspOF0ce98v9JWTjIcCTxhBS5yS0Lum02o2DguHF/5po1sTF2R/U+rE3+FgKumnTTkbs5T0kYEKXIx9ZjbqNSayXsk+uTy0PsnQlkfi7Wlbp2VbJ+pS9+DY6qx7KbollKZKSKHhZZECgYEA/QbvRVKHXQO2HQTLznV7MuSqgeJ8+bjvbTJuMmqhhLScXTMq5MnOzXhUeMQcwR0HeD4u7OsN+gq9s8Li3Vaa6wCdiEla9wVxaH7lYX72ilfX5sOZHbrBnEOOTfoLqRExTnVNbHtApWHZOPUv7RKbjA+iLKurdzXviyQNZXbnH4kCgYEAuUyxFFlNALLkbVxLGFYlFyhiaHNAbghjLQ/GrXrM8MSn3WZHn1KaRqiSyIRJzMbRUNn0EeVKi2r/nShWeYDPu7XXWiWcmJku2Y/DYqDHsbBmGkLNFD/achW0OYYSUzAb/ywL1IRe1v3sx5qdO8StDX+WIV0RyBYJbhn4JsbSSU8CgYA4euTMH5jxrVNodNqdkmHWwW5CIfFtuNdRE7G/dUfqnHpO344Sle2gtdx9PKGChd1V/ONypSFwkBc6WiVT7PIVxQRlGKLCgyeGgNTpB3M2/FbIPx8doMN5AydvxoH10k2kStDmhzit8gKQEUMKc13fTNoRiJx0tshq0bhfzsPWqQKBgQCBJnDty+gdqpIHnyJADhq/70fXoSyxBGuLhsllNIgO8CJH7/fPlhUtVmUoGPwPHCvb/G1e4793ONZ8RRcwjJU9MdqtXDWvLmU3AjqeTY2hzV78wr6JdI/eoD4DMe0nygpZaeu87Z3knwsffCZG+CfdlqWfD21LVgCwmq0Y6c67hQKBgQCKjUkfAAFV/qLRKsZ5mfeAlkNu8ErgksstHvzW2AvRPZT805tude60EFnfWESb2a4KBNJge2h1v14v4oGwdOhRvYAHkhcyX/dR8OiQb/Iti1/6eEDwN7UkPY5i/hmgsb70wcgX2zSOfLlf9Gya7qdKYiwKBlgX/SGa3AJgKlKqlA==",
				"json",
				"utf-8",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu6DtX7fm2hEUVqD4+dwg0Jc0nC5R+T57JVxihKqN6b12G7Sh30ZG3ZpHr6vep24wfIHiIlRuUjAZ2MABrE2n8z1dh8N3gDKB9ljCMYqStTljADSPfXt3zwqL6ONsZhHyV7Eo8pHQEKnq+p0bRw4jSLrgyBFdlEmIp2aqOZYNKfd1bfzPCKdHteE1wnKDH4SyUc6U2dZ+Ea0wBmQMzTfTgkgArE6Oyh5fXoSZLC7nmgwZe167rWmKuTWJ1TXuUFM0ebiI0OBxGGrAexEZ6oF6wkLWHX5lkYkZeTBpd/a7FfayXwA5XyqGajYg5ySBMfKwLu/jyC7+HfC9koJ3hBfjyQIDAQAB",
				"RSA2");
    	
		
		AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
	    alipayRequest.setReturnUrl("http://223.88.3.254:8010/alipay-demo/paySuccess.jsp");
	    alipayRequest.setNotifyUrl("http://223.88.3.254:8010/alipay-demo/NotifyServlet");//在公共参数中设置回跳和通知地址
	    alipayRequest.setBizContent("{" +
	        "    \"out_trade_no\":\"23950320010101001\"," +
	        "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
	        "    \"total_amount\":88.88," +
	        "    \"subject\":\"Iphone8 16G\"," +
	        "    \"body\":\"Iphone6 16G\"," +
	        "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
	        "    \"extend_params\":{" +
	        "    \"sys_service_provider_id\":\"2088511833207846\"" +
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
     * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数
     * 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
     * @return 时间戳字符串
     * @throws IOException
     * @throws DocumentException
     * @throws MalformedURLException
     */
	public static String query_timestamp() throws MalformedURLException,
                                                        DocumentException, IOException {

        //构造访问query_timestamp接口的URL串
        String strUrl = ALIPAY_GATEWAY_NEW + "service=query_timestamp&partner=" + AlipayConfigUtil.partner + "&_input_charset" + AlipayConfigUtil.input_charset;
        StringBuffer result = new StringBuffer();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new URL(strUrl).openStream());

        List<Node> nodeList = doc.selectNodes("//alipay/*");

        for (Node node : nodeList) {
            // 截取部分不需要解析的信息
            if (node.getName().equals("is_success") && node.getText().equals("T")) {
                // 判断是否有成功标示
                List<Node> nodeList1 = doc.selectNodes("//response/timestamp/*");
                for (Node node1 : nodeList1) {
                    result.append(node1.getText());
                }
            }
        }

        return result.toString();
    }

	
}
