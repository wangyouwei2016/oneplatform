package com.oneplatform.modules.system.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jeesuite.common.constants.PermissionLevel;
import com.jeesuite.common.model.IdParam;
import com.jeesuite.common.model.NameValuePair;
import com.jeesuite.common.model.Page;
import com.jeesuite.common.model.PageParams;
import com.jeesuite.common.model.SelectOption;
import com.jeesuite.springweb.CurrentRuntimeContext;
import com.jeesuite.springweb.annotation.ApiMetadata;
import com.jeesuite.springweb.model.PageQueryRequest;
import com.oneplatform.modules.system.constants.FunctionResourceType;
import com.oneplatform.modules.system.constants.GrantSourceType;
import com.oneplatform.modules.system.constants.GrantTargetType;
import com.oneplatform.modules.system.dao.entity.FunctionResourceEntity;
import com.oneplatform.modules.system.dao.mapper.FunctionResourceEntityMapper;
import com.oneplatform.modules.system.dto.UserGroup;
import com.oneplatform.modules.system.dto.param.GrantPermItem;
import com.oneplatform.modules.system.dto.param.GrantRelationParam;
import com.oneplatform.modules.system.dto.param.GrantUserGroupPermParam;
import com.oneplatform.modules.system.dto.param.UserGroupParam;
import com.oneplatform.modules.system.dto.param.UserGroupQueryParam;
import com.oneplatform.modules.system.service.UserGroupService;
import com.oneplatform.modules.system.service.UserPermissionService;

import io.swagger.annotations.ApiOperation;


@RestController
public class UserGroupController {

	@Autowired
	private UserGroupService userGroupService;
	@Autowired 
    private UserPermissionService userPermissionService;
	@Autowired
    private FunctionResourceEntityMapper functionResourceMapper;
 

	
	@ApiOperation(value = "新增角色", notes = "### 新增角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public IdParam<Integer> add(UserGroupParam param) {
		String tenantId = CurrentRuntimeContext.getTenantId(false);
		param.setTenantId(tenantId);
		return userGroupService.addUserGroup(param);
	}

	
	@ApiOperation(value = "删除角色", notes = "### 删除角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public void delete(IdParam<Integer> param) {
		userGroupService.deleteUserGroup(param.getId());
	}

	
	@ApiOperation(value = "更新角色", notes = "### 更新角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public void update(UserGroupParam param) {
		userGroupService.updateUserGroup(param);
	}

	
	@ApiOperation(value = "启用|禁用角色", notes = "### 启用|禁用角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public void switchUserGroup(IdParam<Integer> param) {
		userGroupService.switchUserGroup(param.getId());
	}

	
	@ApiOperation(value = "根据id查询角色", notes = "### 根据id查询角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public UserGroup get(Integer id) {
		return userGroupService.getUserGroup(id);
	}

	
	@ApiOperation(value = "分页查询角色", notes = "### 分页查询角色 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public Page<UserGroup> pageQry(PageQueryRequest<UserGroupQueryParam> queryParam) {
		String tenantId = CurrentRuntimeContext.getTenantId(false);
		if(queryParam.getExample() == null)queryParam.setExample(new UserGroupQueryParam());
		queryParam.getExample().setTenantId(tenantId);
		return userGroupService.pageQryUserGroup(new PageParams(queryParam.getPageNo(), queryParam.getPageSize()),
				queryParam.getExample());
	}

	
	@ApiOperation(value = "角色下拉列表", notes = "### 角色下拉列表 \n -xxx")
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
	public List<SelectOption> options(String departmentId) {
		String tenantId = CurrentRuntimeContext.getTenantId(false);
		UserGroupQueryParam param = new UserGroupQueryParam();
		param.setTenantId(tenantId);
		param.setDepartmentId(departmentId);
		return userGroupService.listByQueryParam(param).stream().map( o -> {
			return new SelectOption(o.getId().toString(), o.getName());
		}).collect(Collectors.toList());
	}


	@ApiMetadata(permissionLevel = PermissionLevel.LoginRequired)
	@ApiOperation(value = "分配用户组权限")
	@PostMapping(value = "/grantPermissions")
	@ResponseBody
	public void grantPermissions(@RequestBody GrantUserGroupPermParam param) {

		UserGroup group = userGroupService.getUserGroup(param.getUserGroupId());
		
		Map<String, List<String>> typeGroupMap = new HashMap<>(3);
		
		List<String> tmpList;
		for (GrantPermItem item : param.getPermItems()) {
			tmpList = typeGroupMap.get(item.getType());
			if (tmpList == null) {
				tmpList = new ArrayList<>();
				typeGroupMap.put(item.getType(), tmpList);
			}
			if(!tmpList.contains(item.getId().toString())) {				
				tmpList.add(item.getId().toString());
			}
		}
		
		//查询按钮归属的菜单
		if(typeGroupMap.containsKey(FunctionResourceType.button.name())){
			List<Integer> buttonIds = typeGroupMap.get(FunctionResourceType.button.name()).stream().map(o -> {
				return Integer.parseInt(o);
		    }).collect(Collectors.toList());
			List<FunctionResourceEntity> buttons = functionResourceMapper.selectByPrimaryKeys(buttonIds);
			for (FunctionResourceEntity button : buttons) {
				tmpList = typeGroupMap.get(FunctionResourceType.button.name());
				if (tmpList == null) {
					tmpList = new ArrayList<>();
					typeGroupMap.put(FunctionResourceType.menu.name(), tmpList);
				}
				if(!tmpList.contains(button.getParentId().toString())){					
					tmpList.add(button.getParentId().toString());
				}
			}
		}

		final List<GrantRelationParam> paramList = new ArrayList<>();
		typeGroupMap.forEach((k, v) -> {
			GrantRelationParam relationParam = new GrantRelationParam();
			relationParam.setTargetType(GrantTargetType.userGroup);
			relationParam.setTargetId(param.getUserGroupId().toString());
			relationParam.setSourceType(GrantSourceType.valueOf(k));
			relationParam.setSourceIdList(v);
			paramList.add(relationParam);
		});
		userGroupService.updateGrantedPermissions(paramList);

	}
	
	
	@ApiMetadata(permissionLevel = PermissionLevel.PermissionRequired, actionLog = true)
    @ApiOperation(value = "查询用户组按钮权限",notes = "### 查询用户组按钮权限 \n -xxx")
    @GetMapping(value = "buttons")
    @ResponseBody
    public List<NameValuePair> listUserGroupButtons(@RequestParam("groupId")Integer userGroupId) {
    	List<NameValuePair> buttons = userPermissionService.findUserGroupGrantButtons(userGroupId);
		return buttons;
    }
}
