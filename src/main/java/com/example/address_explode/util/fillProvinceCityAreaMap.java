package com.example.address_explode.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class fillProvinceCityAreaMap {
    public static void fillProvinceCityAreaMap(HashMap hashMap, String[] strs) {

        String sheng = strs[1];
        String city_name = strs[2];
        String area_name = strs[3];
        String latitude = strs[4];
        String Longitude = strs[5];

        if (sheng.endsWith("省") || sheng.endsWith("市")) {
            sheng = sheng.substring(0, sheng.length() - 1);
        } else if (sheng.equals("新疆维吾尔自治区")) {
            sheng = "新疆";
        } else if (sheng.equals("内蒙古自治区")) {
            sheng = "内蒙古";
        } else if (sheng.equals("西藏自治区")) {
            sheng = "西藏";
        } else if (sheng.equals("广西壮族自治区")) {
            sheng = "广西";
        } else if (sheng.equals("宁夏回族自治区")) {
            sheng = "宁夏";
        } else if (sheng.equals("香港特别行政区")) {
            sheng = "香港";
        } else if (sheng.equals("澳门特别行政区")) {
            sheng = "澳门";
        }


        if (city_name.endsWith("市")) {
            city_name = city_name.substring(0, city_name.length() - 1);
        } else if (city_name.equals("香港特别行政区")) {
            city_name = "香港";
        } else if (city_name.equals("澳门特别行政区")) {
            city_name = "澳门";
        }


        if (area_name.endsWith("市")) {
            area_name = area_name.substring(0, area_name.length() - 1);
        }


        String sheng_city_area_map = sheng + "," + city_name + "," + area_name + "," + latitude + "," + Longitude;


        sheng = sheng + "," + "" + "," + "";
        city_name = "" + "," + city_name + "," + "";
        area_name = "" + "," + "" + "," + area_name;


        if (!hashMap.containsKey(sheng)) {
            hashMap.put(sheng, sheng_city_area_map);
        }

        if (!hashMap.containsKey(city_name)) {
            hashMap.put(city_name, sheng_city_area_map);
        }

        if (!hashMap.containsKey(area_name)) {
            hashMap.put(area_name, sheng_city_area_map);
        }
    }

    public static String addCity(String text) {
        if (StringUtils.isNotBlank(text)) {
            if ((text.length() > 4 && text.endsWith("州")) || (text.length() > 4 && text.endsWith("区")) || (text.length() >= 3 && text.contains("州"))) {
                return text;
            } else {
                return text + "市";
            }
        } else {
            return "";
        }
    }

    public static String addProvince(String text) {
        if (StringUtils.isNotBlank(text)) {
            if (text.contains("重庆") || text.contains("天津") || text.contains("上海") || text.contains("北京")) {
                return text + "市";
            } else if (text.contains("新疆")) {
                return "新疆维吾尔自治区";
            } else if (text.contains("内蒙古")) {
                return "内蒙古自治区";
            } else if (text.contains("西藏")) {
                return "西藏自治区";
            } else if (text.contains("广西")) {
                return "广西壮族自治区";
            } else if (text.contains("宁夏")) {
                return "宁夏回族自治区";
            } else if (text.contains("香港")) {
                return "香港特别行政区";
            } else if (text.contains("澳门")) {
                return "澳门特别行政区";
            } else {
                return text + "省";
            }
        } else {
            return "";
        }
    }
}
