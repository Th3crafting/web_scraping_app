package com.utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class ConexionPagina {
    public void scrape(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Elements titles = doc.select(".titleline");

            PrintWriter writer = new PrintWriter("resultados.csv", "UTF-8");

            for (Element title : titles){
                writer.println(title.text() + ";");
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
