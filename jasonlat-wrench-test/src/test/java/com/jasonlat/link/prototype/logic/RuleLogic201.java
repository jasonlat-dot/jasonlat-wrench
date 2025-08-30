package com.jasonlat.link.prototype.logic;


import com.jasonlat.design.framework.link.prototype.handler.ILogicHandler;
import com.jasonlat.link.prototype.factory.Rule02TradeRuleFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Jasonlat
 * @create 2025-08-29
 */
@Slf4j
@Service
public class RuleLogic201 implements ILogicHandler<String, Rule02TradeRuleFactory.DynamicContext, XxxResponse> {

    public XxxResponse apply(String requestParameter, Rule02TradeRuleFactory.DynamicContext dynamicContext) {

        log.info("link model02 RuleLogic201");

        return next(requestParameter, dynamicContext);
    }

}
