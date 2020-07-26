package com.wj.process.api.pc.system.message.service;

import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.errors.SystemError;
import com.feida.omms.dao.system.message.mapper.MessageMapper;
import com.feida.omms.dao.system.message.model.Message;
import com.feida.omms.tk.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PMessageService extends BaseService<Message> {
    @Autowired
    private MessageMapper messageMapper;

    public List<Dto> getByCondition(Dto params) {
        Dto account = WebUtils.getSessionAccount();
        params.put("receiverId",account.getInteger("id"));
        return messageMapper.getByCondition(params);
    }

    public int sendMes(Dto pamrams, List<Integer> receiverIds) {
        Integer messageType = pamrams.getInteger("messageType");
        Integer senderId = pamrams.getInteger("senderId");
        String title = pamrams.getString("title");
        Integer resourceType = pamrams.getInteger("resourceType");
        Integer resourceSubType = pamrams.getInteger("resourceSubType");
        Integer resourceId = pamrams.getInteger("resourceId");
        if(null == messageType || null == resourceType || null == resourceSubType | null == title){
            log.error("SEND_MSG_PARAMS_ERR"+pamrams.toJson());
            throw new ServiceException(SystemError.SEND_MSG_PARAMS_ERR);
        }
       return sendMes(messageType, senderId, resourceType, resourceSubType, resourceId, title, receiverIds);
    }

    /**
     *
     * @param messageType  1,通知，2告警
     * @param senderId    发送人id 可为空
     * @param resourceType     资源类型： 1，业务表单，2， oa表单，3：库存告警，保修告警等，
     * @param resourceSubType  资源子类型：resourceType为1时：formTypeId, 为2时，oa表单定义id，为3时，业务自定义,
     *                          业务自定义时请使用枚举
     * @param resourceId   对应的表单id  可为空
     * @param title      消息内容
     * @param receiverIds   消息接收人
     * @return
     */
    public int sendMes(Integer messageType,Integer senderId, Integer resourceType, Integer resourceSubType,
                Integer resourceId, String title, List<Integer> receiverIds) {
        if(null == messageType || null == resourceType || null == resourceSubType | null == title){
            log.error("SEND_MSG_PARAMS_ERR"+messageType+"/" + resourceType+"/"+resourceSubType+"/" +title);
            throw new ServiceException(SystemError.SEND_MSG_PARAMS_ERR);
        }

        List<Message> collect = receiverIds.stream().map(accountId -> {
            Message message = new Message();
            message.setMessageType(messageType);
            message.setReceiverId(accountId);
            message.setSenderId(senderId);
            message.setStatus(1);
            message.setMessageTitle(title);
            message.setResourceType(resourceType);
            message.setResourceSubType(resourceSubType);
            message.setResourceId(resourceId);
            message.setSendTime(new Date());
            return message;
        }).collect(Collectors.toList());
        return messageMapper.insertList(collect);

    }


}
