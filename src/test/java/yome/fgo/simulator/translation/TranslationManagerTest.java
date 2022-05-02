package yome.fgo.simulator.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yome.fgo.simulator.translation.TranslationManager.APPLICATION_SECTION;
import static yome.fgo.simulator.translation.TranslationManager.getTranslation;

public class TranslationManagerTest {
    @Test
    public void testGet() {
        TranslationManager.setTranslations("zh_CN");
        assertEquals("警告：未登记的特性：", getTranslation(APPLICATION_SECTION, "Warning: unmapped traits:"));
        assertEquals("选择条件类型以开始", getTranslation(APPLICATION_SECTION, "Select a condition type to start"));
    }
}
