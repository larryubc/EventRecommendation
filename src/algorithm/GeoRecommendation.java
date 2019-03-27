package algorithm;

import db.DBConnection;
import entity.Item;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

public class GeoRecommendation {

    public List<Item> recommendItems(String userId, double lat, double lon){
        List<Item> recommendeItems = new ArrayList<>();
        DBConnection conn = DBConnectionFactory.getDBConnection();
        Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
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


        Set<Item> visitedItems = new HashSet<>();

        for(Map.Entry<String,Integer> category: categoryList) {
            List<Item> items = conn.searchItems(lat,lon,category.getKey());
            List<Item> filteredItems = new ArrayList<>();

            for(Item item : items) {
                if (favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
                    filteredItems.add(item);

                }
            }

            Collections.sort(filteredItems, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
                    return Double.compare(item1.getDistance(),item2.getDistance());
                }
            });

            visitedItems.addAll(items);
            recommendeItems.addAll(filteredItems);
        }

        return recommendeItems;

    }
}
