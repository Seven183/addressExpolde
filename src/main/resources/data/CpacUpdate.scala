package com.lny.utils

import java.io.InputStream
import java.util
import java.util.Scanner

import com.huaban.analysis.jieba.JiebaSegmenter
import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

class CpacUpdate {
  var map: mutable.HashMap[String, String] = new mutable.HashMap[String, String]()

  map.put("省", "")
  map.put("市", "")
  map.put("区", "")

  def setKV(k: String, v: String): Unit = {
    map.put(k, v)
  }

  def getV(k: String) = {
    map(k)
  }
}

object CpacUpdate {
  //自定义的区级到市级的映射,主要用于解决区重名问题,如果定义的映射在模块中已经存在，则会覆盖模块中自带的映射
  val myumap: Map[String, String] = Map(
    "南关区" -> "长春市",
    "南山区" -> "深圳市",
    "宝山区" -> "上海市",
    "市辖区" -> "东莞市",
    "普陀区" -> "上海市",
    "朝阳区" -> "北京市",
    "河东区" -> "天津市",
    "白云区" -> "广州市",
    "西湖区" -> "杭州市",
    "铁西区" -> "沈阳市",
    "兴宁区" -> "南宁市",
    "海城区" -> "北海市",
    "东港区" -> "日照市"
  )

  var province_city_area_map: util.HashMap[(String, String, String), (String, String, String)] = new util.HashMap[(String, String, String), (String, String, String)]
  var latlng: util.HashMap[String, String] = new util.HashMap[String, String]()


  def runner(addr: String): String = {
    //加载词库
    val arr = new ArrayBuffer[String]()
    val path: InputStream = Cpca.getClass.getClassLoader.getResourceAsStream("data/pca.csv")
    val scanner = new Scanner(path)
    //将省市区做映射
    while (scanner.hasNext()) {
      val str: String = scanner.nextLine()
      _data_from_csv(str)
    }
    transform(addr, myumap)
  }

  def _data_from_csv(str: String) = {
    val strs: Array[String] = str.split(",")
    latlng.put(strs(1) + "," + strs(2) + "," + strs(3), strs(4) + "," + strs(5))
    _fill_province_city_area_map(province_city_area_map, strs)
  }

  def _fill_province_city_area_map(hashMap: util.HashMap[(String, String, String), (String, String, String)], strs: Array[String]) = {

    var sheng: String = strs(1)
    var city_name: String = strs(2)
    var area_name: String = strs(3)

    if (sheng.endsWith("省") || sheng.endsWith("市")) {
      sheng = sheng.substring(0, sheng.length - 1)
    } else if (sheng == "新疆维吾尔自治区") {
      sheng = "新疆"
    } else if (sheng == "内蒙古自治区") {
      sheng = "内蒙古"
    } else if (sheng == "西藏自治区") {
      sheng = "西藏"
    } else if (sheng == "广西壮族自治区") {
      sheng = "广西"
    } else if (sheng == "宁夏回族自治区") {
      sheng = "宁夏"
    } else if (sheng == "香港特别行政区") {
      sheng = "香港"
    } else if (sheng == "澳门特别行政区") {
      sheng = "澳门"
    }


    if (city_name.endsWith("市")) {
      city_name = city_name.substring(0, city_name.length - 1)
    } else if (city_name == "香港特别行政区") {
      city_name = "香港"
    } else if (city_name == "澳门特别行政区") {
      city_name = "澳门"
    }

    if (area_name.endsWith("市")) {
      area_name = area_name.substring(0, area_name.length - 1)
    }


    if (!hashMap.containsKey((sheng, "", ""))) {
      hashMap.put((sheng, "", ""), (sheng, city_name, area_name))
    }

    if (!hashMap.containsKey(("", city_name, ""))) {
      hashMap.put(("", city_name, ""), (sheng, city_name, area_name))
    }

    if (!hashMap.containsKey(("", "", area_name))) {
      hashMap.put(("", "", area_name), (sheng, city_name, area_name))
    }

  }

  def transform(location_str: String, umap: Map[String, String]): String = {
    var location_strs = location_str
    if (location_str.length > 4) {
      if (location_strs.startsWith("中国") || location_strs.contains("（") || location_strs.contains("）") || location_strs.contains("自由贸易试验区") || location_strs.contains("伊犁州")) {
        location_strs = location_strs.replace("中国", "")
          .replaceAll("（", "")
          .replaceAll("）", "")
          .replaceAll("\\)", "")
          .replaceAll("\\(", "")
          .replace("伊犁州", "伊犁哈萨克自治州")
      }
      if (!(location_strs.startsWith("上海") || location_strs.startsWith("重庆") ||
        location_strs.startsWith("天津") || location_strs.startsWith("北京"))
        && location_strs.contains("自由贸易试验区")) {
        location_strs = location_strs.replaceAll("自由贸易试验区", "")
      }

      if (location_strs.substring(0, 3).contains("省") || location_strs.startsWith("上海市") || location_strs.startsWith("重庆市") || location_strs.startsWith("天津市") || location_strs.startsWith("北京市")) {

        transform(location_strs, umap, cut = false, lookahead = 8, pos_sensitive = false)

      } else if (location_strs.startsWith("新疆维吾尔自治区") || location_strs.startsWith("内蒙古自治区") || location_strs.startsWith("西藏自治区") || location_strs.startsWith("广西壮族自治区") || location_strs.startsWith("宁夏回族自治区") || location_strs.startsWith("香港特别行政区") || location_strs.startsWith("澳门特别行政区")) {

        transform(location_strs, umap, cut = false, lookahead = 13, pos_sensitive = false)

      } else if (location_strs.startsWith("上海路")) {
        "//"
      } else if (location_strs.startsWith("新疆") || location_strs.startsWith("内蒙") || location_strs.startsWith("西藏") || location_strs.startsWith("广西") || location_strs.startsWith("宁夏") || location_strs.startsWith("香港") || location_strs.startsWith("澳门")) {

        transform(location_strs, umap, cut = false, lookahead = 7, pos_sensitive = false)

      } else {
        transform(location_strs, umap, cut = false, lookahead = 5, pos_sensitive = false)
      }
    } else {
      "//"
    }
  }

  """将地址描述字符串转换以"省","市","区"
        Args:
            locations:地址描述字符集合,可以是list, Series等任意可以进行for in循环的集合
                      比如:["徐汇区虹漕路461号58号楼5楼", "泉州市洛江区万安塘西工业区"]
            umap:自定义的区级到市级的映射,主要用于解决区重名问题,如果定义的映射在模块中已经存在，则会覆盖模块中自带的映射
            cut:是否使用分词，默认使用，分词模式速度较快，但是准确率可能会有所下降
            lookahead:只有在cut为false的时候有效，表示最多允许向前看的字符的数量
                      默认值为8是为了能够发现"新疆维吾尔族自治区"这样的长地名
                      如果你的样本中都是短地名的话，可以考虑把这个数字调小一点以提高性能
            pos_sensitive:如果为True则会多返回三列，分别提取出的省市区在字符串中的位置，如果字符串中不存在的话则显示-1  默认false
        Returns:
            省市区信息，如下：
               省    /市   /区
               上海市/上海市/徐汇区
               福建省/泉州市/洛江区
    """

  def transform(location_strs: String, umap: Map[String, String], cut: Boolean, lookahead: Int, pos_sensitive: Boolean): String = {
    val cpca: Cpca = _handle_one_record(location_strs, umap, cut, lookahead, pos_sensitive)

    addProvince(cpca.getV("省")) + "/" + addCity(cpca.getV("市")) + "/" + cpca.getV("区")
  }

  def addCity(text: String) = {
    if (StringUtils.isNotBlank(text)) {
      if ((text.length > 4 && text.endsWith("州")) || (text.length > 4 && text.endsWith("区"))) {
        text
      } else {
        text + "市"
      }
    } else {
      ""
    }
  }

  def addProvince(text: String) = {
    if (StringUtils.isNotBlank(text)) {
      if (text.contains("重庆") || text.contains("天津") || text.contains("上海") || text.contains("北京")) {
        text + "市"
      } else if (text.contains("新疆")) {
        "新疆维吾尔自治区"
      } else if (text.contains("内蒙古")) {
        "内蒙古自治区"
      } else if (text.contains("西藏")) {
        "西藏自治区"
      } else if (text.contains("广西")) {
        "广西壮族自治区"
      } else if (text.contains("宁夏")) {
        "宁夏回族自治区"
      } else if (text.contains("香港")) {
        "香港特别行政区"
      } else if (text.contains("澳门")) {
        "澳门特别行政区"
      } else {
        text + "省"
      }
    } else {
      ""
    }
  }

  def _handle_one_record(addr: String, umap: Map[String, String], cut: Boolean, lookahead: Int, pos_sensitive: Boolean) = {
    if (addr == "" || addr == null) {
      val cpca = new Cpca()
      cpca
    } else {
      val cpca: Cpca = _extract_addr(addr, cut, lookahead, umap)
      cpca
    }

  }

  def _extract_addr(addr: String, cut: Boolean, lookhead: Int, umap: Map[String, String]): Cpca = {
    if (cut) _jieba_extract(addr)
    else _full_text_extract(addr, lookhead, umap)
  }

  def _jieba_extract(addr: String): Cpca = {
    val cpca = new Cpca()

    import scala.collection.JavaConverters._
    val jieba = new JiebaSegmenter
    val strs: mutable.Seq[String] = jieba.sentenceProcess(addr).asScala
    for (elem <- strs) {
      if (province_city_area_map.containsKey(("", "", elem))) cpca.map.put("区", province_city_area_map.get(("", "", elem))._3)
      else if (province_city_area_map.containsKey(("", elem, ""))) cpca.map.put("市", province_city_area_map.get(("", elem, ""))._2)
      else if (province_city_area_map.containsKey((elem, "", ""))) cpca.map.put("省", province_city_area_map.get((elem, "", ""))._1)
    }
    cpca
  }


  def main(args: Array[String]): Unit = {

    val result: String = runner("中国（上海）自由贸易试验区潍坊西路126号1层")
    val result2: String = runner("中国（上海）自由贸易试验区东方路1381号蓝村大厦18F")



    println(result)
    println(result2)
  }

  def _full_text_extract(addr: String, lookahead: Int, umap: Map[String, String]) = {
    try {
      val cpca = new Cpca
      var i = 0
      var addr1 = ""
      if (addr.startsWith("新疆维吾尔自治区")) {
        addr1 = addr.replace("新疆维吾尔自治区", "新疆省")
      } else if (addr.startsWith("内蒙古自治区")) {
        addr1 = addr.replace("内蒙古自治区", "内蒙古省")
      } else if (addr.startsWith("西藏自治区")) {
        addr1 = addr.replace("西藏自治区", "西藏省")
      } else if (addr.startsWith("广西壮族自治区")) {
        addr1 = addr.replace("广西壮族自治区", "广西省")
      } else if (addr.startsWith("宁夏回族自治区")) {
        addr1 = addr.replace("宁夏回族自治区", "宁夏省")
      } else if (addr.startsWith("香港特别行政区")) {
        addr1 = addr.replace("香港特别行政区", "香港省")
      } else if (addr.startsWith("澳门特别行政区")) {
        addr1 = addr.replace("澳门特别行政区", "澳门省")
      } else {
        addr1 = addr
      }

      while (i < addr1.length) {
        breakable {
          for (x <- 1 until lookahead + 1) {
            var elem = addr1.substring(i, i + x)
            if (province_city_area_map.containsKey((elem, "", ""))) {
              for (x <- 1 until lookahead + 1) {
                if (elem.startsWith("北京") || elem.startsWith("重庆") || elem.startsWith("上海") || elem.startsWith("天津")) {
                  var elem_city = addr1.substring(i + elem.length + 1, i + x + elem.length + 1)
                  //这种情况是只有市区的解析 如 北京市朝阳区
                  if (province_city_area_map.containsKey(("", "", elem_city))) {
                    cpca.map.put("区", province_city_area_map.get(("", "", elem_city))._3)
                    cpca.map.put("市", province_city_area_map.get(("", "", elem_city))._2)
                    cpca.map.put("省", province_city_area_map.get(("", "", elem_city))._1)
                    break()
                  }
                }
                //这种判断省份后面有省字的情况
                if (addr1.substring(0, 4).contains("省")) {
                  var elem_city = addr1.substring(i + elem.length + 1, i + x + elem.length + 1)
                  //这种情况是正常的省市区解析
                  //这个是包含市 例如 XX省XX（市）XX县
                  if (province_city_area_map.containsKey(("", elem_city, ""))) {
                    for (x <- 1 until lookahead + 1) {
                      if (addr1.substring(0, 7).contains("市")) {
                        //判断完市，接着判断区
                        var elem_area = addr1.substring(i + elem.length + 1 + elem_city.length + 1, i + x + elem.length + 1 + elem_city.length + 1)
                        if (province_city_area_map.containsKey(("", "", elem_area))) {
                          cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                          cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                          cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                          break()
                        }
                      } else {
                        var elem_area = addr1.substring(i + elem.length + elem_city.length + 1, i + x + elem.length + elem_city.length + 1)
                        if (province_city_area_map.containsKey(("", "", elem_area))) {
                          cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                          cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                          cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                          break()
                        }
                      }
                    }
                    //没有区就返回市级对应的信息 例如xx省xx市
                    cpca.map.put("区", province_city_area_map.get(("", elem_city, ""))._3)
                    cpca.map.put("市", province_city_area_map.get(("", elem_city, ""))._2)
                    cpca.map.put("省", province_city_area_map.get(("", elem_city, ""))._1)
                    break()
                  }
                  //这个是不包含市 例如 XX省XX县
                  if (province_city_area_map.containsKey(("", "", elem_city))) {
                    cpca.map.put("区", province_city_area_map.get(("", "", elem_city))._3)
                    cpca.map.put("市", province_city_area_map.get(("", "", elem_city))._2)
                    cpca.map.put("省", province_city_area_map.get(("", "", elem_city))._1)
                    break()
                  }
                  //这种判断省份后面没有省字的情况
                } else {
                  var elem_city = addr1.substring(i + elem.length, i + x + elem.length)
                  if (province_city_area_map.containsKey(("", elem_city, ""))) {
                    for (x <- 1 until lookahead + 1) {
                      //这种情况是xxyy市看看又没有县级
                      if (addr1.substring(0, 6).contains("市")) {
                        var elem_area = addr1.substring(i + elem.length + elem_city.length + 1, i + x + elem.length + elem_city.length + 1)
                        if (province_city_area_map.containsKey(("", "", elem_area))) {
                          cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                          cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                          cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                          break()
                        }
                        //这种情况是xxyy看看又没有县级
                      } else {
                        var elem_area = addr1.substring(i + elem.length + elem_city.length + 1, i + x + elem.length + elem_city.length + 1)
                        if (province_city_area_map.containsKey(("", "", elem_area))) {
                          cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                          cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                          cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                          break()
                        }
                      }
                    }
                    //这种情况是xxyy（市）没有县级直接返回市级单位
                    cpca.map.put("区", province_city_area_map.get(("", elem_city, ""))._3)
                    cpca.map.put("市", province_city_area_map.get(("", elem_city, ""))._2)
                    cpca.map.put("省", province_city_area_map.get(("", elem_city, ""))._1)
                    break()
                  }
                  //这个是不包含市 例如 XX省XX县
                  if (province_city_area_map.containsKey(("", "", elem_city))) {
                    cpca.map.put("区", province_city_area_map.get(("", "", elem_city))._3)
                    cpca.map.put("市", province_city_area_map.get(("", "", elem_city))._2)
                    cpca.map.put("省", province_city_area_map.get(("", "", elem_city))._1)
                    break()
                  }
                }
              }
              //最终市级也没有信息，只返回一个省级信息
              cpca.map.put("省", province_city_area_map.get((elem, "", ""))._1)
              break()

              //这种情况是没有省的情况，例如xx(市)yy县
            } else if (province_city_area_map.containsKey(("", elem, ""))) {
              for (x <- 1 until lookahead + 1) {
                //这种情况是有“市字”的情况，例如xx市yy县
                if (addr1.substring(0, 4).contains("市")) {
                  var elem_area = addr1.substring(i + elem.length + 1, i + x + elem.length + 1)
                  if (province_city_area_map.containsKey(("", "", elem_area))) {
                    cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                    cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                    cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                    break()
                  }
                  //这种情况是没有“市字”的情况，例如xxyy县
                } else {
                  var elem_area = addr1.substring(i + elem.length, i + x + elem.length)
                  if (province_city_area_map.containsKey(("", "", elem_area))) {
                    cpca.map.put("区", province_city_area_map.get(("", "", elem_area))._3)
                    cpca.map.put("市", province_city_area_map.get(("", "", elem_area))._2)
                    cpca.map.put("省", province_city_area_map.get(("", "", elem_area))._1)
                    break()
                  }
                }
              }
              cpca.map.put("区", province_city_area_map.get(("", elem, ""))._3)
              cpca.map.put("市", province_city_area_map.get(("", elem, ""))._2)
              cpca.map.put("省", province_city_area_map.get(("", elem, ""))._1)
              break()

              //这种情况是没有省也没有市的情况的情况，例如yy县 直接返回yy县的直接上级省市
            } else if (province_city_area_map.containsKey(("", "", elem))) {
              cpca.map.put("区", province_city_area_map.get(("", "", elem))._3)
              cpca.map.put("市", province_city_area_map.get(("", "", elem))._2)
              cpca.map.put("省", province_city_area_map.get(("", "", elem))._1)
              break()
            }
          }
        }
        i = addr.length
      }

      //上边是正常的信息，下边是对上面返回的信息进一步确认有没有重名地区，有的话单独映射
      if (StringUtils.isNotBlank(cpca.getV("区"))) {
        if (cpca.getV("区").contains("区")) {
          val key1 = cpca.getV("区")
          if (umap.contains(key1)) {
            val value = umap(key1).replace("市", "")
            if (province_city_area_map.containsKey(("", value, ""))) {
              cpca.map.put("区", province_city_area_map.get(("", "", key1))._3)
              cpca.map.put("市", province_city_area_map.get(("", value, ""))._2)
              cpca.map.put("省", province_city_area_map.get(("", value, ""))._1)
            }
          }
        } else {
          val key1 = cpca.getV("区") + "区"
          if (umap.contains(key1)) {
            val value = umap(key1).replace("市", "")
            if (province_city_area_map.containsKey(("", value, ""))) {
              cpca.map.put("区", province_city_area_map.get(("", "", key1))._3)
              cpca.map.put("市", province_city_area_map.get(("", value, ""))._2)
              cpca.map.put("省", province_city_area_map.get(("", value, ""))._1)
            }
          }
        }
      }
      cpca
    }
    catch {
      case e: Exception => new Cpca();
    }
  }
}
