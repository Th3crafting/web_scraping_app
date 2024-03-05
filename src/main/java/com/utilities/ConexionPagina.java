package com.utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class ConexionPagina {
    public void scrape(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Elements titles = doc.select(".titleline");
            Elements timeStamps = doc.select(".age");

            PrintWriter writer = new PrintWriter("resultados.csv", "UTF-8");

            for(int i = 0; i < titles.size(); i++){
                String titleText = titles.get(i).text();

                String timeStampText = timeStamps.get(i).text();

                writer.println(titleText + ";" + timeStampText + ";");
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
