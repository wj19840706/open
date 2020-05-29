package com.feida.ams.api.pc.process.bizInterace;

import com.feida.ams.api.pc.asset.spareDevice.outForUse.processInterface.OutForUserProcessInterface;
import com.feida.common.util.SpringUtil;

public class BizInterfaceFactory {
    public static BizInterface getInstance(Integer formTypeId){
        switch (formTypeId){
            case 1:
                return SpringUtil.getBean(OutForUserProcessInterface.class);
                default:
        }

        return null;
    }
}
