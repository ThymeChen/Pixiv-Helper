package cn.thymechen.xiaoming.plugin.settings;

import cn.chuanwise.toolkit.preservable.AbstractPreservable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PixivSetting extends AbstractPreservable {
    String cookie = "请在此处填写 Pixiv 的 cookie";
    String proxy = "i.pixiv.re";
    String artwork = "画师: {artwork_author}\n" +
            "标签: {artwork_tags}\n" +
            "详情页: {artwork_url}\n";
//            "图片链接: {artwork_image}\n";
}
