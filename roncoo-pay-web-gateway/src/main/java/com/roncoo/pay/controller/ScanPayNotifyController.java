/*
 * Copyright 2015-2102 RonCoo(http://www.roncoo.com) Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roncoo.pay.controller;

/**
 * <b>功能说明:后台通知结果控制类
 * </b>
 * @author  Peter
 * <a href="http://www.roncoo.com">龙果学院(www.roncoo.com)</a>
 */

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.roncoo.pay.common.core.enums.PayWayEnum;
import com.roncoo.pay.common.core.utils.StringUtil;
import com.roncoo.pay.trade.service.RpTradePaymentManagerService;
import com.roncoo.pay.trade.utils.WeiXinPayUtils;
import com.roncoo.pay.trade.utils.alipay.util.AliPayUtil;
import com.roncoo.pay.trade.vo.OrderPayResultVo;

@Controller
@RequestMapping(value = "/scanPayNotify")
public class ScanPayNotifyController {

    @Autowired
    private RpTradePaymentManagerService rpTradePaymentManagerService;

    /**
     * 用户支付成功后，支付宝/微信 异步通知请求该url
     * 
     * http://roncoo.iok.la/roncoo-pay-web-gateway/scanPayNotify/notify/ALIPAY?
     * https://商家网站通知地址?
     * 		voucher_detail_list=
     * 		[{"amount":"0.20",
     * 			"merchantContribute":"0.00",
     * 			"name":"5折券","otherContribute":"0.20",
     * 			"type":"ALIPAY_DISCOUNT_VOUCHER","voucherId":"2016101200073002586200003BQ4"
     * 		}]&
     * 		fund_bill_list=[
     * 			{"amount":"0.80","fundChannel":"ALIPAYACCOUNT"},
     * 			{"amount":"0.20","fundChannel":"MDISCOUNT"}]&
     * 		subject=PC网站支付交易&
     * 		trade_no=2016101221001004580200203978&
     * 		gmt_create=2016-10-12 21:36:12&
     * 		notify_type=trade_status_sync&
     * 		total_amount=1.00&
     * 		out_trade_no=mobile_rdm862016-10-12213600&
     * 		invoice_amount=0.80&
     * 		seller_id=2088201909970555&
     * 		notify_time=2016-10-12 21:41:23&
     * 		trade_status=TRADE_SUCCESS&
     * 		gmt_payment=2016-10-12 21:37:19&
     * 		receipt_amount=0.80&
     * 		passback_params=passback_params123&
     * 		buyer_id=2088102114562585&
     * 		app_id=2016092101248425&
     * 		notify_id=7676a2e1e4e737cff30015c4b7b55e3kh6&
     * 		sign_type=RSA2&
     * 		buyer_pay_amount=0.80&
     * 		sign=***&
     * 		point_amount=0.00
     * @param payWayCode
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws Exception
     */
    @RequestMapping("/notify/{payWayCode}")
    public void notify(@PathVariable("payWayCode") String  payWayCode , HttpServletRequest httpServletRequest , HttpServletResponse httpServletResponse) throws Exception {

        Map<String , String> notifyMap = new HashMap<String , String >();
        if (PayWayEnum.WEIXIN.name().equals(payWayCode)){
            InputStream inputStream = httpServletRequest.getInputStream();// 从request中取得输入流
            notifyMap = WeiXinPayUtils.parseXml(inputStream);
        }else if (PayWayEnum.ALIPAY.name().equals(payWayCode)){
            Map<String, String[]> requestParams = httpServletRequest.getParameterMap();
            notifyMap = AliPayUtil.parseNotifyMsg(requestParams);
        }

        String completeWeiXinScanPay = rpTradePaymentManagerService.completeScanPay(payWayCode ,notifyMap);
        if (!StringUtil.isEmpty(completeWeiXinScanPay)){
            if (PayWayEnum.WEIXIN.name().equals(payWayCode)){
                httpServletResponse.setContentType("text/xml");
            }
            httpServletResponse.getWriter().print(completeWeiXinScanPay);
        }
    }

    /**
     * 买家扫码支付成功后，配置给支付宝同步返回请求此路径
     * 根据本系统的商户配置的return_url返回相应url
     * 
     * 最新支付宝sdk,同步返回的请求中sdk内已完成验签，2017，12，1
     * 
     * http://roncoo.iok.la/roncoo-pay-web-gateway/scanPayNotify/result/ALIPAY?
     * total_amount=0.10&
     * timestamp=2017-12-12+10%3A17%3A08&
     * sign=MHIi%2Ftf9%2FYCi1ozZ1xJ1f2Dqjdq8O741aIZ36DkpPgROOmC8Y2tt7pe%2BQsUs5JjsSomozDE0H1eEjexx7tq4Zi%2FlszG9rySvgu0qTZa%2FS8%2F0FJusk70f01fLfil7RhUsVh6ZxzaFPPFO6AH%2FjPqmtlD6WA7DAiNAj7ZU%2Fu45W8D%2B6a0F8YNSdTzGvNCMe7zv05iPHM31o%2B5Z6UHtX89HVf8jbjgVwh%2Bv3kWh5Zmnex5wtu5o3%2BBQIcPhAbMhiYoBlLYxrfHOmTj6dJJ%2F02qhqxbybFtsFnlGOjxCffyjVcpUf7lu1PKi%2Bj1HHCx0PeI%2FyHRzSmtsT37DcoTYtRkZGg%3D%3D&
     * trade_no=2017121221001004750200160995&
     * sign_type=RSA2&
     * auth_app_id=2016082700319896&
     * charset=utf-8&
     * seller_id=2088102173194640&
     * method=alipay.trade.page.pay.return&
     * app_id=2016082700319896&
     * out_trade_no=66662017121210000021&
     * version=1.0
     * @param payWayCode
     * @param httpServletRequest
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/result/{payWayCode}")
    public String result(@PathVariable("payWayCode") String payWayCode, HttpServletRequest httpServletRequest , Model model) throws Exception {

        Map<String,String> resultMap = new HashMap<String,String>();
        Map requestParams = httpServletRequest.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            valueStr = new String(valueStr);
            resultMap.put(name, valueStr);
        }

        OrderPayResultVo scanPayByResult = rpTradePaymentManagerService.completeScanPayByResult(payWayCode, resultMap);
        model.addAttribute("scanPayByResult",scanPayByResult);

        return "PayResult";
    }

}
