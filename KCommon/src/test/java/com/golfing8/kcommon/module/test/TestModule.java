package com.golfing8.kcommon.module.test;

import com.golfing8.kcommon.config.lang.LangEnum;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;

@ModuleInfo(
        name = "test"
)
public class TestModule extends Module {

    public enum Messages implements LangEnum {
        TEST_MESSAGE(new Message("Some test message"))
        ;
        private Message message;
        Messages(Message message) {
            this.message = message;
        }

        @Override
        public Message getMessage() {
            return message;
        }

        @Override
        public void setMessage(Message message) {
            this.message = message;
        }
    }
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
