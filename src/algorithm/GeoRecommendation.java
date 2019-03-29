package algorithm;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

import java.util.*;

public class GeoRecommendation {

    public List<Item> recommendItems(String userId, double lat, double lon){
        List<Item> recommendeItems = new ArrayList<>();
        DBConnection conn = DBConnectionFactory.getDBConnection();
        Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
        System.out.println(favoriteItemIds.size());
        Map<String,Integer> allCategories = new HashMap<>();


        for(String itemId: favoriteItemIds){
            Set<String> categories = conn.getCategories(itemId);
            for(String category : categories) {
                allCategories.put(category,allCategories.getOrDefault(category,0)+1);
            }
        }


        List<Map.Entry<String,Integer>> categoryList = new ArrayList<Map.Entry<String,Integer>>(allCategories.entrySet());

        Collections.sort(categoryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return Integer.compare(o2.getValue(),o1.getValue());
            }
        });

        System.out.println("catagretlist has size of " + categoryList.size());


        Set<Item> visitedItems = new HashSet<>();

        for(Map.Entry<String,Integer> category: categoryList) {
            List<Item> items = conn.searchItems(lat,lon,category.getKey());
            List<Item> filteredItems = new ArrayList<>();

            for(Item item : items) {
                if (!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
                    filteredItems.add(item);

                }
            }

            Collections.sort(filteredItems, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
                    double distance1 = getDistance(item1.getLatitude(), item1.getLongitude(), lat, lon);
                    double distance2 = getDistance(item2.getLatitude(), item2.getLongitude(), lat, lon);
                    if (distance1 == distance2) {
                        return 0;
                    }
                    return distance1 < distance2 ? -1 : 1;
                }
            });

            visitedItems.addAll(items);
            recommendeItems.addAll(filteredItems);
        }

        return recommendeItems;

    }


    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.sin(dlat / 2 / 180 * Math.PI) * Math.sin(dlat / 2 / 180 * Math.PI)
                + Math.cos(lat1 / 180 * Math.PI) * Math.cos(lat2 / 180 * Math.PI) * Math.sin(dlon / 2 / 180 * Math.PI)
                * Math.sin(dlon / 2 / 180 * Math.PI);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // Radius of earth in miles.
        double R = 3961;
        return R * c;
    }
}
