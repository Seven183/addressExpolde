package com.example.address_explode;

import com.example.address_explode.controller.addressExplode;
import com.example.address_explode.entity.UeMap;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.example.address_explode.util.fillProvinceCityAreaMap.*;
import static com.example.address_explode.util.myMap.*;
import static com.example.address_explode.util.replaceAddress.replaceAddress;

@SpringBootTest
class AddressApplicationTests {

    HashMap<String, String> province_city_area_map = new HashMap<String, String>();


    @Test
    void contextLoads() {
        System.out.println(getAddress("新疆乌鲁木齐市天山区京东卡角度看"));
//        System.out.println(getAddress("湖北省武汉市江岸区工农兵路99号昌盛小区7号楼3单元8层3号"));
//        System.out.println(getAddress("云南省昆明市滇池度假区广福路南悦城1栋1502号"));
//        System.out.println(getAddress("北京市朝阳区新源南路1-3号平安国际金融中心B座15层"));
    }


    public String getAddress(String address) {
        InputStream resourceAsStream = addressExplode.class.getClassLoader().getResourceAsStream("data/pca.csv");
        Scanner scanner = new Scanner(resourceAsStream);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            String[] strs = s.split(",");
            fillProvinceCityAreaMap(province_city_area_map, strs);
        }

        String location_strs = "";
        if (address.length() > 4) {
            location_strs = replaceAddress(address);
            if (location_strs.substring(0, 3).contains("省") || location_strs.startsWith("上海市") || location_strs.startsWith("重庆市") || location_strs.startsWith("天津市") || location_strs.startsWith("北京市")) {
                return transform(location_strs, 8, province_city_area_map);
            } else if (location_strs.startsWith("新疆") || location_strs.startsWith("内蒙") || location_strs.startsWith("西藏") || location_strs.startsWith("广西") || location_strs.startsWith("宁夏") || location_strs.startsWith("香港") || location_strs.startsWith("澳门")) {
                return transform(location_strs, 7, province_city_area_map);
            } else if (location_strs.substring(0, 3).contains("市") && (location_strs.substring(3, 6).contains("市") || location_strs.substring(3, 6).contains("区") || location_strs.substring(3, 6).contains("县"))) {
                return transform(location_strs, 6, province_city_area_map);
            } else {
                return transform(location_strs, 3, province_city_area_map);
            }
        } else {
            return "//";
        }
    }

    public String transform(String location_strs, int lookahead, HashMap province_city_area_map) {

        UeMap ueMap = repalceAddress(extractAddress(location_strs, lookahead));

        String address = addProvince(ueMap.getProvince()) + "/" + addCity(ueMap.getCity()) + "/" + ueMap.getDistinct() + "/" + ueMap.getLatitude() + "/" + ueMap.getLongitude();
        if (address.contains("重庆") || address.contains("天津") || address.contains("上海") || address.contains("北京")) {
            String[] splited = address.split("/");
            if (splited[0].length() > 0 && splited[1].length() == 0) {
                return splited[0] + "/" + splited[0] + "/" + splited[2] + "/" + splited[3] + "/" + splited[4] + "/" + location_strs;
            }
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ueMap.getDistinct()) && location_strs.contains(ueMap.getDistinct())) {
            int i = location_strs.indexOf(ueMap.getDistinct());
            return address + "/" + location_strs.substring(i + ueMap.getDistinct().length());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ueMap.getCity()) && location_strs.contains(addCity(ueMap.getCity()))) {
            int i = location_strs.indexOf(addCity(ueMap.getCity()));
            return address + "/" + location_strs.substring(i + addCity(ueMap.getCity()).length());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ueMap.getProvince()) && location_strs.contains(addProvince(ueMap.getProvince()))) {
            int i = location_strs.indexOf(addProvince(ueMap.getProvince()));
            return address + "/" + location_strs.substring(i + addProvince(ueMap.getProvince()).length());
        }
        return address + "/" + location_strs;
    }


    public UeMap extractAddress(String addr1, int lookahead) {
        try {
            UeMap ueMap = new UeMap();
            int i = 0;
            while (i < addr1.length()) {
                for (int x = 1; x < lookahead + 1; x++) {
                    String elem = addr1.substring(i, i + x);
                    if (province_city_area_map.containsKey(elem + "," + "" + "," + "")) {
                        for (int y = 1; y < lookahead + 1; y++) {
                            if (elem.startsWith("北京") || elem.startsWith("重庆") || elem.startsWith("上海") || elem.startsWith("天津")) {
                                String elem_city = addr1.substring(elem.length() + 1, y + elem.length() + 1);
                                //这种情况是只有市区的解析 如 北京市朝阳区
                                if (province_city_area_map.containsKey("" + "," + "" + "," + elem_city)) {
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[3]);
                                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[2]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[0]);
                                    return ueMap;
                                }
                            }
                            //这种判断省份后面有省字的情况
                            if (addr1.substring(0, 4).contains("省")) {
                                String elem_city = addr1.substring(i + elem.length() + 1, i + y + elem.length() + 1);
                                //这种情况是正常的省市区解析
                                //这个是包含市 例如 XX省XX（市）XX县
                                if (province_city_area_map.containsKey("" + "," + elem_city + "," + "")) {
                                    for (int z = 1; z < lookahead + 1; z++) {
                                        if (addr1.substring(0, 8).contains("市")) {
                                            //判断完市，接着判断区
                                            String elem_area = addr1.substring(elem.length() + 1 + elem_city.length() + 1, z + elem.length() + 1 + elem_city.length() + 1);
                                            if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                                ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                                ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                                ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                                ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                                ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                                return ueMap;
                                            }
                                        } else {
                                            String elem_area = addr1.substring(elem.length() + 1 + elem_city.length(), z + elem.length() + 1 + elem_city.length());
                                            if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                                ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                                ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                                ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                                ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                                ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                                return ueMap;
                                            }
                                        }
                                    }
                                    //没有区就返回市级对应的信息 例如xx省xx市
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[3]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[0]);
                                    return ueMap;
                                }
                                //这个是不包含市 例如 XX省XX县
                                if (province_city_area_map.containsKey("" + "," + "" + "," + elem_city)) {
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[3]);
                                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[2]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[0]);
                                    return ueMap;
                                }
                                //这种判断省份后面没有省字的情况
                            } else {
                                String elem_city = addr1.substring(elem.length(), y + elem.length());
                                if (province_city_area_map.containsKey("" + "," + elem_city + "," + "")) {
                                    for (int z = 1; z < lookahead + 1; z++) {
                                        //这种情况是xxyy市看看又没有县级
                                        if (addr1.substring(0, 7).contains("市")) {
                                            String elem_area = addr1.substring(elem.length() + elem_city.length() + 1, z + elem.length() + elem_city.length() + 1);
                                            if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                                ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                                ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                                ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                                ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                                ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                                return ueMap;
                                            }
                                            //这种情况是xxyy看看又没有县级
                                        } else {
                                            String elem_area = addr1.substring(elem.length() + elem_city.length(), z + elem.length() + elem_city.length());
                                            if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                                ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                                ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                                ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                                ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                                ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                                return ueMap;
                                            }
                                        }
                                    }
                                    //这种情况是xxyy（市）没有县级直接返回市级单位
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[3]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + elem_city + "," + "").split(",")[0]);
                                    return ueMap;
                                }
                                //这个是不包含市 例如 XX省XX县
                                if (province_city_area_map.containsKey("" + "," + "" + "," + elem_city)) {
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[3]);
                                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[2]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_city).split(",")[0]);
                                    return ueMap;
                                }
                            }
                        }
                        //最终市级也没有信息，只返回一个省级信息
                        ueMap.setProvince(province_city_area_map.get(elem + "," + "" + "," + "").split(",")[0]);
                        return ueMap;
                        //这种情况是没有省的情况，例如xx(市)yy县
                    } else if (province_city_area_map.containsKey("" + "," + elem + "," + "")) {
                        for (int y = 1; y < lookahead + 1; y++) {
                            //这种情况是有“市字”的情况，例如xx市yy县
                            if (addr1.substring(0, 4).contains("市")) {
                                String elem_area = addr1.substring(elem.length() + 1, y + elem.length() + 1);
                                if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                    return ueMap;
                                }
                                //这种情况是没有“市字”的情况，例如xxyy县
                            } else {
                                String elem_area = addr1.substring(elem.length(), y + elem.length());
                                if (province_city_area_map.containsKey("" + "," + "" + "," + elem_area)) {
                                    ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[4]);
                                    ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[3]);
                                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[2]);
                                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[1]);
                                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem_area).split(",")[0]);
                                    return ueMap;
                                }
                            }
                        }
                        ueMap.setLongitude(province_city_area_map.get("" + "," + elem + "," + "").split(",")[4]);
                        ueMap.setLatitude(province_city_area_map.get("" + "," + elem + "," + "").split(",")[3]);
                        ueMap.setCity(province_city_area_map.get("" + "," + elem + "," + "").split(",")[1]);
                        ueMap.setProvince(province_city_area_map.get("" + "," + elem + "," + "").split(",")[0]);
                        return ueMap;
                        //这种情况是没有省也没有市的情况的情况，例如yy县 直接返回yy县的直接上级省市
                    } else if (province_city_area_map.containsKey("" + "," + "" + "," + elem)) {
                        ueMap.setLongitude(province_city_area_map.get("" + "," + "" + "," + elem).split(",")[4]);
                        ueMap.setLatitude(province_city_area_map.get("" + "," + "" + "," + elem).split(",")[3]);
                        ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + elem).split(",")[2]);
                        ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + elem).split(",")[1]);
                        ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + elem).split(",")[0]);
                        return ueMap;
                    }
                }
                i = addr1.length();
            }
            return ueMap;
        } catch (Exception e) {
            return new UeMap();
        }
    }

    public UeMap repalceAddress(UeMap ueMap) {

        //上边是正常的信息，下边是对上面返回的信息进一步确认有没有重名地区，有的话单独映射
        if (StringUtils.isNotBlank(ueMap.getDistinct())) {
            if (ueMap.getDistinct().contains("区")) {
                String key1 = ueMap.getDistinct();
                if (myDistinctMap.containsKey(key1)) {
                    String value = myDistinctMap.get(key1).replace("市", "");
                    if (province_city_area_map.containsKey("" + "," + value + "," + "")) {
                        ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + key1).split(",")[2]);
                        ueMap.setCity(province_city_area_map.get("" + "," + value + "," + "").split(",")[1]);
                        ueMap.setProvince(province_city_area_map.get("" + "," + value + "," + "").split(",")[0]);
                        return ueMap;
                    }
                }
            } else {
                String key1 = ueMap.getDistinct() + "区";
                if (myDistinctMap.containsKey(key1)) {
                    String value = myDistinctMap.get(key1).replace("市", "");
                    if (province_city_area_map.containsKey("" + "," + value + "," + "")) {
                        ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + key1).split(",")[2]);
                        ueMap.setCity(province_city_area_map.get("" + "," + value + "," + "").split(",")[1]);
                        ueMap.setProvince(province_city_area_map.get("" + "," + value + "," + "").split(",")[0]);
                        return ueMap;
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(ueMap.getDistinct())) {
            if (ueMap.getDistinct().contains("县")) {
                String key1 = ueMap.getDistinct();
                if (myMap.containsKey(key1)) {
                    String value = myMap.get(key1);
                    ueMap.setDistinct(province_city_area_map.get("" + "," + "" + "," + value).split(",")[2]);
                    ueMap.setCity(province_city_area_map.get("" + "," + "" + "," + value).split(",")[1]);
                    ueMap.setProvince(province_city_area_map.get("" + "," + "" + "," + value).split(",")[0]);
                    return ueMap;
                }
            }
        }
        return ueMap;
    }
}

