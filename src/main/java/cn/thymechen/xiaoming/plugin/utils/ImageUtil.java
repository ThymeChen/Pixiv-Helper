package cn.thymechen.xiaoming.plugin.utils;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;

public class ImageUtil {
    public static Image getMiraiImage(InputStream inputStream, Contact contact) {
        try(ExternalResource externalResource = ExternalResource.create(inputStream)) {
            return ExternalResource.uploadAsImage(externalResource, contact);
        } catch (Exception e) {
            return null;
        }
    }
}
