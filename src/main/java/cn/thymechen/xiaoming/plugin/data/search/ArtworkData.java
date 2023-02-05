package cn.thymechen.xiaoming.plugin.data.search;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArtworkData {
    private String id;
    private String title;
    private int illustType;
    private int xRestrict;
    private int restrict;
    private int sl;
    private String url;
    private String description;
    private List<String> tags;
    private String userId;
    private String userName;
    private int width;
    private int height;
    private int pageCount;
    private boolean isBookmarkable;
    private String bookmarkData;
    private String alt;
    private TitleCaptionTranslation titleCaptionTranslation;
    private Date createDate;
    private Date updateDate;
    private boolean isUnlisted;
    private boolean isMasked;
    private int aiType;
    private String profileImageUrl;
}
