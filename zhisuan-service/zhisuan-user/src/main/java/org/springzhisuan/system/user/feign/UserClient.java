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
package org.springzhisuan.system.user.feign;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springzhisuan.core.tenant.annotation.NonDS;
import org.springzhisuan.core.tool.api.R;
import org.springzhisuan.core.tool.utils.Func;
import org.springzhisuan.system.user.entity.User;
import org.springzhisuan.system.user.entity.UserInfo;
import org.springzhisuan.system.user.entity.UserOauth;
import org.springzhisuan.system.user.enums.UserEnum;
import org.springzhisuan.system.user.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务Feign实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class UserClient implements IUserClient {

	private final IUserService service;

	@Override
	@GetMapping(USER_INFO_BY_ID)
	public R<User> userInfoById(Long userId) {
		return R.data(service.getById(userId));
	}

	@Override
	@GetMapping(USER_INFO_BY_ACCOUNT)
	public R<User> userByAccount(String tenantId, String account) {
		return R.data(service.userByAccount(tenantId, account));
	}

	@Override
	@GetMapping(USER_INFO)
	public R<UserInfo> userInfo(String tenantId, String account) {
		return R.data(service.userInfo(tenantId, account));
	}

	@Override
	@GetMapping(USER_INFO_BY_TYPE)
	public R<UserInfo> userInfo(String tenantId, String account, String userType) {
		return R.data(service.userInfo(tenantId, account, UserEnum.of(userType)));
	}

	@Override
	@PostMapping(USER_AUTH_INFO)
	public R<UserInfo> userAuthInfo(@RequestBody UserOauth userOauth) {
		return R.data(service.userInfo(userOauth));
	}

	@Override
	@PostMapping(SAVE_USER)
	public R<Boolean> saveUser(@RequestBody User user) {
		return R.data(service.submit(user));
	}

	@Override
	@PostMapping(REMOVE_USER)
	public R<Boolean> removeUser(String tenantIds) {
		return R.data(service.remove(Wrappers.<User>query().lambda().in(User::getTenantId, Func.toStrList(tenantIds))));
	}

}
