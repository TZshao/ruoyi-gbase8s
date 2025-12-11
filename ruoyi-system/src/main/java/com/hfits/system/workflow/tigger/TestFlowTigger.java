package com.hfits.system.workflow.tigger;

import com.hfits.system.workflow.domain.FlowInstance;
import org.springframework.stereotype.Component;

@Component
public class TestFlowTigger {

    public void appleyPass(FlowInstance instance) {
        System.out.println(instance);
    }

    public void applyReject(FlowInstance instance) {
        System.out.println(instance);
    }
}
