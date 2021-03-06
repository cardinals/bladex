/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springzhisuan.resource.builder.sms;

import com.yunpian.sdk.YunpianClient;
import lombok.SneakyThrows;
import org.springzhisuan.core.redis.cache.ZhisuanRedis;
import org.springzhisuan.core.sms.SmsTemplate;
import org.springzhisuan.core.sms.props.SmsProperties;
import org.springzhisuan.core.sms.YunpianSmsTemplate;
import org.springzhisuan.resource.entity.Sms;

/**
 * 云片短信构建类
 *
 * @author Chill
 */
public class YunpianSmsBuilder {

	@SneakyThrows
	public static SmsTemplate template(Sms sms, ZhisuanRedis zhisuanRedis) {
		SmsProperties smsProperties = new SmsProperties();
		smsProperties.setTemplateId(sms.getTemplateId());
		smsProperties.setAccessKey(sms.getAccessKey());
		smsProperties.setSignName(sms.getSignName());
		YunpianClient client = new YunpianClient(smsProperties.getAccessKey()).init();
		return new YunpianSmsTemplate(smsProperties, client, zhisuanRedis);
	}

}
