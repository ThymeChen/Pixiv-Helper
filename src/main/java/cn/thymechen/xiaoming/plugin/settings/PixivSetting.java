package cn.thymechen.xiaoming.plugin.settings;

import cn.chuanwise.toolkit.preservable.AbstractPreservable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class PixivSetting extends AbstractPreservable {

    String cookie = "";
    String proxy = "i.pixiv.re";
    Search search = new Search();
    Rank rank = new Rank();

    @Data
    public static class Search {
        Map<String, String> _artworkDescription = new LinkedHashMap<>();
        String artworkDescription = "   {artwork_title}\n" +
                "画师: {artwork_author_name}\n" +
                "标签: {artwork_tags}\n" +
                "详情页: {artwork_url}";
    }

    @Data
    public static class Rank {
        Map<String, String> _artworkDescription = new LinkedHashMap<>();
        String artworkDescription = "   {artwork_title}\n" +
                "画师: {artwork_author_name}\n" +
                "详情页: {artwork_url}";
        Push push = new Push();

        @Data
        public static class Push {
            String time = "12:30";
            List<Long> group = new ArrayList<>();
        }
    }
}
