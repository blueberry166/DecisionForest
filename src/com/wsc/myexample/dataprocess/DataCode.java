package com.wsc.myexample.dataprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;
/**
 * 为包含中文字符的数据重新编码
 * @author Administrator
 *
 */
public class DataCode {

	public HashMap<String, HashMap<String, String>> codemapLst = new HashMap<String, HashMap<String, String>>();
	public HashMap<String, Integer> codeIdmap = new HashMap<String, Integer>();

	public void loadMap(String mapPath) {
		String json;
		try {
			BufferedReader br = new BufferedReader(new FileReader(mapPath));
			json = br.readLine();
			br.close();

			codemapLst = (HashMap<String, HashMap<String, String>>) parserToMap(json);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// private static int codeIdNo = 0;
	public String labelDataforBuild(String originData)
			throws NumberFormatException, IOException {
		String output = originData.substring(0, originData.indexOf("."))
				+ "_code.txt";
		File fout = new File(output);
		if (fout.exists()) {
			fout.delete();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(originData), "GBK"));
		String line;
		String[] arr;
		String wline = null;
		int cnt = 0;

		HashMap<String, String> imap;

		while ((line = br.readLine()) != null && !"".equals(line)) {
			arr = line.split("(\t|,)");
			if (arr.length < 31)
				continue;

			// 去掉第一列和第二列
			wline = "";
			String code;
			for (int i = 2; i < 31; i++) {

				if (containsChinese(arr[i])) {

					if (codemapLst.containsKey(i)) {
						imap = codemapLst.get(i);
					} else {
						imap = new HashMap<String, String>();
					}

					if (!imap.containsKey(arr[i])) {

						int codeid;// maxcodeid
						if (codeIdmap.containsKey(i)) {
							codeid = codeIdmap.get(i) + 1;// maxcodeid
						} else {
							codeid = 0;
						}

						code = i + "A" + codeid;
						imap.put(arr[i], code);

						codeIdmap.put(i + "", codeid);// update maxid

					} else {
						code = imap.get(arr[i]);
					}

					codemapLst.put(i + "", imap);

				} else {
					code = arr[i];
				}

				wline += code + ",";
			}

			// 添加后面
			for (int i = 32; i < arr.length; i++) {
				wline += arr[i] + ",";
			}
			
			bw.write(wline.substring(0, wline.length()-1) + "\r\n");
			
			// 最后添加是否欠费
//			if (Double.parseDouble(arr[31]) > 0) {
//				bw.write(wline + "1" + "\r\n");
//				cnt++;
//			} else {
//				bw.write(wline + "0" + "\r\n");
//			}
		}
		br.close();
		bw.close();

		System.out.println(cnt);

		// 保存map
		String mapPath = originData.substring(0, originData.indexOf("."))
				+ "_map.txt";
		JSONObject jsonObject = JSONObject.fromObject(codemapLst);
		// System.out.println(jsonObject);
		// String json = jsonObject.toString();

		BufferedWriter bwjson = new BufferedWriter(new FileWriter(mapPath));
		bwjson.write(jsonObject.toString());
		bwjson.close();

		return output;
	}

	public String labelDataforPredict(String predictData)
			throws NumberFormatException, IOException {
		String predict_out = predictData.substring(0, predictData.indexOf(".")) + "_labeled.txt";
		File fout = new File(predict_out);
		if (fout.exists()) {
			fout.delete();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(predict_out), "GBK"));
		String line;
		String[] arr;
		String wline = null;
		int cnt = 0;

		HashMap<String, String> imap;

		while ((line = br.readLine()) != null && !"".equals(line)) {
			arr = line.split("(\t|,)");
			if (arr.length < 31)
				continue;

			// 去掉第一列和第二列
			wline = "";
			String code;
			for (int i = 2; i < 31; i++) {

				if (containsChinese(arr[i])) {
					imap = codemapLst.get(i);

					if (!imap.containsKey(arr[i])) {
						code = arr[i];
					} else {
						code = imap.get(arr[i]);
					}
					codemapLst.put(i+"", imap);

				} else {
					code = arr[i];
				}

				wline += code + ",";
			}

			// 添加后面
			for (int i = 32; i < arr.length; i++) {
				wline += arr[i] + ",";
			}
			
			bw.write(wline.substring(0, wline.length()-1) + "\r\n");
		}
		br.close();
		bw.close();

		System.out.println(cnt);

		return predict_out;
	}

	public static Map parserToMap(String s) {
		Map map = new HashMap();
		JSONObject json = JSONObject.fromObject(s);
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = json.get(key).toString();
			if (value.startsWith("{") && value.endsWith("}")) {
				map.put(key, parserToMap(value));
			} else {
				map.put(key, value);
			}

		}
		return map;
	}

	/**
	 * 判断是否包含汉字
	 * 
	 * @param args
	 */
	public boolean containsChinese(String str) {
		return (str.getBytes().length == str.length()) ? false : true;
	}

	//将category类型转为number
	public String CtgToNumber(String path) throws IOException {
		String output = path.substring(0, path.indexOf(".")) + "_number.txt";
		File fout = new File(output);
		if (fout.exists()) {
			fout.delete();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
		BufferedReader br = new BufferedReader(new FileReader(path));

		String line;
		String[] arr;
		while ((line = br.readLine()) != null) {
			arr = line.split(",");
			
			line = "";
			for(int i = 0;i<arr.length;i++){
				if(arr[i].matches("([0-9]+A)[0-9]+?")){
					arr[i] = arr[i].replaceFirst("([0-9]+A)", "");
				}
				line += arr[i]+",";
			}
			
			bw.write(line.substring(0,line.length()-1)+"\r\n");
		}

		br.close();
		bw.close();

		return output;
	}

	public static void main(String[] args) {
		// ObjectMapper mapper = new ObjectMapper();
		// Map<String,Object> productMap = mapper.readValue(json);//转成map

		Map<String, Map> mapt = new HashMap();

		Map map = new HashMap();
		Map map2 = new HashMap();

		map.put("aaaaaa", "ccccc");
		map.put("bbbbbb", "dddddd");

		map2.put("aaaaaa", "ccccc");
		map2.put("bbbbbb", "dddddd");

		mapt.put(1 + "", map);
		mapt.put(2 + "", map2);

		JSONObject jsonObject = JSONObject.fromObject(mapt);
		System.out.println(jsonObject);

		String json = jsonObject.toString();

		// ObjectMapper mapper = new ObjectMapper();
		// Map<String,Map> mapt2 = mapper.readValue(json);//转成map

		System.out.println(parserToMap(json));
	}
}
