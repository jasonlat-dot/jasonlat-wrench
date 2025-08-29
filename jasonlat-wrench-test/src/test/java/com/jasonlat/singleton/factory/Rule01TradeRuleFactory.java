package com.jasonlat.singleton.factory;

import com.jasonlat.design.framework.link.singleton.ILogicLink;
import com.jasonlat.singleton.logic.RuleLogic101;
import com.jasonlat.singleton.logic.RuleLogic102;
import lombok.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class Rule01TradeRuleFactory {

    @Resource
    private RuleLogic101 ruleLogic101;
    @Resource
    private RuleLogic102 ruleLogic102;

    public ILogicLink<String, DynamicContext, String> openLogicLink() {
        ruleLogic101.appendNext(ruleLogic102);
        return ruleLogic101;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private String age;
    }

}
