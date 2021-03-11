package com.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SpringBootApplication
public class TestApplication {

	static int offset;
	static Map<String, Integer> mapTotal = new HashMap<String, Integer>();
	static Map<String, Integer> mapCounter = new HashMap<String, Integer>();
	static boolean flag = false;

	static final OkHttpClient httpClient = new OkHttpClient();

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);

		ExecutorService es = Executors.newSingleThreadExecutor();

		for (offset = 0; offset < 900; offset = offset + 50) {
			es.submit(() -> {
				getData();
				if (flag == false) {
					calculate();
				}
			});
		}
	}

	private static void calculate() {

		Map<String, Integer> map = new HashMap<String, Integer>();

		if (offset == 900) {
			flag = true;
			System.out.println("\n");
			for (String key : mapCounter.keySet()) {
				map.put(key, mapTotal.get(key) / mapCounter.get(key));
			}
			for (String key : map.keySet()) {
				System.out.println(key + " Promedio: $" + map.get(key));
			}
		}
	}

	private static void getData() {
		String result = null;
		Request request = new Request.Builder()
				.url("https://api.mercadolibre.com/sites/MLA/search?category=MLA1763&ITEM_CONDITION=2230284&offset="
						+ offset)
				.addHeader("Authorization",
						"Bearer APP_USR-5462365307209179-012401-81c38576a38e70f623cc9931f79e58a5-202388516")
				.build();
		try (Response response = httpClient.newCall(request).execute()) {
			result = response.body().string();
			JsonObject json = new Gson().fromJson(result, JsonObject.class);
			JsonArray results = (JsonArray) json.getAsJsonArray("results");
			results.forEach(m -> {
				JsonObject moto = (JsonObject) m;
				JsonArray attributes = (JsonArray) moto.get("attributes");
				attributes.forEach(a -> {
					JsonObject attribute = (JsonObject) a;
					try {
						if (attribute.get("id").getAsString().equals("BRAND")) {
//							System.out.println(attribute.get("value_name").getAsString());
//							System.out.println(moto.get("price").getAsString());
							if (mapTotal.containsKey(attribute.get("value_name").getAsString())) {
								mapTotal.put(attribute.get("value_name").getAsString(),
										mapTotal.get(attribute.get("value_name").getAsString())
												+ moto.get("price").getAsInt());
//								System.out.println(mapTotal.get(attribute.get("value_name").getAsString()));
								mapCounter.put(attribute.get("value_name").getAsString(),
										mapCounter.get(attribute.get("value_name").getAsString()) + 1);
							} else {
								mapTotal.put(attribute.get("value_name").getAsString(), moto.get("price").getAsInt());
								mapCounter.put(attribute.get("value_name").getAsString(), 1);
//								System.out.println(mapTotal.get(attribute.get("value_name").getAsString()));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				});
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
