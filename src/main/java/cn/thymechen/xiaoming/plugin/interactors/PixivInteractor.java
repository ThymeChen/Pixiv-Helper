package cn.thymechen.xiaoming.plugin.interactors;

import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.user.XiaoMingUser;
import cn.thymechen.xiaoming.plugin.PixivHelper;
import cn.thymechen.xiaoming.plugin.data.artwork.ArtworkInfo;
import cn.thymechen.xiaoming.plugin.settings.PixivSetting;
import cn.thymechen.xiaoming.plugin.utils.HttpUtil;
import com.alibaba.fastjson.JSON;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.RawForwardMessage;
import net.mamoe.mirai.utils.ExternalResource;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PixivInteractor extends SimpleInteractors<PixivHelper> {

    boolean connected;
    PixivSetting setting;

    final Pattern PATH = Pattern.compile("https://i.pximg.net/c/250x250_80_a2/img-master(/img/\\d{4}/\\d{2}/\\d{2}/\\d{2}/\\d{2}/\\d{2}/\\d+_p\\d)_square1200(\\.\\w+)");

    @Override
    public void onRegister() {
        connected = plugin.isConnected();
        setting = plugin.getSetting();

        xiaoMingBot.getScheduler().runAtFixedRate(TimeUnit.SECONDS.toMillis(10), () -> {
            try {
                HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/");
                connected = true;
            } catch (IOException ignored) {
                connected = false;
            }
        });
    }

    @Filter("(搜索|搜|search)(图片|图|image) {标签}")
    @Required("pixiv.user.search")
    public void search(XiaoMingUser<?> user, @FilterParameter("标签") String tag) {
        if (!connected) {
            user.sendMessage("无法连接至 Pixiv");
            return;
        }

        user.sendMessage("正在搜索，该过程可能需要几分钟，请耐心等待...");
        xiaoMingBot.getScheduler().run(() -> {
            String json;
            try {
                json = HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/" + tag);
            } catch (IOException e) {
                user.sendMessage("搜索时出现异常\n" + e);
                getLogger().error(e.toString(), e);
                return;
            }
            json = JSON.parseObject(json).getString("body");
            json = JSON.parseObject(json).getString("illustManga");
            json = JSON.parseObject(json).getString("data");

            List<ArtworkInfo> artworks = JSON.parseArray(json, ArtworkInfo.class);

            List<ForwardMessage.Node> nodes = new ArrayList<>();

            Map<String, String> parameter = new HashMap<>();

            for (int index = 0; index < 30; index++) {
                ArtworkInfo artwork = artworks.get(index);
                try {
                    Matcher matcher = PATH.matcher(artwork.getUrl());
                    StringBuilder imageUrl = new StringBuilder("https://").append(setting.getProxy()).append("/img-original");
                    if (matcher.find()) { imageUrl.append(matcher.group(1)).append(matcher.group(2)); }

                    // 设置变量
                    parameter.put("artwork_author"  , artwork.getUserName());
                    parameter.put("artwork_tags"    , artwork.getTags().toString());
                    parameter.put("artwork_url"     , "https://www.pixiv.net/artworks/" + artwork.getId());
                    parameter.put("artwork_image"   , imageUrl.toString());
                    parameter.put("artwork_pid"     , artwork.getId());
                    parameter.put("artwork_title"   , artwork.getTitle());

                    // 构建模板
                    StrSubstitutor strSubstitutor = new StrSubstitutor(parameter);
                    strSubstitutor.setVariablePrefix("{");
                    strSubstitutor.setVariableSuffix("}");

                    ForwardMessage.Node node = new ForwardMessage.Node(xiaoMingBot.getMiraiBot().getId(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                            new PlainText(strSubstitutor.replace(setting.getArtwork())));
                    ForwardMessage.Node imageNode;
                    try (InputStream inputStream = new URL(imageUrl.toString()).openStream();
                         ExternalResource externalResource = ExternalResource.create(inputStream)) {
                        imageNode = new ForwardMessage.Node(xiaoMingBot.getMiraiBot().getId(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                ExternalResource.uploadAsImage(externalResource, xiaoMingBot.getContactManager().getBotPrivateContact().getMiraiContact()));
                    } catch (Exception e) {
                        imageNode = new ForwardMessage.Node(xiaoMingBot.getMiraiBot().getId(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                new PlainText("【无法加载的图片】"));
                    }

                    if (index == 0) { nodes.add(new ForwardMessage.Node(xiaoMingBot.getMiraiBot().getId(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(), new PlainText("ヾ( ﾟ▽ﾟ)ノ图片找到啦~"))); }
                    nodes.add(node);
                    nodes.add(imageNode);

                    parameter.clear();
                } catch (Exception ignored) {
                }
            }
            ForwardMessage forwardMessage = new RawForwardMessage(nodes).render(ForwardMessage.DisplayStrategy.Default);

            user.sendMessage(forwardMessage);
        });
    }
}
