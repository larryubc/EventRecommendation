package rpc;


import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;


@WebServlet("/search")
public class SearchItem extends HttpServlet {


    public SearchItem() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONArray array = new JSONArray();



        try {

            String userId = request.getParameter("user_id");
            System.out.println(request);
            double lat = Double.parseDouble(request.getParameter("lat"));
            double lon = Double.parseDouble(request.getParameter("lon"));
            String keyword = request.getParameter("term");



            DBConnection conn = DBConnectionFactory.getDBConnection();
            List<Item> items = conn.searchItems(lat, lon, keyword);

            Set<String> favorite = conn.getFavoriteItemIds(userId);


            for (Item item : items) {
                JSONObject obj = item.toJSONObject();
                if (favorite != null) {
                    obj.put("favorite", favorite.contains(item.getItemId()));
                }
                array.put(obj);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(request);
        }

        RpcHelper.writeJsonArray(response,array);


    }


}
