package cn.thymechen.xiaoming.plugin.data.search;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchBody {
    private IllustManga illustManga;
    private Popular popular;
    private List<String> relatedTags;
    private Map<String, Map<String, String>> tagTranslation;
    private ZoneConfig zoneConfig;
    private ExtraData extraData;

    @Data
    public static class IllustManga {
        private List<ArtworkData> data;
        private int total;
        private List<BookmarkRanges> bookmarkRanges;

        @Data
        public static class BookmarkRanges {
            private String min;
            private String max;
        }
    }

    @Data
    static class Popular {
        private List<ArtworkData> recent;
        private List<ArtworkData> permanent;
    }

    @Data
    static class ZoneConfig {
        private Map<String, String> header;
        private Map<String, String>  footer;
        private Map<String, String>  infeed;
    }

    @Data
    static class ExtraData {
        private Meta meta;

        @Data
        public static class Meta {
            private String title;
            private String description;
            private String canonical;
            private Map<String, String> alternateLanguages;
            private String descriptionHeader;
        }
    }
}
