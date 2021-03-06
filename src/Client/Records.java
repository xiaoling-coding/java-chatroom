package Client;

import Client.Constants.LinkPrefix;
import Client.Utils.ByteUtils;
import Client.Utils.FileUtils;
import Model.Message;
import Model.User;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EscapeUtil;
import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Records {
    String baseHtml = "<html>"
            + "<body style='font-size:14px;word-wrap:break-word;white-space:normal;'></body>"
            + "</html>";
    public Document content = Jsoup.parse(baseHtml);
    Elements body = content.getElementsByTag("body");

    private Element genUserA(User user, boolean buildA) {
        Element a = new Element("a");
        String userName = user.userName;
        a.attr("style", "font-weight:bold");
        if (user.userName.equals(CurrUser.getInstance().getUserName())) {
            userName += " (我)";
            a.attr("style", a.attr("style") + ";color:#00bea9");
        } else {
            if (buildA)
                a.attr("href", LinkPrefix.USER + JSON.toJSONString(user));
        }
        a.append(EscapeUtil.escapeHtml4(userName));
        return a;
    }

    private Element genUserA(User user) {
        return genUserA(user, true);
    }

    private String genHeader(Message msg) {
        return genHeader(msg, true);
    }

    private String genHeader(Message msg, boolean buildA) {
        Element span = new Element("span");
        span.attr("style", "font-weight:bold; color: blue");
        span.append("[" + DateUtil.format(new Date(msg.timeStamp), "yyyy-MM-dd HH:mm:ss") + "]");
        return span.toString() + genUserA(msg.fromUser, buildA).toString();
    }

    public String parseText(Message msg, boolean buildA) {
        String text = EscapeUtil.escapeHtml4(msg.msg.replace("\n", "<br/>"));
        String build = genHeader(msg, buildA) +
                "：<br /><p style='font-size:16px;margin-top:3px;'>" + text + "</p><br />";
        body.append(build);
        return content.toString();
    }

    public String parseText(Message msg) {
        return parseText(msg, true);
    }

    public String parseJoinOrLeft(Message msg) {
        String type = msg.msg.equals("left") ? "离开" : "加入";

        String build = genHeader(msg, !msg.msg.equals("left")) + "<span style='font-weight:bold; color: red'>" + type + "了聊天室。</span><br />";
        body.append(build);
        return content.toString();
    }

    public String parseOnlineUsers(Message msg) {
        String html = baseHtml;
        Document c = Jsoup.parse(html);
        Elements b = c.getElementsByTag("body");
        b.attr("style", "color:#192e4d;font-size:10px;word-wrap:break-word;white-space:normal;");
        Element ul = new Element("ul");
        b.append("当前在线 (" + msg.users.size() + "人) ：");
        ul.attr("style", "font-size:13px;padding:0;margin:14");
        for (User user : msg.users) {
            Element li = new Element("li");
            Element a = genUserA(user);
            li.appendChild(a);
            ul.appendChild(li);
        }
        b.append(ul.toString());
        return c.toString();
    }

    public String parseImg(Message msg, JFrame frame) throws Exception {
        Path tempDir = FileUtils.getTempDirectory();
        if (tempDir == null) {
            throw new Exception("tempDir is null");
        }
        byte[] imgSrc = ByteUtils.decodeBase64StringToByte(msg.msg);
        imgSrc = ByteUtils.unGZip(imgSrc);
        URI path = Files.write(Paths.get(tempDir + "/" + msg.filename), imgSrc).toAbsolutePath().toUri();
        Element a = new Element("a");
        a.attr("href", LinkPrefix.IMAGE + path);
        Element img = new Element("img");
        img.attr("src", String.valueOf(path));
        img.attr("alt", "无法显示图片");
        img.attr("style", "height:auto;");
        img.attr("width", String.valueOf((frame.getSize().width / 2)));
        a.appendChild(img);

        String build = genHeader(msg) +
                "：<br /><div style='font-size:16px;margin-top:3px;'>" +
                a +
                "</div><br />";
        body.append(build);
        return content.toString();
    }

    public String parseFile(Message msg) {
        Element a = new Element("a");
        a.attr("style", "font-weight:bold");
        a.attr("href", LinkPrefix.FILE + msg.filename + ";" + msg.msg);
        a.append(msg.filename);
        String build = genHeader(msg) +
                "：<br /><div style='font-size:20px;margin-top:3px;'>" +
                a.toString() +
                "</div><br />";
        body.append(build);
        return content.toString();
    }
}
