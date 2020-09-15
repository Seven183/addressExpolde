package com.example.address_explode.util;

public class replaceAddress {
    public static String replaceAddress(String location_str) {
        String location_strs = location_str;
        if (location_str.startsWith("中国") || location_str.contains("（") || location_str.contains("）") || location_str.contains("自由贸易试验区") || location_str.contains("伊犁州")) {
            location_strs = location_strs.replace("中国", "")
                    .replaceAll("（", "")
                    .replaceAll("）", "")
                    .replaceAll("\\)", "")
                    .replaceAll("\\(", "");
//                    .replace("伊犁州", "伊犁哈萨克自治州");
        }
        if (!(location_strs.startsWith("上海") || location_strs.startsWith("重庆") ||
                location_strs.startsWith("天津") || location_strs.startsWith("北京"))
                && (location_strs.contains("自由贸易试验区") || location_strs.contains("经济技术开发区"))) {
            location_strs = location_strs.replaceAll("自由贸易试验区", "").replaceAll("经济技术开发区", "");
        }
        if (location_str.startsWith("新疆维吾尔自治区")) {
            location_strs = location_str.replace("新疆维吾尔自治区", "新疆省");
        }
        if (location_str.startsWith("内蒙古自治区")) {
            location_strs = location_str.replace("内蒙古自治区", "内蒙古省");
        }
        if (location_str.startsWith("西藏自治区")) {
            location_strs = location_str.replace("西藏自治区", "西藏省");
        }
        if (location_str.startsWith("广西壮族自治区")) {
            location_strs = location_str.replace("广西壮族自治区", "广西省");
        }
        if (location_str.startsWith("宁夏回族自治区")) {
            location_strs = location_str.replace("宁夏回族自治区", "宁夏省");
        }
        if (location_str.startsWith("香港特别行政区")) {
            location_strs = location_str.replace("香港特别行政区", "香港省");
        }
        if (location_str.startsWith("澳门特别行政区")) {
            location_strs = location_str.replace("澳门特别行政区", "澳门省");
        }
        return location_strs;
    }
}
