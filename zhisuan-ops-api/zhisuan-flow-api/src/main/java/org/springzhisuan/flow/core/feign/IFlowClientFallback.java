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
package org.springzhisuan.flow.core.feign;

import org.springzhisuan.core.tool.api.R;
import org.springzhisuan.flow.core.entity.ZhisuanFlow;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 流程远程调用失败处理类
 *
 * @author Chill
 */
@Component
public class IFlowClientFallback implements IFlowClient {

	@Override
	public R<ZhisuanFlow> startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
		return R.fail("远程调用失败");
	}

	@Override
	public R<ZhisuanFlow> startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
		return R.fail("远程调用失败");
	}

	@Override
	public R completeTask(String taskId, String processInstanceId, String comment, Map<String, Object> variables) {
		return R.fail("远程调用失败");
	}

	@Override
	public R<Object> taskVariable(String taskId, String variableName) {
		return R.fail("远程调用失败");
	}

	@Override
	public R<Map<String, Object>> taskVariables(String taskId) {
		return R.fail("远程调用失败");
	}

}
