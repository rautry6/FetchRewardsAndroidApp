package com.example.fetchrewards;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Item {
    int id;
    int listId;
    String name;

    Item(int id, int listId, String name) {
        this.id = id;
        this.listId = listId;
        this.name = name;
    }
}

public class MainActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private List<TextView> textViews;
    private GridLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollView = new ScrollView(this);
        layout = new GridLayout(this);
        textViews = new ArrayList<>();


        scrollView.addView(layout);

        setContentView(scrollView);

        fetchData();
    }

    private void fetchData() {
        new Thread(() -> {
            List<Item> items = fetchItems();
            runOnUiThread(() -> displayItems(items));
        }).start();
    }

    private List<Item> fetchItems() {
        List<Item> items = new ArrayList<>();
        try {
            // fetch data from url
            URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder jsonString = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonString.toString());

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");

                int listId = jsonObject.getInt("listId");

                String name = jsonObject.optString("name", null);

                if (name != "null" && !name.trim().isEmpty()) {
                    items.add(new Item(id, listId, name));
                }
            }

            // sort items by list id and then by name
            Collections.sort(items, Comparator.comparingInt((Item i) -> i.listId).thenComparing(i -> i.name));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    private void displayItems(List<Item> items) {
        HashMap<Integer, ArrayList<Item>> groupedItems = new HashMap<>();
        for (Item item : items) {
            // if list id has not been added to map yet, create a new entry
            if (!groupedItems.containsKey(item.listId)) {
                groupedItems.put(item.listId, new ArrayList<>());
            }

            // add the item to the appropriate entry list
            groupedItems.get(item.listId).add(item);
        }


        for (Map.Entry<Integer, ArrayList<Item>> entry : groupedItems.entrySet()) {
            TextView textView = new TextView(this);

            // reset text for next list
            String text = "";

            // format each header
            text += "List ID: " + entry.getKey() + "\t\n";

            // make list
            for (Item item : entry.getValue()) {
                text += "  - " + item.name + "\n";
            }

            // update text view for that list and add to views list
            textView.setText(text);
            textViews.add(textView);


        }

        // add the text layouts to the layout
        for(TextView textView : textViews){
            layout.addView(textView);
        }

    }
}
