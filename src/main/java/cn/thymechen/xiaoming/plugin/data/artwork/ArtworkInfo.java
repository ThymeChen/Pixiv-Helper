package cn.thymechen.xiaoming.plugin.data.artwork;

import lombok.Data;

import java.util.List;

@Data
public class ArtworkInfo {
    private int pageCount;
    private String updateDate;
    private boolean isBookmarkable;
    private String alt;
    private String description;
    private int xRestrict;
    private String title;
    private int restrict;
    private String userName;
    private boolean isMasked;
    private String userId;
    private String url;
    private List<String> tags;
    private boolean isUnlisted;
    private int aiType;
    private int width;
    private int illustType;
    private int sl;
    private TitleCaptionTranslation titleCaptionTranslation;
    private String id;
    private String profileImageUrl;
    private int height;
    private String createDate;
}
