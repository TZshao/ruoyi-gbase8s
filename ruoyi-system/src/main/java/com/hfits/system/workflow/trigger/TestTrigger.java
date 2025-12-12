package com.hfits.system.workflow.trigger;

import com.hfits.system.workflow.domain.FlowInstance;
import org.springframework.stereotype.Component;

@Component
public class TestTrigger implements FlowTrigger {

    public void pass(FlowInstance instance) {
        System.out.println(instance);
    }

    public void reject(FlowInstance instance) {
        System.out.println(instance);
    }

    public void closed(FlowInstance instance) {
        System.out.println(instance);
    }

}
