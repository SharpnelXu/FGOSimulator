package yome.fgo.simulator.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslationManagerTest {
    @Test
    public void testGet() {
        TranslationManager.setTranslations("zh_CN");
        assertEquals("警告：未登记的特性：", TranslationManager.getTranslation(TranslationManager.APPLICATION_SECTION, "Warning: unmapped traits:"));
    }
}
