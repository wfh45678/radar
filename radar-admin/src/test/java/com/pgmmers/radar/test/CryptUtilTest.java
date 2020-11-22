package com.pgmmers.radar.test;
import com.pgmmers.radar.util.CryptUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CryptUtilTest {


    @Test
    public void testSHA() {
        String str = CryptUtils.sha("123456", "admin").toUpperCase();
        System.out.println(str);
    }

}
