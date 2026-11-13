package com.pgmmers.radar.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;


/**
 * CaptchaUtil class.
 *
 * @author feihu.wang
 */
public class CaptchaUtil {
    
    private Logger logger = LoggerFactory.getLogger(CaptchaUtil.class);
    private Random random = new Random();
    private String randString = "23456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
    private int width = 140;
    private int height = 30;
    private int lineSize = 40;
    private int stringNum = 4;

    /**
     * 获得字体.
     *
     * @return a {@link Font} object.
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, 18);
    }

    /**
     * <p>获得颜色.</p>
     *
     * @param fc a {@link Integer} object.
     * @param bc a {@link Integer} object.
     * @return a {@link Color} object.
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }

        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }


    /**
     *
     *
     * @return captcha
     */
    public Captcha genRandcode() {
        Captcha captcha = new Captcha();
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }
        // 绘制随机字符
        String randomString = "";
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }

        g.dispose();
        logger.info("curr captcha:{}", randomString);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", out);
            captcha.setCaptcha(randomString);
            captcha.setContents(out.toByteArray());
        } catch (Exception e) {
            logger.error("create register rand code error" , e);
            captcha.setCaptcha("");
        }
        return captcha;
    }

    /**
     * 绘制字符串.
     *
     * @param g a {@link Integer} object.
     * @param randomString a {@link Integer} object.
     * @return a {@link Color} object.
     */
    private String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random
                .nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(randString
                .length())));
        randomString += rand;
        g.translate(random.nextInt(25), random.nextInt(5));
        g.drawString(rand, 13 * i, 16);
        return randomString;
    }

    /**
     * 绘制干扰线.
     *
     * @param g a {@link Graphics} object.
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /**
     * 获取随机的字符.
     *
     * @param num a {@link Integer} object.
     * @return a {@link String} object.
     */
    public String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }

    public class Captcha {

        private String captcha;

        private byte[] contents;

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
        }

        public byte[] getContents() {
            return contents;
        }

        public void setContents(byte[] contents) {
            this.contents = contents;
        }
    }
}

