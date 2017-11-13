package com.tghoul.domain;

import com.tghoul.proxy.AbstractDownloaderProxy;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.FpUtils;
import sun.security.krb5.internal.PAEncTSEnc;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.awt.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zpj
 * @date 2017/11/6 17:40
 */
public class S91RepoPageProcessor implements PageProcessor {
    /** 日志 */
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Site site = Site
            .me()
            //重试次数
            .setRetryTimes(10)
            .setCycleRetryTimes(10)
            //抓取间隔
            .setSleepTime(3000);

    @Override
    public void process(Page page) {
        //url地址
        List<String> videoUrls = new ArrayList<>(20);
        for (int pageNo = 1; pageNo < 10; pageNo++) {
            page.addTargetRequest("http://91.91p18.space/v.php?next=watch&page=" + pageNo);
            videoUrls.addAll(page.getHtml()
                    .xpath("//div[@id='videobox']/table/tbody/tr/td/div[@class='listchannel']/a")
                    .links()
                    .regex("http://91\\.91p18\\.space/view_video\\.php.*")
                    .all());
        }

        if (CollectionUtils.isNotEmpty(videoUrls)) {
            page.addTargetRequests(videoUrls);
        }

        if (page.getUrl().toString().contains("view_video")) {
            page.putField("videoUrl", page.getHtml().xpath("//video[@id='vid']/source/@src").get());
            LOGGER.info("Video Url ---------- {}", page.getHtml().xpath("//video[@id='vid']/source/@src").get());
            LOGGER.info("Request ---------- {}",page.getRequest().toString());
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        //使用代理类定制化downloader
        AbstractDownloaderProxy downloaderProxy = new AbstractDownloaderProxy(new HttpClientDownloader());
        Spider.create(new S91RepoPageProcessor())
              .addUrl("http://91.91p18.space/v.php?next=watch")
              .setDownloader(downloaderProxy)
              .addPipeline(new JsonFilePipeline("D:\\webmagic\\"))
              .thread(5)
              .run();
    }
}