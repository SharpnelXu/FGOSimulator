package yome.fgo.data.writer.creaftessences;

import yome.fgo.data.proto.FgoStorageData.CraftEssenceData;

import static yome.fgo.data.writer.DataWriter.writeCraftEssence;

public class CraftEssence1080 {
    public static void main(final String[] args) {
        final String id = "craftEssence1080";
        final CraftEssenceData craftEssenceData = CraftEssenceData.newBuilder()
                .setId(id)
                .build();
        writeCraftEssence(craftEssenceData);
    }
}
