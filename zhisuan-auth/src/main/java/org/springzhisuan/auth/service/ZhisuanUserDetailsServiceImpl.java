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
package org.springzhisuan.auth.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.stereotype.Service;
import org.springzhisuan.auth.constant.AuthConstant;
import org.springzhisuan.auth.utils.TokenUtil;
import org.springzhisuan.common.cache.CacheNames;
import org.springzhisuan.core.redis.cache.ZhisuanRedis;
import org.springzhisuan.core.tool.api.R;
import org.springzhisuan.core.tool.utils.*;
import org.springzhisuan.system.entity.Tenant;
import org.springzhisuan.system.feign.ISysClient;
import org.springzhisuan.system.user.entity.User;
import org.springzhisuan.system.user.entity.UserInfo;
import org.springzhisuan.system.user.enums.UserEnum;
import org.springzhisuan.system.user.feign.IUserClient;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;

/**
 * 用户信息
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class ZhisuanUserDetailsServiceImpl implements UserDetailsService {

	public static final Integer FAIL_COUNT = 5;

	private final IUserClient userClient;
	private final ISysClient sysClient;

	private final ZhisuanRedis zhisuanRedis;

	@Override
	@SneakyThrows
	public ZhisuanUserDetails loadUserByUsername(String username) {
		HttpServletRequest request = WebUtil.getRequest();
		// 获取用户绑定ID
		String headerDept = request.getHeader(TokenUtil.DEPT_HEADER_KEY);
		String headerRole = request.getHeader(TokenUtil.ROLE_HEADER_KEY);
		// 获取租户ID
		String headerTenant = request.getHeader(TokenUtil.TENANT_HEADER_KEY);
		String paramTenant = request.getParameter(TokenUtil.TENANT_PARAM_KEY);
		String password = request.getParameter(TokenUtil.PASSWORD_KEY);
		String grantType = request.getParameter(TokenUtil.GRANT_TYPE_KEY);
		if (StringUtil.isAllBlank(headerTenant, paramTenant)) {
			throw new UserDeniedAuthorizationException(TokenUtil.TENANT_NOT_FOUND);
		}
		String tenantId = StringUtils.isBlank(headerTenant) ? paramTenant : headerTenant;

		// 判断登录是否锁定
		// TODO 2.8.3版本将增加：1.参数管理读取配置 2.用户管理增加解封按钮
		int count = getFailCount(tenantId, username);
		if (count >= FAIL_COUNT) {
			throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_TOO_MANY_FAILS);
		}

		// 获取租户信息
		R<Tenant> tenant = sysClient.getTenant(tenantId);
		if (tenant.isSuccess()) {
			if (TokenUtil.judgeTenant(tenant.getData())) {
				throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_TENANT_PERMISSION);
			}
		} else {
			throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_TENANT);
		}

		// 获取用户类型
		String userType = Func.toStr(request.getHeader(TokenUtil.USER_TYPE_HEADER_KEY), TokenUtil.DEFAULT_USER_TYPE);

		// 远程调用返回数据
		R<UserInfo> result;
		// 根据不同用户类型调用对应的接口返回数据，用户可自行拓展
		if (userType.equals(UserEnum.WEB.getName())) {
			result = userClient.userInfo(tenantId, username, UserEnum.WEB.getName());
		} else if (userType.equals(UserEnum.APP.getName())) {
			result = userClient.userInfo(tenantId, username, UserEnum.APP.getName());
		} else {
			result = userClient.userInfo(tenantId, username, UserEnum.OTHER.getName());
		}

		// 判断返回信息
		if (result.isSuccess()) {
			UserInfo userInfo = result.getData();
			User user = userInfo.getUser();
			// 用户不存在,但提示用户名与密码错误并锁定账号
			if (user == null || user.getId() == null) {
				setFailCount(tenantId, username, count);
				throw new UsernameNotFoundException(TokenUtil.USER_NOT_FOUND);
			}
			// 用户存在但密码错误,超过次数则锁定账号
			if (!grantType.equals(TokenUtil.REFRESH_TOKEN_KEY) && !user.getPassword().equals(DigestUtil.hex(password))) {
				setFailCount(tenantId, username, count);
				throw new UsernameNotFoundException(TokenUtil.USER_NOT_FOUND);
			}
			// 用户角色不存在
			if (Func.isEmpty(userInfo.getRoles())) {
				throw new UserDeniedAuthorizationException(TokenUtil.USER_HAS_NO_ROLE);
			}
			// 多部门情况下指定单部门
			if (Func.isNotEmpty(headerDept) && user.getDeptId().contains(headerDept)) {
				user.setDeptId(headerDept);
			}
			// 多角色情况下指定单角色
			if (Func.isNotEmpty(headerRole) && user.getRoleId().contains(headerRole)) {
				R<List<String>> roleResult = sysClient.getRoleAliases(headerRole);
				if (roleResult.isSuccess()) {
					userInfo.setRoles(roleResult.getData());
				}
				user.setRoleId(headerRole);
			}
			return new ZhisuanUserDetails(user.getId(),
				user.getTenantId(), StringPool.EMPTY, user.getName(), user.getRealName(), user.getDeptId(), user.getPostId(), user.getRoleId(), Func.join(userInfo.getRoles()), Func.toStr(user.getAvatar(), TokenUtil.DEFAULT_AVATAR),
				username, AuthConstant.ENCRYPT + user.getPassword(), userInfo.getDetail(), true, true, true, true,
				AuthorityUtils.commaSeparatedStringToAuthorityList(Func.join(result.getData().getRoles())));
		} else {
			throw new UsernameNotFoundException(result.getMsg());
		}
	}

	/**
	 * 获取账号错误次数
	 *
	 * @param tenantId 租户id
	 * @param username 账号
	 * @return int
	 */
	private int getFailCount(String tenantId, String username) {
		return Func.toInt(zhisuanRedis.get(CacheNames.tenantKey(tenantId, CacheNames.USER_FAIL_KEY, username)), 0);
	}

	/**
	 * 设置账号错误次数
	 *
	 * @param tenantId 租户id
	 * @param username 账号
	 * @param count    次数
	 */
	private void setFailCount(String tenantId, String username, int count) {
		zhisuanRedis.setEx(CacheNames.tenantKey(tenantId, CacheNames.USER_FAIL_KEY, username), count + 1, Duration.ofMinutes(30));
	}


}
