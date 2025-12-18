package com.hfits.system.workflow.trigger;

import com.hfits.system.workflow.domain.FlowInstance;
import org.springframework.stereotype.Component;

@Component
public class TestTrigger implements FlowTrigger {

    public void pass(FlowInstance instance) {
        aaa("pass");
        System.out.println(instance);
        aaa("pass");
    }

    public void reject(FlowInstance instance) {
        aaa("reject");
        System.out.println(instance);
        aaa("reject");
    }

    public void closed(FlowInstance instance) {
        aaa("closed");
        System.out.println(instance);
        aaa("closed");
    }

    private void aaa(String s) {
        String ss = "";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                ss += s;
            }
            ss += "\n";
        }
        System.out.println(ss);
    }
}
