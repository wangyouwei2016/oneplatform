package com.oneplatform.modules.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.model.IdParam;
import com.jeesuite.common.util.BeanUtils;
import com.oneplatform.modules.system.constants.GrantSourceType;
import com.oneplatform.modules.system.constants.GrantTargetType;
import com.oneplatform.modules.system.constants.SubRelationType;
import com.oneplatform.modules.system.dao.entity.GrantRelationEntity;
import com.oneplatform.modules.system.dao.entity.SubordinateRelationEntity;
import com.oneplatform.modules.system.dao.mapper.GrantRelationEntityMapper;
import com.oneplatform.modules.system.dao.mapper.SubordinateRelationEntityMapper;
import com.oneplatform.modules.system.dto.GrantRelation;
import com.oneplatform.modules.system.dto.param.GrantRelationParam;
import com.oneplatform.modules.system.dto.param.SubordinateRelationParam;
import com.oneplatform.modules.system.dto.param.SubordinateRelationQueryParam;
import com.oneplatform.modules.system.dto.param.UserGrantRoleParam;

/**
 * 
 * <br>
 * Class Name   : RelationInternalService
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年12月23日
 */
@Service
public class RelationInternalService {

	private @Autowired SubordinateRelationEntityMapper subRelationMapper;
	private @Autowired GrantRelationEntityMapper grantRelationMapper;

	/**
	 * 更新从属关系
	 * @param parentId
	 * @param childIds
	 * @param systemId
	 * @param relationType
	 */
	@Transactional(rollbackFor = Exception.class)
	public void updateParentSubRelations(SubRelationType relationType,String parentId, List<String> childIds) {
		if (childIds == null) {
			childIds = new ArrayList<>(0);
		}
		Map<String, SubordinateRelationEntity> existRelations = subRelationMapper.findSubRelationByQueryParam(new SubordinateRelationQueryParam(relationType,parentId,null))
				                           .stream()
				                           .collect(Collectors.toMap(SubordinateRelationEntity::getChildId, e -> e));

		List<String> addIdList;
		List<String> removeIdList = null;
		if (!existRelations.isEmpty()) {
			addIdList = new ArrayList<>(childIds);
			addIdList.removeAll(existRelations.keySet());
			removeIdList = new ArrayList<>(existRelations.keySet());
			removeIdList.removeAll(childIds);
		} else {
			addIdList = childIds;
		}
		// add new
		if (addIdList != null && !addIdList.isEmpty()) {
			List<SubordinateRelationEntity> addList = addIdList.stream().map(childId -> {
				return new SubordinateRelationEntity( relationType.name(), parentId, childId);
			}).collect(Collectors.toList());
			subRelationMapper.insertList(addList);
		}
		// remove his
		if (removeIdList != null && !removeIdList.isEmpty()) {
			for (String childId : removeIdList) {
				subRelationMapper.deleteByPrimaryKey(existRelations.get(childId).getId());
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void updateChildSubRelations(SubRelationType relationType,String childId, String childDisplayName, List<String> parentIds) {
		Map<String, SubordinateRelationEntity> existRelations = subRelationMapper.findParentRelations( relationType.name(), childId)
		                 .stream()
                         .collect(Collectors.toMap(SubordinateRelationEntity::getParentId, e -> e));
		List<String> addIdList;
		List<String> removeIdList = null;
		if (!existRelations.isEmpty()) {
			addIdList = new ArrayList<>(parentIds);
			addIdList.removeAll(existRelations.keySet());
			removeIdList = new ArrayList<>(existRelations.keySet());
			removeIdList.removeAll(parentIds);
		} else {
			addIdList = parentIds;
		}
		// add new
		if (addIdList != null && !addIdList.isEmpty()) {
			List<SubordinateRelationEntity> addList = addIdList.stream().map(parentId -> {
				return new SubordinateRelationEntity( relationType.name(), parentId, childId, childDisplayName);
			}).collect(Collectors.toList());
			subRelationMapper.insertList(addList);
		}
		// remove his
		if (removeIdList != null && !removeIdList.isEmpty()) {
			for (String parentId : removeIdList) {
				subRelationMapper.deleteByPrimaryKey(existRelations.get(parentId).getId());
			}
		}
	}

	/**
	 * 保存授权数据（可替换updateGrantRelations方法）
	 * @param list
	 */
	public void saveGrantRelations(List<GrantRelation> list) {
		if(list != null && !list.isEmpty()){
			GrantRelation one = list.get(0);
			// 1.先删掉数据
			grantRelationMapper.deleteByTargetIdAndTargetType(one.getTargetId(), one.getTargetType());
			// 2.再批量插入
			List<GrantRelationEntity> addList = list.stream().map(GrantRelationEntity::fromObject).collect(Collectors.toList());
			grantRelationMapper.insertList(addList);
		}
	}

	/**
	 * 更新授权
	 * @param param 授权参数
	 */
	@Transactional(rollbackFor = Exception.class)
	public void updateGrantRelations(GrantRelationParam param) {
		List<String> sourceIdList = param.getSourceIdList();
		if(CollectionUtils.isEmpty(sourceIdList)){
			sourceIdList = new ArrayList<>(0);
		}
		Map<String, GrantRelationEntity> existRelations = grantRelationMapper.findSourceGrantRelations(param.getTargetType().name(),
				param.getTargetId(),param.getSourceType().name())
				.stream()
				.collect(Collectors.toMap(GrantRelationEntity::getSourceId,e -> e));
		List<String> addIdList;
		List<String> removeIdList = null;
		if(!existRelations.isEmpty()){
			addIdList = new ArrayList<>(sourceIdList);
			addIdList.removeAll(existRelations.keySet());
			removeIdList = new ArrayList<>(existRelations.keySet());
			removeIdList.removeAll(sourceIdList);
		}else {
			addIdList = sourceIdList;
		}
		//add new
		if(!CollectionUtils.isEmpty(addIdList)){
			List<GrantRelationEntity> addList = addIdList.stream().map(sourceId -> {
				return new GrantRelationEntity(param.getTargetId(),param.getTargetType().name(), sourceId, param.getSourceType().name());
			}).collect(Collectors.toList());
			grantRelationMapper.insertList(addList);
		}
		//remove his
		if(!CollectionUtils.isEmpty(removeIdList)){
			removeIdList.stream().forEach(sourceId -> {
				grantRelationMapper.deleteByPrimaryKey(existRelations.get(sourceId).getId());
			});
		}
	}

	/**
	 * 
	 * @param systemId
	 * @param subRelationType
	 * @param parentId
	 * @return
	 */
	public List<String> findChildIdsFromSubRelations(SubRelationType subRelationType,String parentId){
		SubordinateRelationQueryParam queryParam = new SubordinateRelationQueryParam( subRelationType, parentId, null);
		return subRelationMapper.findSubRelationByQueryParam(queryParam)
				.stream()
				.map(subordinateRelationEntity -> {
					return subordinateRelationEntity.getChildId();
				}).collect(Collectors.toList());
	}
	
	public List<String> findParentIdsFromSubRelations(SubRelationType subRelationType,String childId){
		SubordinateRelationQueryParam queryParam = new SubordinateRelationQueryParam( subRelationType, null, childId);
		return subRelationMapper.findSubRelationByQueryParam(queryParam)
				.stream()
				.map(subordinateRelationEntity -> {
					return subordinateRelationEntity.getParentId();
				}).collect(Collectors.toList());
	}
	
	public Map<String, String> findGrantRelationIdMappings(GrantSourceType sourceType,GrantTargetType targetType,String targetId){
		List<GrantRelationEntity> relations = grantRelationMapper.findSourceGrantRelations( targetType.name(), targetId, sourceType.name());
		Map<String, String> map = new HashMap<>(relations.size());
		for (GrantRelationEntity entity : relations) {
			map.put(entity.getSourceId(), entity.getTargetId());
		}
		return map;
	}

    /**
     * 批量增加用户在系统下的角色，进行重复校验（重复的不会继续添加）
     * @param params
     * @return
     */
    public List<IdParam<Integer>> batchAddUserRoles(List<UserGrantRoleParam> params) {
	    List<SubordinateRelationEntity> insertEntities = new ArrayList<>();
		params.stream().forEach(param -> {
            if(!isExistRelationOfUserAndRole(param.getRoleId(), param.getUserId())) {
                SubordinateRelationEntity entity = new SubordinateRelationEntity();
                entity.setParentId(param.getRoleId());
                entity.setChildId(param.getUserId());
				entity.setChildName(param.getUserName());
                entity.setRelationType(SubRelationType.userToGroup.name());
                insertEntities.add(entity);
            }
        });
	    if(CollectionUtils.isEmpty(insertEntities)) {
	        return new ArrayList<>();
        }
	    subRelationMapper.insertList(insertEntities);
	    return insertEntities.stream().map(entity -> new IdParam<>(entity.getId())).collect(Collectors.toList());
    }

    /**
     * 角色和用户的关系是否已经存在
     * @param systemId
     * @param roleId
     * @param userId
     * @return
     */
    private Boolean isExistRelationOfUserAndRole( String roleId, String userId) {
	    SubordinateRelationQueryParam queryParam = new SubordinateRelationQueryParam();
	    queryParam.setRelationType(SubRelationType.userToGroup);
	    queryParam.setChildId(userId);
	    queryParam.setParentId(roleId);
	    List<SubordinateRelationEntity> entities = subRelationMapper.findSubRelationByQueryParam(queryParam);
	    return !CollectionUtils.isEmpty(entities);
    }

    /**
     * 批量调整角色下的用户
     * 全量删除，全量新增
     * @param paramList
     * @param roleId
     */
	@Transactional(rollbackFor = Exception.class)
    public void batchUpdateUsersForRole(List<SubordinateRelationParam> paramList, String roleId) {
		//不能同时为空，如果只穿了roleId！=null & paramList = [],直接删除roleId下的user
        if(CollectionUtils.isEmpty(paramList) && StringUtils.isEmpty(roleId)) {
            return;
        }
        if(StringUtils.isEmpty(roleId)) {
            roleId = paramList.get(0).getParentId();
            if(StringUtils.isEmpty(roleId)) {
                throw new JeesuiteBaseException("角色的id为空");
            }
        }
        subRelationMapper.deleteByParentId(roleId);
        //paramList为空，无需添加新的用户
        if(CollectionUtils.isEmpty(paramList)) {
        	return;
		}
        //以防参数中roleId不一致情况
        String finalRoleId = roleId;
        List<SubordinateRelationEntity> entityList = paramList.stream().map(param -> {
            SubordinateRelationEntity entity = new SubordinateRelationEntity();
            BeanUtils.copy(param,entity);
            entity.setParentId(finalRoleId);
            entity.setRelationType(SubRelationType.userToGroup.name());
            return entity;
        }).collect(Collectors.toList());

        subRelationMapper.insertList(entityList);
    }
}
