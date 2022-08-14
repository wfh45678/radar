package com.pgmmers.radar.service.impl.data;

import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.dal.data.MobileInfoDal;
import com.pgmmers.radar.service.data.MobileInfoService;
import com.pgmmers.radar.util.MobileLocationUtil;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
//@ConditionalOnProperty(prefix = "sys.conf",name="app", havingValue = "engine")
public class MobileInfoServiceImpl implements MobileInfoService {

    public static Logger logger = LoggerFactory.getLogger(MobileInfoServiceImpl.class);

    private static Map<String, MobileInfoVO> mobileMap = new HashMap<>();

    @Autowired
    private MobileInfoDal mobileDal;

    @Value("${mobile.info.path}")
    private String mobileInfoFilePath;

    @PostConstruct
    public void init() {
        logger.info("mobile info loading...");
        Long begin = System.currentTimeMillis();
        int count = 0;
        try {
            Path p = FileSystems.getDefault().getPath(mobileInfoFilePath);
            List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            List<MobileInfoVO> mobileInfoVOList = lines.stream().map(line -> {
                String[] info = line.split(",");
                MobileInfoVO mobile = new MobileInfoVO();
                if (info.length == 5) {
                    mobile.setMobile(info[0]);
                    mobile.setProvince(info[1]);
                    mobile.setCity(info[2]);
                    mobile.setSupplier(info[3]);
                    mobile.setRegionCode(info[4]);
                }
                return mobile;
            }).collect(Collectors.toList());
            for (MobileInfoVO mobileInfoVO : mobileInfoVOList) {
                mobileMap.put(mobileInfoVO.getMobile(), mobileInfoVO);
            }
            count = mobileInfoVOList.size();
        } catch (Throwable ex) {
            logger.error("", ex);
            throw new RuntimeException("mobile info load error");
        } finally {
            long cost = System.currentTimeMillis() - begin;
            logger.info("{}, mobile info cached, costs:{}", count, cost);
        }
    }

    @Override
    public MobileInfoVO getMobileInfoByMobile(String mobile) {
        MobileInfoVO vo = mobileMap.get(mobile);
        if (vo == null) {
            // 如果当前系统没有该号码段，则需要通过第三方API 获取 改号码段信息。
            new Thread(() -> {
                    String result = MobileLocationUtil.getLocation(mobile);
                    JSONObject json = JSONObject.parseObject(result);
                    String retMsg = json.getString("retMsg");
                    String province = json.getString("province");
                    String city = json.getString("city");
                    if (retMsg.equals("success")) {
                        List<MobileInfoVO> list = mobileDal.listMobileInfo(province, city, 1, 2);
                        if (list != null && list.size() > 0) {
                            MobileInfoVO info = new MobileInfoVO();
                            info.setMobile(mobile.substring(0, 7));
                            info.setProvince(list.get(0).getProvince());
                            info.setCity(list.get(0).getCity());
                            info.setSupplier(list.get(0).getSupplier());
                            info.setRegionCode(list.get(0).getRegionCode());
                            info.setCreateTime(new Date());
                            info.setUpdateTime(new Date());
                            mobileDal.save(info);
                            mobileMap.put(info.getMobile(), info);
                        }
                    }
            });//.start();
        }
        return vo;
    }

}
