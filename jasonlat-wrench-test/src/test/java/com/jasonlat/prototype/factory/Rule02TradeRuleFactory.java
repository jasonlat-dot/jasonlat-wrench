package com.jasonlat.prototype.factory;


import com.jasonlat.design.framework.link.prototype.LinkArmory;
import com.jasonlat.design.framework.link.prototype.chain.BusinessLinkedList;
import com.jasonlat.prototype.logic.RuleLogic201;
import com.jasonlat.prototype.logic.RuleLogic202;
import com.jasonlat.prototype.logic.XxxResponse;
import lombok.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @author Jasonlat
 * @create 2025-08-29
 */
@Service
public class Rule02TradeRuleFactory {

    @Bean("demo01")
    public BusinessLinkedList<String, DynamicContext, XxxResponse> demo01(RuleLogic201 ruleLogic201, RuleLogic202 ruleLogic202) {

        LinkArmory<String, DynamicContext, XxxResponse> linkArmory = new LinkArmory<>("demo01", ruleLogic201, ruleLogic202);

        return linkArmory.getLogicLink();
    }

    @Bean("demo02")
    public BusinessLinkedList<String, DynamicContext, XxxResponse> demo02(RuleLogic202 ruleLogic202) {

        LinkArmory<String, DynamicContext, XxxResponse> linkArmory = new LinkArmory<>("demo02", ruleLogic202);

        return linkArmory.getLogicLink();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext extends com.jasonlat.design.framework.link.prototype.DynamicContext {
        private String age;
    }

}
