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
package org.springzhisuan.system.wrapper;

import org.springzhisuan.core.mp.support.BaseEntityWrapper;
import org.springzhisuan.core.tool.constant.ZhisuanConstant;
import org.springzhisuan.core.tool.node.ForestNodeMerger;
import org.springzhisuan.core.tool.utils.BeanUtil;
import org.springzhisuan.core.tool.utils.Func;
import org.springzhisuan.system.cache.SysCache;
import org.springzhisuan.system.entity.Role;
import org.springzhisuan.system.vo.RoleVO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class RoleWrapper extends BaseEntityWrapper<Role, RoleVO> {

	public static RoleWrapper build() {
		return new RoleWrapper();
	}

	@Override
	public RoleVO entityVO(Role role) {
		RoleVO roleVO = Objects.requireNonNull(BeanUtil.copy(role, RoleVO.class));
		if (Func.equals(role.getParentId(), ZhisuanConstant.TOP_PARENT_ID)) {
			roleVO.setParentName(ZhisuanConstant.TOP_PARENT_NAME);
		} else {
			Role parent = SysCache.getRole(role.getParentId());
			roleVO.setParentName(parent.getRoleName());
		}
		return roleVO;
	}


	public List<RoleVO> listNodeVO(List<Role> list) {
		List<RoleVO> collect = list.stream().map(this::entityVO).collect(Collectors.toList());
		return ForestNodeMerger.merge(collect);
	}

}
