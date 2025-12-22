package com.hfits.system.workflow.trigger;

import com.hfits.system.workflow.domain.FlowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestTrigger implements FlowTrigger {
    private static final Logger log = LoggerFactory.getLogger(TestTrigger.class);

    public void pass(FlowInstance instance) {
        log.info("{}", instance);
    }

    public void reject(FlowInstance instance) {
        log.info("{}", instance);
    }

    public void closed(FlowInstance instance) {
        log.info("{}", instance);
    }

}
