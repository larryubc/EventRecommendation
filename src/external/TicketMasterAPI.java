package external;

import org.json.JSONException;
import entity.Item;
import entity.Item.ItemBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketMasterAPI {
    private static final String API_HOST = "app.ticketmaster.com";
    private static final String SEARCH_PATH = "/discovery/v2/events.json";
    private static final String DEFAULT_TERM = ""; // no restriction
    private static final String API_KEY = "roXzVSWNT30RnjCUC7U0MuXnk8Nywnhl";



    public List<Item> search(double lat, double lon, String term) {


        String url =  "http://" + API_HOST + SEARCH_PATH;
        String geoHash = GeoHash.encodeGeohash(lat,lon,3);
        term = (term == null ? DEFAULT_TERM : term);
        term = urlEncodeHelper(term);
        String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=150", API_KEY, geoHash, term);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            System.out.println();
            System.out.println("\nSending 'GET' request to URL : " + url + "?" + query); // use for debug
            System.out.println("Response Code : " + responseCode); // use for debug

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response =  new StringBuilder();
            while ((inputLine = in.readLine())!= null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responJson = new JSONObject(response.toString());
            JSONObject embedded = (JSONObject) responJson.get("_embedded");
            JSONArray events = embedded.getJSONArray("events");
            return getItemList(events);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<Item>();


    }

    private List<Item> getItemList(JSONArray events) throws JSONException {
        List<Item> itemList = new ArrayList<>();

        for (int i = 0; i < events.length(); i++) {

            JSONObject event = events.getJSONObject(i);

            ItemBuilder builder = new ItemBuilder();

            if (!event.isNull("name")) {
                builder.setName(event.getString("name"));
            }

            if (!event.isNull("id")) {
                builder.setItemId(event.getString("id"));
            }

            if (!event.isNull("url")) {
                builder.setUrl(event.getString("url"));
            }

            if (!event.isNull("rating")) {
                builder.setRating(event.getDouble("rating"));
            }

            if (!event.isNull("distance")) {
                builder.setDistance(event.getDouble("distance"));
            }

            builder.setCategories(getCategories(event));
            builder.setAddress(getAddress(event));
            builder.setImageUrl(getImageUrl(event));
            itemList.add(builder.build());
        }

        return itemList;
    }

    private String getImageUrl(JSONObject events) throws JSONException {
        if(!events.isNull("images")) {
            JSONArray images = events.getJSONArray("images");

            for(int i = 0; i< images.length(); ++i) {
                JSONObject image = images.getJSONObject(i);

                if(!image.isNull("url")) {
                    return image.getString("url");
                }
            }
        }
        return "";
    }

    private Set<String> getCategories (JSONObject events) throws JSONException {
        Set<String> categories = new HashSet<>();
        JSONArray classifications = events.getJSONArray("classifications");
        for(int i = 0; i < classifications.length() ; i++) {
            JSONObject classification = classifications.getJSONObject(i);
            JSONObject segment = classification.getJSONObject("segment");
            categories.add(segment.getString("name"));
        }
        return categories;
    }

    private String getAddress (JSONObject event) throws JSONException {
        if (!event.isNull("_embedded")) {
            JSONObject embedded = event.getJSONObject("_embedded");
            if(!embedded.isNull("venues")) {
                JSONArray venues = embedded.getJSONArray("venues");

                for(int i = 0;i < venues.length();++i) {
                    JSONObject venue = venues.getJSONObject(i);

                    StringBuilder sb = new StringBuilder();

                    if(!venue.isNull("address")) {
                        JSONObject address =venue.getJSONObject("address");
                        if (!address.isNull("line1")) {
                            sb.append(address.getString("line1"));
                        }
                        if (!address.isNull("line2")) {
                            sb.append(" ");
                            sb.append(address.getString("line2"));
                        }
                        if (!address.isNull("line3")) {
                            sb.append(" ");
                            sb.append(address.getString("line3"));
                        }
                    }
                    if(!venue.isNull("city")) {
                        JSONObject city = venue.getJSONObject("city");

                        if (!city.isNull("name")) {
                            sb.append(" ");
                            sb.append(city.getString("name"));
                        }
                    }
                    if (!sb.toString().equals("")) {
                        return sb.toString();
                    }
                }
            }
        }
        return "";
    }





    private String urlEncodeHelper(String term) {
        try {
            term = java.net.URLEncoder.encode(term, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return term;
    }

    private void queryAPI(double lat, double lon) {
        List<Item> itemList = search(lat, lon, null);
        try {
            for (Item item: itemList) {

                JSONObject itemJson = item.toJSONObject();
                System.out.println(itemJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        TicketMasterAPI tmApi = new TicketMasterAPI();

        tmApi.queryAPI(39.682684, -95.295410);
    }





}